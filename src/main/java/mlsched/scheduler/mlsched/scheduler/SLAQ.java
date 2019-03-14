package mlsched.scheduler;

import java.util.ArrayList;
import java.util.Collections;

import mlsched.events.ResourceAllocated;
import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class SLAQ extends InterJobScheduler {

	public SLAQ(boolean schedulingEpoch) {
		super(schedulingEpoch);
	}

	// TODO: For now copied EqualShareLogic. Change this.
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
		ArrayList<Job> sortedByMaxPar = new ArrayList<>();
		for (Job j : Main.jobList) {
			if (j.logicalFairShare < j.maxParallelism) {
				sortedByMaxPar.add(j);
			}
		}

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

//		for (Job j : Main.jobList) {
//			System.out.println("Job Id --- " + j.jobId + " ---- Logical Fair Share ==== " + j.logicalFairShare);
//		}

		// Some jobs are still running in their iteration, so don't distribute now. When all jobs go into waiting
		// the next schedule epoch event will distribute and start the jobs.
		for (Job j : Main.jobList) {
			if(j.jobState != Job.State.WAITING_FOR_RESOURCES) {
				System.out.println("Computed new LFS, but not distributing yet because jobs are running");
				return;
			}
		}

		this.distributeResources();
	}
}
