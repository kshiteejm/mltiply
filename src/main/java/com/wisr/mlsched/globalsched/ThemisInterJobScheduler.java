package com.wisr.mlsched.globalsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.wisr.mlsched.ClusterEventQueue;
import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.events.ResourceAvailableEvent;
import com.wisr.mlsched.job.Bid;
import com.wisr.mlsched.localsched.IntraJobScheduler;
import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

import gurobi.*;

public class ThemisInterJobScheduler extends InterJobScheduler {

	public List<Bid> pickWinningBids(List<GPU> gpu_set, List<Bid> bid_set) {
		List<Bid> winners = new ArrayList<Bid>();
		GRBEnv env;
		GRBModel solver;
		try {
			env = new GRBEnv("themis_gurobi.log");
			env.set(GRB.IntParam.LogToConsole, 0);
			solver = new GRBModel(env);
			solver.set(GRB.DoubleParam.TimeLimit, 10.0);
			// create helpers and gurobi variables per bid
			HashMap<GPU, Set<Bid>> bidsPerGPU = new HashMap<GPU, Set<Bid>>();
			HashMap<Bid, GRBVar> bidVariables = new HashMap<Bid, GRBVar>();
			HashMap<IntraJobScheduler, Set<Bid>> bidsPerJob = new HashMap<IntraJobScheduler, Set<Bid>>();
			for (Bid bid: bid_set) {
				bidVariables.put(bid, solver.addVar(0.0, 1.0, 0.0, GRB.BINARY, "Bid: " + bid.toString()));
				for (GPU gpu: bid.getGPUList()) {
					if (bidsPerGPU.containsKey(gpu)) {
						bidsPerGPU.get(gpu).add(bid);
					} else {
						Set<Bid> bids = new HashSet<Bid>();
						bids.add(bid);
						bidsPerGPU.put(gpu, bids);
					}
				}
				if (bidsPerJob.containsKey(bid.getJob())) {
					bidsPerJob.get(bid.getJob()).add(bid);
				} else {
					Set<Bid> bids = new HashSet<Bid>();
					bids.add(bid);
					bidsPerJob.put(bid.getJob(), bids);
				}
			}
			solver.update();
			// create gurobi constraints per gpu, per job
			for (GPU gpu: bidsPerGPU.keySet()) {
				GRBLinExpr packingConstraint = new GRBLinExpr();
				for (Bid bid: bidsPerGPU.get(gpu)) {
					packingConstraint.addTerm(1.0, bidVariables.get(bid));
				}
				solver.addConstr(packingConstraint, GRB.LESS_EQUAL, 1.0, "");
			}
			for (IntraJobScheduler job: bidsPerJob.keySet()) {
				GRBLinExpr jobMaxParallelConstraint = new GRBLinExpr();
				for (Bid bid: bidsPerJob.get(job)) {
					jobMaxParallelConstraint.addTerm(bid.getGPUList().size(), bidVariables.get(bid));
				}
				solver.addConstr(jobMaxParallelConstraint, GRB.LESS_EQUAL, 
						job.getMaxParallelism() - job.getGPUsAvailableForNextIteration().size(),  "");
			}
			// create gurobi objective for maximizing product of bid valuations
			GRBLinExpr valuationObjective = new GRBLinExpr();
			for (Bid bid: bid_set) {
//				// potential problem in logarithm
//				double logExpectedBenefit = -1000;
//				if (Double.compare(bid.getExpectedBenefit(), 1) < 0) {
//					logExpectedBenefit = Math.log10(bid.getExpectedBenefit());
//				}
//				if (Double.compare(bid.getExpectedBenefit(), 0) == 0) {
//					logExpectedBenefit = -1000;
//				
				double logExpectedBenefit = Math.log(bid.getExpectedBenefit());
				//if (Double.compare(bid.getExpectedBenefit(), 1) > 0) {
				//	logExpectedBenefit = Math.log(bid.getExpectedBenefit());
				//}
				valuationObjective.addTerm(logExpectedBenefit, bidVariables.get(bid));
			}
			solver.setObjective(valuationObjective, GRB.MINIMIZE);
			solver.update();
			// optimize
			solver.optimize();
			if (solver.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL
					|| solver.get(GRB.IntAttr.Status) == GRB.Status.TIME_LIMIT ) {
				double maxValuation = solver.get(GRB.DoubleAttr.ObjVal);
				for (Bid bid: bidVariables.keySet()) {
					GRBVar var = bidVariables.get(bid);
					if (var.get(GRB.DoubleAttr.X) == 1) {
						winners.add(bid);
					}
				}
			} else {
				solver.computeIIS();
	            solver.write(".quark-unsat.ilp");
				System.out.println("UNSOLVABLE.");
			}
		} catch (GRBException e) {
			e.printStackTrace();
		}
		return winners;
	}

