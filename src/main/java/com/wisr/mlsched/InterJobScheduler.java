package com.wisr.mlsched;

import java.util.List;

/**
 * Interface for InterJobScheduler types
 */
public abstract class InterJobScheduler {
	public abstract void onResourceAvailable(List<GPU> gpu_set);
	
	protected void startWaitingJobs() {
		List<IntraJobScheduler> jobs = Cluster.getInstance().getRunningJobs();
		for(IntraJobScheduler job : jobs) {
			if(job.isWaitingForResources() && job.hasResourcesForNextIteration()) {
				ClusterEventQueue.getInstance().enqueueEvent(
						new StartIterationEvent(Simulation.getSimulationTime(), job));
			}
		}
	}
}