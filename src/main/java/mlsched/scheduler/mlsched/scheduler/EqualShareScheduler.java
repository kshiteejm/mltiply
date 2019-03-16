package mlsched.scheduler;

import java.util.ArrayList;
import java.util.Collections;

import mlsched.events.DistributeResources;
import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class EqualShareScheduler extends InterJobScheduler {

	
	public EqualShareScheduler(boolean schedulingEpoch) {
		super(schedulingEpoch);
	}

	@Override
	public void computeLogicalFairShare() {
		
		int totalLogicallyAllocatedRes = 0;
		
		for (Job j : Main.jobList) {
			j.logicalFairShare = Math.min(j.maxParallelism, (int) Main.cluster.numGPUs / Main.jobList.size());
			totalLogicallyAllocatedRes += j.logicalFairShare;
		}
		
		Collections.sort(Main.jobList, new JobComparator());
		
		int remainingRes = Main.cluster.numGPUs - totalLogicallyAllocatedRes;
		
		ArrayList<Job> sortedByMaxPar = new ArrayList<>();
		for (Job j : Main.jobList) {
			if (j.logicalFairShare < j.maxParallelism) {
					sortedByMaxPar.add(j);
			}
		}
		
		// Remaining resources are allocated in a round robin fashion
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

		// this.distributeResources();
		Main.eventQueue.add(new DistributeResources(Main.currentTime));
	}
}
