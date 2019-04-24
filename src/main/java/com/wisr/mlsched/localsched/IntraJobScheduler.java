package com.wisr.mlsched.localsched;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wisr.mlsched.ClusterEventQueue;
import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.config.ConfigUtils;
import com.wisr.mlsched.events.EndIterationEvent;
import com.wisr.mlsched.events.ResourceAvailableEvent;
import com.wisr.mlsched.events.StartIterationEvent;
import com.wisr.mlsched.job.Bid;
import com.wisr.mlsched.job.JobStatistics;
import com.wisr.mlsched.job.LossFunction;
import com.wisr.mlsched.job.LossFunctionFactory;
import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

import org.json.simple.JSONObject;

public abstract class IntraJobScheduler {
	// List of Job configurations
	private int mJobGroupId; // Unique identifier for job group to which this job belongs
	private int mJobId; // Unique identifier for this job
	protected double mJobStartTime; // Job start time
	protected int mTotalExpectedIterations; // Total number of iterations job is expected to run
	protected double mTimePerIteration; // Amount of time for a single iteration of job on 1 GPU
	protected int mMaxParallelism; // Represents max GPUs job can request
	private int mRandomSeed; // Random seed for loss curve
	private LossFunction mLossCurve; // Representation of loss curve
	private double mCrossSlotSlowdown; // Slowdown due to network b/w GPUs across slots
	private double mCrossMachineSlowdown; // Slowdown due to network b/w GPUs across machines
	private double mCrossRackSlowdown; // Slowdown due to network b/w GPUs across slots

	private final double CHECKPOINTING_OVERHEAD_PER_GPU = 0.1; // 6 seconds overhead
	
