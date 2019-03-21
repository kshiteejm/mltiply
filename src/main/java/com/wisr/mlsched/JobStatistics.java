package com.wisr.mlsched;

import java.util.HashMap;

public class JobStatistics {
	
	private static JobStatistics sInstance = null; // Holder of singleton
	private HashMap<Integer, SingleJobStat> mJobTime; // Job start and end time per job
	
	/**
	 * Private constructor to enforce singleton
	 */
	private JobStatistics() {
		mJobTime = new HashMap<Integer, SingleJobStat>();
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
	
	/**
	 * Print out individual JCTs, average JCT, and makespan
	 */
	public void printStats() {
		printJCT();
		printMakespan();
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
}