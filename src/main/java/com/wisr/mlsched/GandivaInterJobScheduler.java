package com.wisr.mlsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GandivaInterJobScheduler extends InterJobScheduler {

	@Override
	public void onResourceAvailable(List<GPU> gpu_set) {
		// TODO: Implement
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
			Collections.sort(bids, new GandivaBidComparator());
			Bid winningBid = bids.get(0); // Select the winner
			winningBid.getJob().notifyResourceAssignment(gpuList);
			gpu.assignGPU(Cluster.getInstance().getLeaseTime(), winningBid.getJob());
		}
		startWaitingJobs();
	}
	
	private class GandivaBidComparator implements Comparator<Bid> {

		@Override
		public int compare(Bid bid1, Bid bid2) {
			if(bid1.getExpectedBenefit() > bid2.getExpectedBenefit()) {
				return -1;
			} else {
				return 1;
			}
		}
		
	}
}