	// State management for job
	private boolean mIsLeader; // Whether this job is the leader in it's job group
	protected int mTotalIterationsRemaining; // Number of iterations of job remaining
	protected Set<GPU> mCurrentIterationGPUs; // GPUs for current iteration
	private Set<GPU> mNextIterationExpectedGPUs; // GPUs we expect from current iteration to be used for next iteration
	protected Set<GPU> mNextIterationGPUs; // GPUs allocated for next iteration
	private boolean mIsWaiting; // Represents if job is waiting for resources
	protected double oldRatio; // Old value of Ts/Ti
	protected double themisTs; // Themis Ts
	private double mTimeLastResourceAssignment; 
	private static Logger sLog; // Instance of logger
	private double mGpuTime;

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
		oldRatio = Double.POSITIVE_INFINITY;
		themisTs = Double.POSITIVE_INFINITY;
		mGpuTime = 0;
		mTimeLastResourceAssignment = Simulation.getSimulationTime()-1;
		mIsLeader = true; // By default, everyone is a leader unless told otherwise
		JobStatistics.getInstance().recordJobStart(mJobId, Simulation.getSimulationTime());
		List<GPU> availableResources = getResourcesAvailableInCluster();
		if (!availableResources.isEmpty()) {
			ClusterEventQueue.getInstance()
					.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime(), availableResources));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntraJobScheduler)) {
			return false;
		}
		IntraJobScheduler job = (IntraJobScheduler) o;
		return (mJobId == job.mJobId) && (mJobGroupId == job.mJobGroupId);
	}
	
	/**
	 * Set the role of job and number of iterations it needs to run.
	 * @param is_leader
	 * @param iterations
	 */
	public void setRole(boolean is_leader, int iterations) { 
		mIsLeader = is_leader;
		mTotalIterationsRemaining = iterations;
		mTotalExpectedIterations = iterations;
	}

	/**
	 * @return the mTotalIterationsRemaining
	 */
	public int getmTotalIterationsRemaining() {
		return mTotalIterationsRemaining;
	}

	/**
	 * @param mTotalIterationsRemaining the mTotalIterationsRemaining to set
	 */
	public void setmTotalIterationsRemaining(int mTotalIterationsRemaining) {
		this.mTotalIterationsRemaining = mTotalIterationsRemaining;
	}
	
	public double getCurrentEstimateForThemis() {
		// Do update if we do not have resources
		if(mCurrentIterationGPUs.size() == 0) {
			return (Simulation.getSimulationTime() - mJobStartTime) + mTotalIterationsRemaining*mTimePerIteration;
		} else {
			return getCurrentEstimate();
		}
	}

	/**
	 * @return the mTotalExpectedIterations
	 */
	public int getmTotalExpectedIterations() {
		return mTotalExpectedIterations;
	}

	/**
	 * @param mTotalExpectedIterations the mTotalExpectedIterations to set
	 */
	public void setmTotalExpectedIterations(int mTotalExpectedIterations) {
		this.mTotalExpectedIterations = mTotalExpectedIterations;
	}

	public void startIteration() {
		sLog.log(Level.INFO, "Starting iteration for job " + Integer.toString(mJobId));
		mCurrentIterationGPUs = new HashSet<GPU>(mNextIterationGPUs);
		mNextIterationGPUs = new HashSet<GPU>();
		mIsWaiting = false;
		assert(mCurrentIterationGPUs.size() > 0);
		System.out.println("Placement Job " + Integer.toString(mJobId) + ":" 
				+ " Time: " + Double.toString(Simulation.getSimulationTime())
				+ " Iteration: " + Integer.toString(mTotalExpectedIterations - mTotalIterationsRemaining)
				+ " NumGPUs: " + Integer.toString(mCurrentIterationGPUs.size())
				+ " Score: " + Double.toString(getPlacementSlowdown(mCurrentIterationGPUs))
				+ " Number_jobs_running: " + Integer.toString(Cluster.getInstance().getRunningJobs().size()));
		ClusterEventQueue.getInstance().enqueueEvent(
				new EndIterationEvent(Simulation.getSimulationTime() + mTimePerIteration / getJobSpeedup(), this));
		mGpuTime = mGpuTime + (mTimePerIteration/getJobSpeedup())*mCurrentIterationGPUs.size();
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
		setmTotalIterationsRemaining(getmTotalIterationsRemaining() - 1);
		sLog.info("Iterations Remaning: " + Integer.toString(getmTotalIterationsRemaining()));
		oldRatio = getCurrentEstimate()/getIdealEstimate();
		themisTs = getCurrentEstimate();
		if (getmTotalIterationsRemaining() == 0) {
			// Job is done
			System.out.println("Job " + Integer.toString(mJobId) + " done");
			sLog.info("Job " + Integer.toString(mJobId) + " done");
			List<GPU> relinquished_resources = relinquishAllResources();
			// Make all relinquished resources available
			ClusterEventQueue.getInstance()
					.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime()
							+ CHECKPOINTING_OVERHEAD_PER_GPU*relinquished_resources.size(), relinquished_resources));
			Cluster.getInstance().removeJob(this);
			JobStatistics.getInstance().recordJobEnd(mJobId, Simulation.getSimulationTime(), mJobStartTime,
					getIdealEstimate(), mIsLeader, mGpuTime);
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
			.enqueueEvent(new ResourceAvailableEvent(Simulation.getSimulationTime() +
					CHECKPOINTING_OVERHEAD_PER_GPU*mCurrentIterationGPUs.size(), expiredResources));
		}
		
		if (mNextIterationGPUs.isEmpty()) {
			mCurrentIterationGPUs = new HashSet<GPU>();
			mIsWaiting = true;
		} else {
			ClusterEventQueue.getInstance().enqueueEvent(new StartIterationEvent(Simulation.getSimulationTime(), this));
		}
	}
	
	public void resetOldRatio() {
		oldRatio = Double.POSITIVE_INFINITY;
	}

	public double getCurrentEstimate() {
		return (Simulation.getSimulationTime() - mJobStartTime)
				+ (getmTotalIterationsRemaining() * mTimePerIteration) / getJobSpeedup();
	}
	
	public double getEstimateAfterAllocation() {
		return (Simulation.getSimulationTime() - mJobStartTime)
				+ (getmTotalIterationsRemaining() * mTimePerIteration) / getJobSpeedup1();
	}

	public double getIdealEstimate() {
		return 1.0 * mTimePerIteration * getmTotalExpectedIterations() / mMaxParallelism;
	}

	public double getLossGradient() {
		return mLossCurve.getSlope(getmTotalExpectedIterations() - getmTotalIterationsRemaining());
	}
	
	public double getLoss(int iteration) {
		return mLossCurve.getValue(iteration);
	}

	public void notifyResourceAssignment(List<GPU> assignment) {
		sLog.info("Job " + Integer.toString(mJobId) + " got resources"); 
		mNextIterationGPUs.addAll(assignment);
		//themisTs = getEstimateAfterAllocation();
	}
	
	public void notifyResourceAvailable() {
		mIsWaiting = false;
		mTimeLastResourceAssignment = Simulation.getSimulationTime();
	}
	
	public double getJobSpeedup1() {
		return getGPUsAvailableForNextIteration().size()* getPlacementSlowdown(getGPUsAvailableForNextIteration());
	}

	public double getJobSpeedup() {
		return mCurrentIterationGPUs.size() * getPlacementSlowdown(mCurrentIterationGPUs);
	}
	
	public int getJobId() {
		return mJobId;
	}
	
	public boolean willParticipateInBid() {
		return true;
	}
	
	public int getJobGroupId() {
		return mJobGroupId;
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
	
	public Set<GPU> getGPUsAvailableForNextIteration() {
		Set<GPU> set = new HashSet<GPU>(mNextIterationGPUs);
		// Now add all GPUs to this set which will not expire after the iteration
		set.addAll(mNextIterationExpectedGPUs);
		return set;
	}
	
	/**
	 * Returns the maximum number of GPUs this job can take
	 * @return
	 */
	public int getMaxParallelism() {
		return mMaxParallelism;
	}
	
	public double getOldBenefit() {
		double oldSpeedup = getGPUsAvailableForNextIteration().size() * getPlacementSlowdown(getGPUsAvailableForNextIteration());
		double oldTs = (Simulation.getSimulationTime() - mJobStartTime) + 
				(mTotalIterationsRemaining*mTimePerIteration)/oldSpeedup;
		double oldratio = oldTs/getIdealEstimate();
		return oldratio;
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
		mJobGroupId = Integer.parseInt(ConfigUtils.getAttributeValue(config, "job_group_id"));
		mJobId = Integer.parseInt(ConfigUtils.getAttributeValue(config, "job_id"));
		setmTotalExpectedIterations(Integer.parseInt(ConfigUtils.getAttributeValue(config, "total_iterations")));
		mTimePerIteration = Double.parseDouble(ConfigUtils.getAttributeValue(config, "time_per_iteration"));
		mMaxParallelism = Integer.parseInt(ConfigUtils.getAttributeValue(config, "max_parallelism"));
		mRandomSeed = Integer.parseInt(ConfigUtils.getAttributeValue(config, "random_seed"));
		mCrossSlotSlowdown = Double.parseDouble(ConfigUtils.getAttributeValue(config, "cross_slot_slowdown"));
		mCrossMachineSlowdown = Double.parseDouble(ConfigUtils.getAttributeValue(config, "cross_machine_slowdown"));
		mCrossRackSlowdown = Double.parseDouble(ConfigUtils.getAttributeValue(config, "cross_rack_slowdown"));
		mLossCurve = LossFunctionFactory.createInstance(ConfigUtils.getAttributeValue(
				config, "loss_function_type"), getmTotalExpectedIterations(), mRandomSeed);
		setmTotalIterationsRemaining(getmTotalExpectedIterations());
		//mCrossSlotSlowdown = 1.0;
		//mCrossMachineSlowdown = random(0.6, 0.98);
		//mCrossRackSlowdown = mCrossMachineSlowdown/0.7;
	}
	
	private double random( double min, double max ) {
	  double diff = max - min;
	  return min + Math.random( ) * diff;
	}
}