	@Override
	public void onResourceAvailable(List<GPU> gpu_set) {
		if(Cluster.getInstance().getRunningJobs().size() == 0) {
			// Means no jobs are running, hence nothing to do
			return;
		}
		List<IntraJobScheduler> allJobsInCluster = Cluster.getInstance().getRunningJobs();
		List<Integer> jobsToConsider = new ArrayList<>();
		for(IntraJobScheduler job : allJobsInCluster) {
			jobsToConsider.add(job.getJobId());
		}
		gpu_set = gpu_set.stream().filter(g -> !g.isLeased()).collect(Collectors.toList());
		while(gpu_set.size() > 0) {
			List<JobFairness> fairnessValues = new ArrayList<JobFairness>();
			List<IntraJobScheduler> runningJobs = Cluster.getInstance().getRunningJobs();
			for(IntraJobScheduler job: runningJobs) {
				if(jobsToConsider.contains(job.getJobId())) {
					fairnessValues.add(new JobFairness(job, job.getCurrentEstimateForThemis()/job.getIdealEstimate()));
				}
			}
			if(fairnessValues.size() == 0) {
				break;
			}
			// Sort in descending order of fairness - Most unfair to most fair
			Collections.sort(fairnessValues, new JobFairnessComparator());
			double d = Cluster.getInstance().getFairnessThreshold();
			
			// We need to now check which jobs to consider for bidding
			int num_jobs_to_consider = Math.max((int) (d*fairnessValues.size()), 1);
			double lowest_value_to_consider = fairnessValues.get(num_jobs_to_consider-1).getFairnessValue();
			// These are jobs we will definitely consider for bidding.
			List<JobFairness> fairnessValuesDefinite = new ArrayList<>();
			// These are jobs that are tied at the lowest Ts/Ti. We will randomly pick jobs from this list.
			List<JobFairness> fairnessValuesProbable = new ArrayList<>();
			for(JobFairness job: fairnessValues) {
				if(Double.compare(job.getFairnessValue(), lowest_value_to_consider) > 0) {
					fairnessValuesDefinite.add(job);
				} else if(Double.compare(job.getFairnessValue(), lowest_value_to_consider) == 0) {
					fairnessValuesProbable.add(job);
				}
			}
			List<JobFairness> allJobs = new ArrayList<>();
			// First add in all definite jobs
			allJobs.addAll(fairnessValuesDefinite);
			// Shuffle and add in probable jobs
			Collections.shuffle(fairnessValuesProbable, new Random(0));
			allJobs.addAll(fairnessValuesProbable);
			// Take number of jobs stipulated by fairness knob
			allJobs = allJobs.subList(0, num_jobs_to_consider);
			
			// Get bids from above jobs
			List<Bid> bids = new ArrayList<Bid>();
			for(JobFairness job: allJobs) {
				List<Bid> bidsFromJob = job.getJob().prepareBid(gpu_set);
				if(bidsFromJob != null && !bidsFromJob.isEmpty()) {
					bids.addAll(bidsFromJob);
				}
			}
			
			// If no bids, let's see if others not selected have any resources requirements
			if(bids.size() == 0) {
				for(JobFairness job : allJobs) {
					jobsToConsider.remove(new Integer(job.getJob().getJobId()));
				}
				continue;
			}
			List<GPU> remainingGPUSet = new ArrayList<>(gpu_set);
			//System.out.println(gpu_set.toString());
			// Restrict the number of bids here
			List<Bid> finalBids = new ArrayList<Bid>();
			Map<Integer, List<Bid>> resourceNumberToBidMap = new HashMap<Integer, List<Bid>>();
			for(int i=1;i<=gpu_set.size();i++) {
				resourceNumberToBidMap.put(i, new ArrayList<Bid>());
			}
			// Map all bids into resource map
			for(Bid bid : bids) {
				resourceNumberToBidMap.get(bid.getGPUList().size()).add(bid);
			}
			
			// For each resource size, limit number of bids
			/*for(Integer rno : resourceNumberToBidMap.keySet()) {
				List<Bid> bidsForRno = resourceNumberToBidMap.get(rno);
				if(bidsForRno.size() == 0) {
					continue;
				}
				int num_bids_to_consider = Math.max(bidsForRno.size()/rno, 1);
				Collections.sort(bidsForRno, new PerGPUBidComparator());
				finalBids.addAll(bidsForRno.subList(0, num_bids_to_consider));
			}*/
			
			List<Bid> winningBids = pickWinningBids(gpu_set, bids);//finalBids);
			if(winningBids.size() == 0) {
				System.out.println("Have a problem!");
				for(Bid bid : bids) {
					System.out.println("Bid: " + bid);
				}
				for (GPU gpu: gpu_set) {
					System.out.println("GPU: " + gpu.getLocation());
				}
			}
			for(Bid bid : winningBids) {
				bid.getJob().notifyResourceAssignment(bid.getGPUList());
				remainingGPUSet.removeAll(bid.getGPUList());
				//System.out.println(remainingGPUSet.size());
				for(GPU g: bid.getGPUList()) {
					g.assignGPU(Cluster.getInstance().getLeaseTime(), bid.getJob());
				}
				if(remainingGPUSet.size() == 0) {
					break;
				}
			}
			gpu_set = remainingGPUSet;
		}
		startWaitingJobs();
	}
	
