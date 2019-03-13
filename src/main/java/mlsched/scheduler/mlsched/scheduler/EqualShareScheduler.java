package mlsched.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class EqualShareScheduler extends InterJobScheduler {

	@Override
	public void computeLogicalFairShare() {
		
//		Collections.sort(Main.jobList, new JobComparator());
		
		int totalLogicallyAllocatedRes = 0;
		
		for (Job j : Main.jobList) {
			j.logicalFairShare = Math.min(j.maxParallelism, (int) Main.cluster.numGPUs / Main.jobList.size());
			totalLogicallyAllocatedRes += j.logicalFairShare;
//			j.nextIterAllocation = j.logicalFairShare;
		}
		
		// for the same reason as above i don't feel the need here
		
		Collections.sort(Main.jobList, new JobComparator());
		 // those that are farthest from their fair share should get max resources
		// this is currently being done randomly (last time's distance from fair share)
		
		int remainingRes = Main.cluster.numGPUs - totalLogicallyAllocatedRes;
		
		// Remaining resources are allocated in a round robin fashion
		// TODO: Make sure no infinite loop :P
		
		ArrayList<Job> sortedByMaxPar = new ArrayList<>();
		for (Job j : Main.jobList) {
			if (j.logicalFairShare < j.maxParallelism) {
					sortedByMaxPar.add(j);
			}
		}
//		int numTicks = 0;
//		int i = 0;
////		while ((remainingRes > 0) && (numTicks < 2 * Main.jobList.size())) {
//			if (Main.jobList.get(i).logicalFairShare < Main.jobList.get(i).maxParallelism) {
//				numTicks = 0;
//				remainingzRes -= 1;
//				Main.jobList.get(i).logicalFairShare += 1;
//				Collections.sort(Main.jobList, new JobComparator());
////				i=0;
//			} else {
//				numTicks += 1;
//			}
////			i = (i + 1) % Main.jobList.size();
//		}
		
		int index = 0;
		while(remainingRes > 0 && !sortedByMaxPar.isEmpty()) {
			Job rrJob = sortedByMaxPar.get(index);
			rrJob.logicalFairShare += 1;
			if (rrJob.logicalFairShare >= rrJob.maxParallelism) {
				sortedByMaxPar.remove(rrJob);
			}
			remainingRes--;
			index = (index + 1) % sortedByMaxPar.size();
		}
		
		for (Job j : Main.jobList) {
			System.out.println("Job Id --- " + j.jobId + " ---- Logical Fair Share ==== " + j.logicalFairShare);
		}

		this.distributeResources();
	}
}
