package com.wisr.mlsched;

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
	
	public ClusterConfiguration(int racks, int machines_per_rack,
			int slots_per_machine, int gpus_per_slot, String policy, double lease,
			double fairness_threshold, double epsilon) {
		mRacks = racks;
		mMachinesPerRack = machines_per_rack;
		mSlotsPerMachine = slots_per_machine;
		mGPUsPerSlot = gpus_per_slot;
		mPolicy = policy;
		mLeaseTime = lease;
		mFairnessThreshold = fairness_threshold;
		mEpsilon = epsilon;
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
}