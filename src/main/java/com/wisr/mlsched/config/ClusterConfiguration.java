package com.wisr.mlsched.config;

/**
 * Representation of the cluster's configuration
 */
public class ClusterConfiguration {
	private int mRacks;
	private int mMachinesPerRack;
	private int mSlotsPerMachine;
	private int mGPUsPerSlot;
	private String mPolicy;
	private double mLeaseTime;
	private double mFairnessThreshold;
	private double mEpsilon;
	private double mBadJobThreshold;
	private double mPromisingJobThreshold;
	private double mBadJobDiscount;
	private double mPromisingJobDiscount;
	
	public ClusterConfiguration(int racks, int machines_per_rack,
			int slots_per_machine, int gpus_per_slot, String policy, double lease,
			double fairness_threshold, double epsilon, double bad_job_threshold,
			double promising_job_threshold, double bad_job_discount,
			double promising_job_discount) {
		mRacks = racks;
		mMachinesPerRack = machines_per_rack;
		mSlotsPerMachine = slots_per_machine;
		mGPUsPerSlot = gpus_per_slot;
		mPolicy = policy;
		mLeaseTime = lease;
		mFairnessThreshold = fairness_threshold;
		mEpsilon = epsilon;
		mBadJobThreshold = bad_job_threshold;
		mPromisingJobThreshold = promising_job_threshold;
		mBadJobDiscount = bad_job_discount;
		mPromisingJobDiscount = promising_job_discount;
	}
	
	public double getLeaseTime() {
		return mLeaseTime;
	}
	
	public int getRacks() {
		return mRacks;
	}

	public int getMachinesPerRack() {
		return mMachinesPerRack;
	}

	public int getSlotsPerMachine() {
		return mSlotsPerMachine;
	}

	public int getGPUsPerSlot() {
		return mGPUsPerSlot;
	}

	public String getPolicy() {
		return mPolicy;
	}
	
	public double getFairnessThreshold() {
		return mFairnessThreshold;
	}
	
	public double getEpsilon() {
		return mEpsilon;
	}

	public double getBadJobThreshold() {
		return mBadJobThreshold;
	}

	public double getPromisingJobThreshold() {
		return mPromisingJobThreshold;
	}

	public double getBadJobDiscount() {
		return mBadJobDiscount;
	}
	
	public double getPromisingJobDiscount() {
		return mPromisingJobDiscount;
	}
}