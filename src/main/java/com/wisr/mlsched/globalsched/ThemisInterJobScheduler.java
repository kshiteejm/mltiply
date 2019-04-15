package com.wisr.mlsched.globalsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

public class ThemisInterJobScheduler extends InterJobScheduler {

	@Override
	public void onResourceAvailable(List<GPU> gpu_set) {
		if(Cluster.getInstance().getRunningJobs().size() == 0) {
			// Means no jobs are running, hence nothing to do
			return;
		}
		gpu_set = gpu_set.stream().filter(g -> !g.isLeased()).collect(Collectors.toList());
		while(gpu_set.size() > 0) {
			List<JobFairness> fairnessValues = new ArrayList<JobFairness>();
			List<IntraJobScheduler> runningJobs = Cluster.getInstance().getRunningJobs();
			for(IntraJobScheduler job: runningJobs) {
				fairnessValues.add(new JobFairness(job, job.getCurrentEstimate()/job.getIdealEstimate()));
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
				bids = new ArrayList<Bid>();
				runningJobs = Cluster.getInstance().getRunningJobs();
				for(IntraJobScheduler job: runningJobs) {
					List<Bid> bidsFromJob = job.prepareBid(gpu_set);
					if(bidsFromJob != null && !bidsFromJob.isEmpty()) {
						bids.addAll(bidsFromJob);
					}
				}
				// If still no bids, all jobs are running at full capacity
				if(bids.size() == 0) {
					break;
				}
			}
			
			// Maximize fairness based on bids. For now, going for a greedy approach
			Collections.sort(bids, new PerGPUBidComparator());
			List<GPU> remainingGPUSet = new ArrayList<>(gpu_set);
			for(Bid bid: bids) {
				List<GPU> assignedGPUs = bid.getGPUList();
				// Check if all GPUs are available in remaining GPU set
				boolean allGpusPresent = true;
				for(GPU gpu : assignedGPUs) {
					if(!isGpuPresent(gpu, remainingGPUSet)) {
						allGpusPresent = false;
					}
				}
				if(!allGpusPresent) {
					// This bid cannot be honored
					continue;
				}
				Set<GPU> gpuSet = new HashSet<GPU>(bid.getJob().getGPUsAvailableForNextIteration());
				gpuSet.addAll(assignedGPUs);
				if(gpuSet.size() > bid.getJob().getMaxParallelism()) {
					// This is an illegal assignment.
					// It is possible that the job got resources from another bid
					continue;
				}
				bid.getJob().notifyResourceAssignment(assignedGPUs);
				remainingGPUSet.removeAll(assignedGPUs);
				for(GPU g: assignedGPUs) {
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
