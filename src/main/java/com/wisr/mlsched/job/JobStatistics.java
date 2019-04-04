package com.wisr.mlsched.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wisr.mlsched.ClusterEventQueue;
import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.events.JobStatisticEvent;
import com.wisr.mlsched.localsched.IntraJobScheduler;
import com.wisr.mlsched.localsched.JobGroupManager;
import com.wisr.mlsched.resources.Cluster;

public class JobStatistics {
	
	private static JobStatistics sInstance = null; // Holder of singleton
	private HashMap<Integer, SingleJobStat> mJobTime; // Job start and end time per job
	private List<FairnessIndex> mFairnessIndices; // List of fairness indices measured over time
	private List<LossValue> mLossValues; // List of cumulative loss values measured over time
	
	/**
	 * Private constructor to enforce singleton
	 */
	private JobStatistics() {
		mJobTime = new HashMap<Integer, SingleJobStat>();
		mFairnessIndices = new ArrayList<FairnessIndex>();
		mLossValues = new ArrayList<LossValue>();
		ClusterEventQueue.getInstance().enqueueEvent(new 
				JobStatisticEvent(Simulation.getSimulationTime() + 1));
	}
	
	/**
	 * Get the instance of JobStatistics.
	 */
	public static JobStatistics getInstance() {
		if(sInstance == null) {
			sInstance = new JobStatistics();
		}
		return sInstance;
	}
	
	/**
	 * Record the start of job
	 * @param jobid
	 * @param timestamp
	 */
	public void recordJobStart(int jobid, double timestamp) {
		mJobTime.put(jobid, new SingleJobStat(timestamp));
	}
	
	/**
	 * Record the end of job
	 * @param jobid
	 * @param timestamp
	 */
	public void recordJobEnd(int jobid, double timestamp) {
		mJobTime.get(jobid).setEndTime(timestamp);
	}
	
	public void recordJobStatistics() {
		mFairnessIndices.add(new FairnessIndex(Simulation.getSimulationTime(), computeJainFairness()));
		mLossValues.add(new LossValue(Simulation.getSimulationTime(), computeCumulativeLoss()));
		if(Cluster.getInstance().getRunningJobs().size() > 0) {
			ClusterEventQueue.getInstance().enqueueEvent(new 
					JobStatisticEvent(Simulation.getSimulationTime() + 5));
		}
	}
	
	private double computeCumulativeLoss() {
		List<IntraJobScheduler> jobs = Cluster.getInstance().getRunningJobs();
		double loss = 0.0;
		for(IntraJobScheduler job : jobs) {
			loss += job.getLoss(job.getmTotalExpectedIterations()-job.getmTotalIterationsRemaining());
		}
		return loss;
	}
	
	private double computeJainFairness() {
		List<IntraJobScheduler> jobs = Cluster.getInstance().getRunningJobs();
		List<Double> ratios = new ArrayList<Double>();
		List<Integer> jobGroups = new ArrayList<Integer>();
		for(IntraJobScheduler job: jobs) {
			if(!jobGroups.contains(job.getJobGroupId())) {
				// Have not processed this job group yet
				jobGroups.add(job.getJobGroupId());
				List<IntraJobScheduler> jobsInGroup = JobGroupManager.getInstance().getJobsInGroup(job);
				double total = 0;
				for(IntraJobScheduler jg : jobsInGroup) {
					total += jg.getCurrentEstimate()/jg.getIdealEstimate();
				}
				ratios.add(total/jobsInGroup.size());
			}
		}
		return Math.pow(sum(ratios), 2)/(ratios.size() * sumOfSquares(ratios));
	}
	
	private double sum(List<Double> values) {
		double sum = 0.0;
		for(Double val : values) {
			sum += val;
		}
		return sum;
	}
	
	private double sumOfSquares(List<Double> values) {
		double sum = 0.0;
		for(Double val : values) {
			sum += val*val;
		}
		return sum;
	}
	
	/**
	 * Print out individual JCTs, average JCT, and makespan
	 */
	public void printStats() {
		printJCT();
		printMakespan();
		printFairnessIndex();
		printLosses();
	}
	
	private void printJCT() {
		double total_jct = 0.0;
		for(Integer key : mJobTime.keySet()) {
			total_jct += mJobTime.get(key).getJobTime();
			System.out.println("Job " + Integer.toString(key) + " ran for time: " + 
					Double.toString(mJobTime.get(key).getJobTime()));
		}
		double avg_jct = total_jct/mJobTime.keySet().size();
		System.out.println("Average JCT: " + Double.toString(avg_jct));
	}
	
	private void printMakespan() {
		double earliest_start_time = Double.MAX_VALUE;
		double latest_end_time = Double.MIN_VALUE;
		for(Integer key : mJobTime.keySet()) {
			double start_time = mJobTime.get(key).getStartTime();
			double end_time = mJobTime.get(key).getEndTime();
			if(start_time < earliest_start_time) {
				earliest_start_time = start_time;
			}
			if(end_time > latest_end_time) {
				latest_end_time = end_time;
			}
		}
		System.out.println("Makespan: " + Double.toString(latest_end_time-earliest_start_time));
	}
	
	private void printFairnessIndex() {
		for(FairnessIndex f : mFairnessIndices) {
			System.out.println("JF " + Double.toString(f.getTimestamp()) + 
					" " + Double.toString(f.getFairness()));
		}
	}
	
	private void printLosses() {
		for(LossValue l : mLossValues) {
			System.out.println("Loss " + Double.toString(l.getTimestamp()) + 
					" " + Double.toString(l.getLoss()));
		}
	}
	
	private class SingleJobStat {
		private double mStartTime;
		private double mEndTime;
		
		public SingleJobStat(double start_time) {
			mStartTime = start_time;
			mEndTime = -1; // indicates not set
		}
		
		public void setEndTime(double end_time) {
			mEndTime = end_time;
		}
		
		public double getStartTime() {
			return mStartTime;
		}
		
		public double getEndTime() {
			return mEndTime;
		}
		
		public double getJobTime() {
			if(mEndTime == -1) { // not set
				return -1; // invalid request
			}
			return mEndTime - mStartTime;
		}
	}
	
	private class FairnessIndex {
		double mTimestamp;
		double mFairness;
		
		public FairnessIndex(double timestamp, double fairness) {
			mTimestamp = timestamp;
			mFairness = fairness;
		}
		
		public double getTimestamp() {
			return mTimestamp;
		}
		
		public double getFairness() {
			return mFairness;
		}
	}
	
	private class LossValue {
		private double mTimestamp;
		private double mLoss;
		
		public LossValue(double timestamp, double loss) {
			mTimestamp = timestamp;
			mLoss = loss;
		}
		
		public double getTimestamp() {
			return mTimestamp;
		}
		
		public double getLoss() {
			return mLoss;
		}
	}
}