	private boolean isGpuPresent(GPU gpu, List<GPU> gpuList) {
		for(GPU g: gpuList) {
			if(g.getLocation().compareTo(gpu.getLocation())) {
				return true;
			}
		}
		return false;
	}
	
	private class JobLossGradient {
		private IntraJobScheduler mJob;
		private double mGradient;
		
		public JobLossGradient(IntraJobScheduler job, double gradient) {
			mJob = job;
			mGradient = gradient;
		}
		
		public IntraJobScheduler getJob() {
			return mJob;
		}
		
		public double getLossGradient() {
			return mGradient;
		}
	}
	
	private class JobFairness {
		private IntraJobScheduler mJob;
		private Double mFairnessValue;
		
		public JobFairness(IntraJobScheduler job, double value) {
			mJob = job;
			mFairnessValue = value;
		}
		
		public IntraJobScheduler getJob() {
			return mJob;
		}
		
		public double getFairnessValue() {
			return mFairnessValue;
		}
	}
	
	private class JobFairnessComparator implements Comparator<JobFairness> {

		@Override
		public int compare(JobFairness j1, JobFairness j2) {
			int comp = Double.compare(j1.getFairnessValue(), j2.getFairnessValue());
			if(comp != 0) {
				return -1*comp;
			}
			return j1.getJob().getJobId() - j2.getJob().getJobId();
		}
		
	}
	
	private class LossGradientComparator implements Comparator<JobLossGradient> {

		@Override
		public int compare(JobLossGradient j1, JobLossGradient j2) {
			int comp = Double.compare(j1.getLossGradient(), j2.getLossGradient());
			if(comp != 0) {
				return -1*comp;
			}
			return j1.getJob().getJobId() - j2.getJob().getJobId();
		}
		
	}
}
