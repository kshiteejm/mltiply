package com.wisr.mlsched;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

public abstract class IntraJobScheduler {
	// List of Job configurations
	private int mJobId; // Unique identifier for this job
	private double mJobStartTime; // Job start time
	protected int mTotalExpectedIterations; // Total number of iterations job is expected to run
	private double mTimePerIteration; // Amount of time for a single iteration of job on 1 GPU
	protected int mMaxParallelism; // Represents max GPUs job can request
	private int mRandomSeed; // Random seed for loss curve
	private LossFunction mLossCurve; // Representation of loss curve
	private double mCrossSlotSlowdown; // Slowdown due to network b/w GPUs across slots
	private double mCrossMachineSlowdown; // Slowdown due to network b/w GPUs across machines
	private double mCrossRackSlowdown; // Slowdown due to network b/w GPUs across slots

	// State management for job
	protected int mTotalIterationsRemaining; // Number of iterations of job remaining
	protected Set<GPU> mCurrentIterationGPUs; // GPUs for current iteration
	private Set<GPU> mNextIterationExpectedGPUs; // GPUs we expect from current iteration to be used for next iteration
	protected Set<GPU> mNextIterationGPUs; // GPUs allocated for next iteration
	private boolean mIsWaiting; // Represents if job is waiting for resources
	private static Logger sLog; // Instance of logger

	public IntraJobScheduler(JSONObject config) {
		initFromConfig(config);
		mJobStartTime = Simulation.getSimulationTime();
		sLog = Logger.getLogger(Cluster.class.getSimpleName());
		sLog.setLevel(Simulation.getLogLevel());
		sLog.info("Starting job " + Integer.toString(mJobId));
		mCurrentIterationGPUs = new HashSet<GPU>();
		mNextIterationExpectedGPUs = new HashSet<GPU>();
		mNextIterationGPUs = new HashSet<GPU>();
		mIsWaiting = true;
		JobStatistics.getInstance().recordJobStart(mJobId, Simulation.getSimulationTime());
		List<GPU> availableResources = getResourcesAvailableInCluster();
		if (!availableResources.isEmpty()) {
			ClusterEventQueue.getInstance()
					.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), availableResources));
		}
	}

	public void startIteration() {
		sLog.log(Level.INFO, "Starting iteration for job " + Integer.toString(mJobId));
		mCurrentIterationGPUs = new HashSet<GPU>(mNextIterationGPUs);
		mNextIterationGPUs = new HashSet<GPU>();
		mIsWaiting = false;
		// TODO: Sanity check if we have GPUs assigned before proceeding
		ClusterEventQueue.getInstance().enqueueEvent(
				new EndIterationEvent(Simulation.getSimulationTime() + mTimePerIteration / getJobSpeedup(), this));
		Iterator<GPU> gpuIter = mCurrentIterationGPUs.iterator();
		mNextIterationExpectedGPUs = new HashSet<GPU>();
		while(gpuIter.hasNext()) {
			GPU gpu = gpuIter.next();
			if(gpu.getLeaseEnd() > Simulation.getSimulationTime() + mTimePerIteration / getJobSpeedup()) {
				mNextIterationExpectedGPUs.add(gpu);
			}
		}
	}

	public void endIteration() {
		sLog.log(Level.ALL, "End iteration for job " + Integer.toString(mJobId));
		mTotalIterationsRemaining--;
		sLog.info("Iterations Remaning: " + Integer.toString(mTotalIterationsRemaining));
		if (mTotalIterationsRemaining == 0) {
			// Job is done
			sLog.info("Job " + Integer.toString(mJobId) + " done");
			List<GPU> relinquished_resources = relinquishAllResources();
			// Make all relinquished resources available
			ClusterEventQueue.getInstance()
					.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), relinquished_resources));
			Cluster.getInstance().removeJob(this);
			JobStatistics.getInstance().recordJobEnd(mJobId, Simulation.getSimulationTime());
			return;
		}
		// Job has iterations left
		List<GPU> expiredResources = new ArrayList<GPU>();
		Iterator<GPU> currentGPUIterator = mCurrentIterationGPUs.iterator();
		while (currentGPUIterator.hasNext()) {
			GPU gpu = currentGPUIterator.next();
			if (gpu.hasLeaseExpired()) {
				expiredResources.add(gpu);
				gpu.markLeaseEnd();
			} else {
				mNextIterationGPUs.add(gpu);
			}
		}
		if(!expiredResources.isEmpty()) {
			ClusterEventQueue.getInstance()
			.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), expiredResources));
		}
		
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
	
	public double getLoss(int iteration) {
		return mLossCurve.getValue(iteration);
	}

	public void notifyResourceAssignment(List<GPU> assignment) {
		sLog.info("Job " + Integer.toString(mJobId) + " got resources"); 
		mNextIterationGPUs.addAll(assignment);
	}
	
	public void notifyResourceAvailable() {
		mIsWaiting = false;
	}

	public double getJobSpeedup() {
		return mCurrentIterationGPUs.size() * getPlacementSlowdown(mCurrentIterationGPUs);
	}
	
	public int getJobId() {
		return mJobId;
	}

	public double getPlacementSlowdown(Set<GPU> gpus) {
		HashSet<Integer> map = new HashSet<Integer>();
		Iterator<GPU> gpuIter = gpus.iterator();
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
		gpuIter = gpus.iterator();
		while (gpuIter.hasNext()) {
			GPU gpu = gpuIter.next();
			map.add(gpu.getLocation().getMachineId());
			if (map.size() > 1) {
				return mCrossMachineSlowdown;
			}
		}
		// Check if across slots
		map = new HashSet<Integer>();
		gpuIter = gpus.iterator();
		while (gpuIter.hasNext()) {
			GPU gpu = gpuIter.next();
			map.add(gpu.getLocation().getSlotId());
			if (map.size() > 1) {
				return mCrossSlotSlowdown;
			}
		}
		return 1.0;
	}
	
	public boolean isWaitingForResources() {
		return mIsWaiting;
	}
	
	public boolean hasResourcesForNextIteration() {
		return mNextIterationGPUs.size() > 0;
	}
	
	protected Set<GPU> getGPUsAvailableForNextIteration() {
		Set<GPU> set = new HashSet<GPU>(mNextIterationGPUs);
		// Now add all GPUs to this set which will not expire after the iteration
		set.addAll(mNextIterationExpectedGPUs);
		return set;
	}

	public abstract List<Bid> prepareBid(List<GPU> offeredGPUs);

	private List<GPU> relinquishAllResources() {
		List<GPU> gpus = Cluster.getInstance().getGPUsInCluster();
		List<GPU> releasedResources = new ArrayList<GPU>();
		Iterator<GPU> gpuIterator = gpus.iterator();
		while (gpuIterator.hasNext()) {
			GPU gpu = gpuIterator.next();
			if (gpu.getJob() != null && gpu.getJob().equals(this)) {
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
		mTotalIterationsRemaining = mTotalExpectedIterations;
	}
}
