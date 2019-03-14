package mlsched.scheduler;

import java.util.PriorityQueue;

import mlsched.events.DistributeResources;
import mlsched.simulator.Main;
import mlsched.workload.Job;

public class SLAQ extends InterJobScheduler {

	public SLAQ(boolean schedulingEpoch) {
		super(schedulingEpoch);
	}

	public double predictLossReduction(Job j) {
		
		double iterationDuration = j.serialIterationDuration / j.logicalFairShare; 
		int numIterationsEpoch = (int) Math.floor(Main.schedulingInterval / iterationDuration);
		
		double lossValInitial = j.lossFunction.getValue(j.currIterationNum);
		double lossValFinal = j.lossFunction.getValue(j.currIterationNum + numIterationsEpoch);
		
		assert(lossValFinal <= lossValInitial); // Because KC said so.
		
		return Math.abs(lossValFinal - lossValInitial);
	}

	// TODO: For now copied EqualShareLogic. Change this.
	@Override
	public void computeLogicalFairShare() {

		int totalResources = Main.cluster.numGPUs;

		if(Main.jobList.isEmpty()) {
			return;
		}
		
		for (Job j : Main.jobList) {
			j.logicalFairShare = 0;
		}

		PriorityQueue<Job> priorQ = new PriorityQueue<>(new SLAQJobComparator());
		for (Job j : Main.jobList) {
			assert (totalResources != 0);
			j.logicalFairShare += 1;
			totalResources -= 1;
			j.predLossRed = predictLossReduction(j);
			priorQ.add(j);
		}

		while (totalResources > 0) {
			Job j = priorQ.poll();
			j.logicalFairShare += 1;
			totalResources -= 1;
			j.predLossRed = predictLossReduction(j);
			priorQ.add(j);
		}

		if (Main.distributeResourcesFlag == false)
			// this.distributeResources();
			Main.eventQueue.add(new DistributeResources(Main.currentTime));
	}
}
