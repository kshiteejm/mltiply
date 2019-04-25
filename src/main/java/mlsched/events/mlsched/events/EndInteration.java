package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Statistics;

public class EndInteration extends Event {

	public EndInteration(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {

		Statistics statObj = Main.jobStats.get(j.jobId);
		statObj.iterEndTimes.add(Main.currentTime);
		double lossValue = j.lossFunction.getValue(j.currIterationNum);
		double nextLossValue = j.lossFunction.getValue(j.currIterationNum + 1);
		statObj.lossValues.add(lossValue);
		Main.jobStats.put(j.jobId, statObj);

		Main.cluster.availableGPUs += j.currIterAllocation;
		j.currIterAllocation = 0;
		
		// NOTE: epoch scheduling only change when we use SLAQ. We will never
		// enter the if condition for any other scheduler.
		if (Main.epochScheduling) {
			// Since epoch has changed, release all the resources
			// at the end of the iteration and wait for next epoch to trigger

			
			if (j.currIterationNum < j.numIterations && lossValue > 1E-4 && nextLossValue > 0) {
				j.jobState = Job.State.WAITING_FOR_RESOURCES;
				Main.eventQueue.add(new DistributeResources(Main.currentTime));
				// Main.interJobScheduler.distributeResources();
			} else {
				Main.eventQueue.add(new JobCompleted(Main.currentTime, j));
			}
		} else {
			// The epoch number has not changed
			if (j.currIterationNum < j.numIterations && lossValue > 1E-4 && nextLossValue > 0) {
				
				if (!Main.epochScheduling) {
					// In case of not SLAQ, put it to waiting and recompute LFS
					j.jobState = Job.State.WAITING_FOR_RESOURCES;
					Main.eventQueue.add(new ComputeLogicalFairShare(Main.currentTime));
				} else {
					// In case of SLAQ, no need to release resources, just start
					// next iteration with whatever you have now.
					j.intraJobScheduler.schedule(j);
				}
			} else {
				Main.eventQueue.add(new JobCompleted(Main.currentTime, j));
			}
		}
	}
	
	@Override
	public void printInfo() {
		System.out.println("Event = " + this.getClass().toString() + " Timestamp = " + this.timeStamp + " JobID = " + this.j.jobId + " Iteration = " + this.j.currIterationNum);
	}
}
