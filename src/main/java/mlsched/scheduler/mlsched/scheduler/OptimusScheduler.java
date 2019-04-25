package mlsched.scheduler;

import java.util.ArrayList;
import java.util.Collections;

import mlsched.events.DistributeResources;
import mlsched.simulator.Main;
import mlsched.workload.Job;

public class OptimusScheduler extends InterJobScheduler {

	public OptimusScheduler(boolean schedulingEpoch) {
		super(schedulingEpoch);
	}
	
	public double getTimeToCompletion(Job j, int numParameterServers, int numWorkers) {
		int numStepsRemaining = j.numIterations - j.currIterationNum;
		double jobConvergenceSpeed = j.trainingSpeedFunction.getValue(numParameterServers, numWorkers);
		double timeToCompletion = numStepsRemaining / jobConvergenceSpeed;
		return timeToCompletion;
	}
	
	//TODO: workerResourceRequirement should be replaced by the requirement of Dominant Resource if multiple
	// resource types are introduced.
	public double getMarginalGainByAddingWorker(Job j) {
		return (getTimeToCompletion(j, j.numWorkersShare, j.numParameterServersShare)
				- getTimeToCompletion(j, j.numWorkersShare + 1, j.numParameterServersShare)) / j.workerResourceRequirement;
	}
	
	//TODO: resource requirement to be replaced by the requirement of the Dominant Resource if multiple
	// resource types are introduced.
	public double getMarginalGainByAddingPS(Job j) {
		return (getTimeToCompletion(j, j.numWorkersShare, j.numParameterServersShare)
				- getTimeToCompletion(j, j.numWorkersShare, j.numParameterServersShare + 1)) / j.parameterServerResourceRequirement;
	}
	
	public double getMarginalGain(Job j) {
		return Math.max(getMarginalGainByAddingWorker(j), getMarginalGainByAddingPS(j));
	}
	
	@Override
	public void computeLogicalFairShare() {

		int totalResources = Main.cluster.numGPUs;

		if(Main.jobList.isEmpty()) {
			return;
		}
		
		for (Job j : Main.jobList) {
			j.logicalFairShare = 0;
			j.numWorkersShare = 0;
			j.numParameterServersShare = 0;
		}

		ArrayList<Job> priorQ = new ArrayList<>();
		for (Job j : Main.jobList) {
			if (totalResources >= j.workerResourceRequirement) {
				j.numWorkersShare = 1;
				j.logicalFairShare += j.workerResourceRequirement;
				totalResources -= j.workerResourceRequirement;
			}
			
			if (totalResources >= j.parameterServerResourceRequirement) {
				j.numParameterServersShare = 1;
				j.logicalFairShare += j.parameterServerResourceRequirement;
				totalResources -= j.parameterServerResourceRequirement;
			}
			
			j.marginalGain = getMarginalGain(j);
			priorQ.add(j);
//			if (j.marginalGain > 0) { // if marginalGain < 0, no need to allocate more resources
//				priorQ.add(j);
//			}
		}

		Collections.sort(priorQ, new OptimusJobComparator());
		while (!priorQ.isEmpty() && totalResources > 0) {
			Job j = priorQ.get(0);
			
			double marginalGain = getMarginalGain(j);
			
//			if (marginalGain < 0) {
//				priorQ.remove(j);
//			} else {
				double gainByWorkerAddition = getMarginalGainByAddingWorker(j);
				double gainByPSAddition = getMarginalGainByAddingPS(j);
				
				// slight modifications from the suggested algorithm
				if (gainByWorkerAddition > gainByPSAddition) {
					if (totalResources >= j.workerResourceRequirement) {
						j.numWorkersShare += 1;
						j.logicalFairShare += j.workerResourceRequirement;
						totalResources -= j.workerResourceRequirement;
						j.marginalGain = getMarginalGain(j);
					} else if (gainByPSAddition > 0 && j.parameterServerResourceRequirement <= totalResources) {
						j.numParameterServersShare += 1;
						j.logicalFairShare += j.parameterServerResourceRequirement;
						totalResources -= j.parameterServerResourceRequirement;
						j.marginalGain = gainByPSAddition;
					} else {
						priorQ.remove(j);
					}
				} else {
					if (totalResources >= j.parameterServerResourceRequirement) {
						j.numParameterServersShare += 1;
						j.logicalFairShare += j.parameterServerResourceRequirement;
						totalResources -= j.parameterServerResourceRequirement;
						j.marginalGain = getMarginalGain(j);
					} else if (gainByWorkerAddition > 0 && j.workerResourceRequirement <= totalResources) {
						j.numWorkersShare += 1;
						j.logicalFairShare += j.workerResourceRequirement;
						totalResources -= j.workerResourceRequirement;
						j.marginalGain = gainByWorkerAddition;
					} else {
						priorQ.remove(j);
					}
				}
				Collections.sort(priorQ, new OptimusJobComparator());
			//}
		}

//		if (Main.distributeResourcesFlag == false)
		Main.eventQueue.add(new DistributeResources(Main.currentTime));
		
		
		//TODO: this algorithm also takes into account the placement that helps lower the communication cost.
		// However, we don't have that metric in other algorithms for an effective comparison
		
		//TODO: What happens when enough resources are not available?
		
		//TODO: Task Placement
		
	}
}
