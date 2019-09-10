package com.wisr.mlsched.job;

import java.util.List;

import com.wisr.mlsched.localsched.IntraJobScheduler;
import com.wisr.mlsched.resources.GPU;

/**
 * Encapsulation for a bid
 */
public class Bid {
	private List<GPU> mList; // GPU list part of the bid
	private double mExpectedBenefit; // Expected gain from making the bid
	private IntraJobScheduler mJob; // Job making this bid
	private double mOldBenefit;
	public double mOldPscore;
	public double mNewPscore;
	
	/**
	 * Constructor
	 * @param list
	 * @param benefit
	 */
	public Bid(List<GPU> list, double benefit, IntraJobScheduler job) {
		mList = list;
		mExpectedBenefit = benefit;
		mJob = job;
		mOldBenefit = 0;
	}
	
	public Bid(List<GPU> list, double benefit, double old_benefit, IntraJobScheduler job,
			double newScore, double oldScore) {
		mList = list;
		mExpectedBenefit = benefit;
		mJob = job;
		mOldBenefit = old_benefit;
		mOldPscore = oldScore;
		mNewPscore = newScore;
	}
	
	public double getOldBenefit() {
		return mOldBenefit;
	}
	
	/**
	 * Return list of GPUs part of bid
	 */
	public List<GPU> getGPUList() {
		return mList;
	}
	
	/**
	 * Return benefit from bid
	 */
	public double getExpectedBenefit() {
		return mExpectedBenefit;
	}
	
	/**
	 * Returns the job that created this bid
	 */
	public IntraJobScheduler getJob() {
		return mJob;
	}
	
	@Override
	public String toString() {
		String out = "";
		for (GPU gpu: mList) {
			out = out + ":[" + gpu.getLocation().getPrettyString() +"]";
		}
		out = out + " exp:" + mExpectedBenefit;
		out = out + " old: " + mOldBenefit;
		out = out + " jobid:" + mJob.getJobId();
		out = out + " old speedup: " + mOldPscore;
		out = out + " new speedup: " + mNewPscore;
		return out;
	}
}