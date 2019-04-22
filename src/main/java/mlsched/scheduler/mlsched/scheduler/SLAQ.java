package mlsched.scheduler;

import java.util.ArrayList;
import java.util.Collections;

import mlsched.events.DistributeResources;
import mlsched.simulator.Main;
import mlsched.workload.Job;

public class SLAQ extends InterJobScheduler {

	public SLAQ(boolean schedulingEpoch) {
		super(schedulingEpoch);
	}

	public double predictLossResource(Job j, int numResources) {
		double iterationDuration = j.serialIterationDuration / numResources; 
		int numIterationsEpoch = (int) Math.floor(Main.schedulingInterval / iterationDuration);
		
		double lossValInitial = j.lossFunction.getValue(j.currIterationNum);
		double lossValFinal = j.lossFunction.getValue(j.currIterationNum + numIterationsEpoch);
		
		assert(lossValFinal <= lossValInitial);
		return lossValFinal - lossValInitial;
	}
	
	public double predictLossReduction(Job j) {
		double predLoss = predictLossResource(j, j.logicalFairShare);
		double predLossP1 = predictLossResource(j, j.logicalFairShare+1);
		
		assert(predLoss >= predLossP1);
		return predLoss - predLossP1;
	}

	@Override
	public void computeLogicalFairShare() {

		int totalResources = Main.cluster.numGPUs;

		if(Main.jobList.isEmpty()) {
			return;
		}
		
		for (Job j : Main.jobList) {
			j.logicalFairShare = 0;
		}

		// TODO: Find out which type of job has better loss reduction.
		ArrayList<Job> priorQ = new ArrayList<>();
		for (Job j : Main.jobList) {
			assert (totalResources != 0);
			j.logicalFairShare += 1;
			totalResources -= 1;
			j.predLossRed = predictLossReduction(j);
			priorQ.add(j);
		}

		Collections.sort(priorQ, new SLAQJobComparator());
		while (totalResources > 0) {
			Job j = priorQ.get(0);
			j.logicalFairShare += 1;
			totalResources -= 1;
			j.predLossRed = predictLossReduction(j);
			Collections.sort(priorQ, new SLAQJobComparator());
		}

		// if (Main.distributeResourcesFlag == false)
			Main.eventQueue.add(new DistributeResources(Main.currentTime));
	}
}
