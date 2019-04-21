package com.wisr.mlsched.globalsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.wisr.mlsched.ClusterEventQueue;
import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.events.StartIterationEvent;
import com.wisr.mlsched.job.Bid;
import com.wisr.mlsched.localsched.IntraJobScheduler;
import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

/**
 * Interface for InterJobScheduler types
 */
public abstract class InterJobScheduler {
	
	protected Random mRand;
	public InterJobScheduler() {
		mRand = new Random(0); // seed to make it deterministic
	}
	public abstract void onResourceAvailable(List<GPU> gpu_set);
	
	protected void startWaitingJobs() {
		List<IntraJobScheduler> jobs = Cluster.getInstance().getRunningJobs();
		for(IntraJobScheduler job : jobs) {
			if(job.isWaitingForResources() && job.hasResourcesForNextIteration()) {
				ClusterEventQueue.getInstance().enqueueEvent(
						new StartIterationEvent(Simulation.getSimulationTime(), job));
				job.notifyResourceAvailable();
			} else if(job.isWaitingForResources()) {
				job.resetOldRatio();
			}
		}
	}
	
	protected void perGPUResourceAllocator(List<GPU> gpu_set) {
		if(Cluster.getInstance().getRunningJobs().size() == 0) {
			// Means no jobs are running, hence nothing to do
			return;
		}
		for(GPU gpu : gpu_set) {
			// Put this GPU up for auction by getting bids from jobs
			if(gpu.isLeased()) {
				// Already leased.
				continue;
			}
			List<Bid> bids = new ArrayList<Bid>();
			List<GPU> gpuList = new ArrayList<GPU>();
			gpuList.add(gpu);
			List<IntraJobScheduler> jobs = Cluster.getInstance().getRunningJobs();
			for(IntraJobScheduler job : jobs) {
				List<Bid> bidsFromJob = job.prepareBid(gpuList);
				if(bidsFromJob != null && !bidsFromJob.isEmpty()) {
					bids.addAll(bidsFromJob);
				}
			}
			if(bids.size() == 0) {
				// No bids. All jobs must be running at full capacity
				continue;
			}
			Collections.sort(bids, new PerGPUBidComparator());
			Bid winningBid = bids.get(0); // Select the winner
			winningBid.getJob().notifyResourceAssignment(gpuList);
			gpu.assignGPU(Cluster.getInstance().getLeaseTime(), winningBid.getJob());
		}
		startWaitingJobs();
	}
	
	protected class PerGPUBidComparator implements Comparator<Bid> {

		@Override
		public int compare(Bid bid1, Bid bid2) {
			int comp = Double.compare(bid1.getExpectedBenefit(), bid2.getExpectedBenefit());
			if(comp != 0) {
				return -1*comp;
			}
			if(bid1.getJob().getGPUsAvailableForNextIteration().size() !=
					bid2.getJob().getGPUsAvailableForNextIteration().size()) {
				return bid1.getJob().getGPUsAvailableForNextIteration().size() -
						bid2.getJob().getGPUsAvailableForNextIteration().size();
			}
			return bid1.getJob().getJobId() - bid2.getJob().getJobId();
		}
		
	}
}