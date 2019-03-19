package com.wisr.mlsched;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONObject;

public abstract class IntraJobScheduler {
	// List of Job configurations
	private int mJobId; // Unique identifier for this job
	private double mJobStartTime; // Job start time
	private int mTotalExpectedIterations; // Total number of iterations job is expected to run
	private double mTimePerIteration; // Amount of time for a single iteration of job on 1 GPU
	private int mMaxParallelism; // Represents max GPUs job can request
	private int mRandomSeed; // Random seed for loss curve
	private LossFunction mLossCurve; // Representation of loss curve
	private double mCrossSlotSlowdown; // Slowdown due to network b/w GPUs across slots
	private double mCrossMachineSlowdown; // Slowdown due to network b/w GPUs across machines
	private double mCrossRackSlowdown; // Slowdown due to network b/w GPUs across slots

	// State management for job
	private int mTotalIterationsRemaining; // Number of iterations of job remaining
	private Set<GPU> mCurrentIterationGPUs; // GPUs for current iteration
	private Set<GPU> mNextIterationGPUs; // GPUs allocated for next iteration
	private boolean mIsWaiting; // Represents if job is waiting for resources

	public IntraJobScheduler(JSONObject config) {
		initFromConfig(config);
		mJobStartTime = Simulation.getSimulationTime();
		mCurrentIterationGPUs = new HashSet<GPU>();
		mNextIterationGPUs = new HashSet<GPU>();
		mIsWaiting = true;
		List<GPU> availableResources = getResourcesAvailableInCluster();
		if (!availableResources.isEmpty()) {
			ClusterEventQueue.getInstance()
					.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), availableResources));
		}
	}

	public void startIteration() {
		mCurrentIterationGPUs = new HashSet<GPU>(mNextIterationGPUs);
		mNextIterationGPUs = new HashSet<GPU>();
		mIsWaiting = false;
		// TODO: Sanity check if we have GPUs assigned before proceeding
		ClusterEventQueue.getInstance().enqueueEvent(
				new EndIterationEvent(Simulation.getSimulationTime() + mTimePerIteration / getJobSpeedup(), this));
	}

	public void endIteration() {
		mTotalIterationsRemaining--;
		if (mTotalIterationsRemaining == 0) {
			// Job is done
			List<GPU> relinquished_resources = relinquishAllResources();
			// Make all relinquished resources available
			ClusterEventQueue.getInstance()
					.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), relinquished_resources));
			Cluster.getInstance().removeJob(this);
			// TODO: Record job statistics on job end
			return;
		}
		// Job has iterations left
		List<GPU> expiredResources = new ArrayList<GPU>();
		Iterator<GPU> currentGPUIterator = mCurrentIterationGPUs.iterator();
		while (currentGPUIterator.hasNext()) {
			GPU gpu = currentGPUIterator.next();
			if (gpu.hasLeaseExpired()) {
				expiredResources.add(gpu);
			} else {
				mNextIterationGPUs.add(gpu);
			}
		}
		ClusterEventQueue.getInstance()
				.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), expiredResources));
		if (mNextIterationGPUs.isEmpty()) {
			mIsWaiting = true;
		} else {
			ClusterEventQueue.getInstance().enqueueEvent(new StartIterationEvent(Simulation.getSimulationTime(), this));
		}
	}

	public double getCurrentEstimate() {
		return (Simulation.getSimulationTime() - mJobStartTime)
				+ (mTotalIterationsRemaining * mTimePerIteration) / getJobSpeedup();
	}

	public double getIdealEstimate() {
		return 1.0 * mTimePerIteration * mTotalExpectedIterations / Cluster.getInstance().getGPUsInCluster().size();
	}

	public double getLossGradient() {
		return mLossCurve.getSlope(mTotalExpectedIterations - mTotalIterationsRemaining);
	}

	public void notifyResourceAssignment(List<GPU> assignment) {
		mNextIterationGPUs.addAll(assignment);
	}

	public double getJobSpeedup() {
		return mCurrentIterationGPUs.size() * getPlacementSlowdown();
	}

	public double getPlacementSlowdown() {
		HashSet<Integer> map = new HashSet<Integer>();
		Iterator<GPU> gpuIter = mCurrentIterationGPUs.iterator();
		// Check if across racks
		while (gpuIter.hasNext()) {
			GPU gpu = gpuIter.next();
			map.add(gpu.getLocation().getRackId());
			if (map.size() > 1) {
				return mCrossRackSlowdown;
			}
		}
		// Check if across machines
		map = new HashSet<Integer>();
		gpuIter = mCurrentIterationGPUs.iterator();
		while (gpuIter.hasNext()) {
			GPU gpu = gpuIter.next();
			map.add(gpu.getLocation().getMachineId());
			if (map.size() > 1) {
				return mCrossMachineSlowdown;
			}
		}
		// Check if across slots
		map = new HashSet<Integer>();
		gpuIter = mCurrentIterationGPUs.iterator();
		while (gpuIter.hasNext()) {
			GPU gpu = gpuIter.next();
			map.add(gpu.getLocation().getSlotId());
			if (map.size() > 1) {
				return mCrossSlotSlowdown;
			}
		}
		return 1.0;
	}

	public abstract void prepareBid(List<GPU> offeredGPUs);

	private List<GPU> relinquishAllResources() {
		List<GPU> gpus = Cluster.getInstance().getGPUsInCluster();
		List<GPU> releasedResources = new ArrayList<GPU>();
		Iterator<GPU> gpuIterator = gpus.iterator();
		while (gpuIterator.hasNext()) {
			GPU gpu = gpuIterator.next();
			if (gpu.getJob().equals(this)) {
				releasedResources.add(gpu);
				gpu.markLeaseEnd();
			}
		}
		return releasedResources;
	}

	private List<GPU> getResourcesAvailableInCluster() {
		List<GPU> gpus = Cluster.getInstance().getGPUsInCluster();
		List<GPU> availableResources = new ArrayList<GPU>();
		Iterator<GPU> gpuIterator = gpus.iterator();
		while (gpuIterator.hasNext()) {
			GPU gpu = gpuIterator.next();
			if (!gpu.isLeased()) {
				availableResources.add(gpu);
			}
		}
		return availableResources;
	}

	private void initFromConfig(JSONObject config) {
		mJobId = Integer.parseInt(ConfigUtils.getAttributeValue(config, "job_id"));
		mTotalExpectedIterations = Integer.parseInt(ConfigUtils.getAttributeValue(config, "total_iterations"));
		mTimePerIteration = Double.parseDouble(ConfigUtils.getAttributeValue(config, "time_per_iteration"));
		mMaxParallelism = Integer.parseInt(ConfigUtils.getAttributeValue(config, "max_parallelism"));
		mRandomSeed = Integer.parseInt(ConfigUtils.getAttributeValue(config, "random_seed"));
		mCrossSlotSlowdown = Double.parseDouble(ConfigUtils.getAttributeValue(config, "cross_slot_slowdown"));
		mCrossMachineSlowdown = Double.parseDouble(ConfigUtils.getAttributeValue(config, "cross_machine_slowdown"));
		mCrossRackSlowdown = Double.parseDouble(ConfigUtils.getAttributeValue(config, "cross_rack_slowdown"));
		mLossCurve = LossFunctionFactory.createInstance(ConfigUtils.getAttributeValue(
				config, "loss_function_type"), mTotalExpectedIterations, mRandomSeed);
	}
}
