package com.wisr.mlsched;

import java.util.List;

/**
 * Encapsulation for a bid
 */
public class Bid {
	private List<GPU> mList; // GPU list part of the bid
	private double mExpectedBenefit; // Expected gain from making the bid
	private IntraJobScheduler mJob; // Job making this bid
	
	/**
	 * Constructor
	 * @param list
	 * @param benefit
	 */
	public Bid(List<GPU> list, double benefit, IntraJobScheduler job) {
		mList = list;
		mExpectedBenefit = benefit;
		mJob = job;
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
}