package com.wisr.mlsched.globalsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		List<JobFairness> fairnessValues = new ArrayList<JobFairness>();
		List<IntraJobScheduler> runningJobs = Cluster.getInstance().getRunningJobs();
		for(IntraJobScheduler job: runningJobs) {
			fairnessValues.add(new JobFairness(job, job.getCurrentEstimate()/job.getIdealEstimate()));
		}
		Collections.sort(fairnessValues, new JobFairnessComparator());
		JobFairness mostUnfairJob = fairnessValues.get(fairnessValues.size()-1);
		double d = Cluster.getInstance().getFairnessThreshold();
		if(mostUnfairJob.getFairnessValue() >= 1.0 + d) {
			// Consider only those jobs which have their fairness above threshold
			fairnessValues = fairnessValues.stream()
				    .filter(j -> j.getFairnessValue() > 1.0 + d).collect(Collectors.toList());
		}
		// For the selected jobs, prioritize them based on the gradient of loss function
		List<JobLossGradient> lossGradients = new ArrayList<JobLossGradient>();
		for(JobFairness j: fairnessValues) {
			lossGradients.add(new JobLossGradient(j.getJob(), j.getJob().getLossGradient()));
		}
		// Sort in descending order based on loss gradient
		Collections.sort(lossGradients, new LossGradientComparator());
		// Now choose epsilon fraction of this
		double e = Cluster.getInstance().getEpsilon();
		lossGradients = lossGradients.subList(0, (int) (e*lossGradients.size()));
		
		// Now offer bids to jobs remaining in lossGradients
		gpu_set = gpu_set.stream().filter(g -> !g.isLeased()).collect(Collectors.toList());
		List<Bid> bids = new ArrayList<Bid>();
		for(JobLossGradient job: lossGradients) {
			List<Bid> bidsFromJob = job.getJob().prepareBid(gpu_set);
			if(bidsFromJob != null && !bidsFromJob.isEmpty()) {
				bids.addAll(bidsFromJob);
			}
		}
		if(bids.size() == 0) {
			// No bids. All jobs must be running at full capacity
			return;
		}
		// Maxmimize fairness based on bids. For now, going for a greedy approach
		Collections.sort(bids, new ThemisBidComparator());
		List<GPU> remainingGPUSet = new ArrayList<>(gpu_set);
		for(Bid bid: bids) {
			List<GPU> assignedGPUs = bid.getGPUList();
			//System.out.println("Arjun: " + assignedGPUs.toString());
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
			if(j1.getFairnessValue() < j2.getFairnessValue()) {
				return -1;
			} else if(j1.getFairnessValue() > j2.getFairnessValue()){
				return 1;
			}
			if(mRand.nextBoolean()) {
				return -1;
			} else {
				return 1;
			}
		}
		
	}
	
	private class LossGradientComparator implements Comparator<JobLossGradient> {

		@Override
		public int compare(JobLossGradient j1, JobLossGradient j2) {
			if(j1.getLossGradient() < j2.getLossGradient()) {
				return 1;
			} else if(j1.getLossGradient() > j2.getLossGradient()) {
				return -1;
			}
			if(mRand.nextBoolean()) {
				return -1;
			} else {
				return 1;
			}
		}
		
	}
	
	private class ThemisBidComparator implements Comparator<Bid> {

		@Override
		public int compare(Bid bid1, Bid bid2) {
			if(bid1.getExpectedBenefit() > bid2.getExpectedBenefit()) {
				return -1;
			} else if(bid1.getExpectedBenefit() < bid2.getExpectedBenefit()){
				return 1;
			}
			// Else the bids are exact same. We need to break it randomly
			if(mRand.nextBoolean()) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}