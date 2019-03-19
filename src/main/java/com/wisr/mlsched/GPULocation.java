package com.wisr.mlsched;

/**
 * Represents the physical location coordinates of the GPU.
 */
public class GPULocation {
	private int mGpuId; // Unique GPU ID
    private int mSlotId; // Slot within a machine that GPU belongs to
    private int mMachineId; // Machine on rack that GPU belongs to
    private int mRackId; // Rack ID within cluster
    
    /**
     * Constructor for GPULocation object
     * @param gpu_id
     * @param slot_id
     * @param machine_id
     * @param rack_id
     */
    public GPULocation(int gpu_id, int slot_id, int machine_id, int rack_id) {
    	this.mGpuId = gpu_id;
    	this.mSlotId = slot_id;
    	this.mMachineId = machine_id;
    	this.mRackId = rack_id;
    }
    
    /**
     * Returns the GPU ID
     */
    public int getGPUId() {
    	return mGpuId;
    }
    
    /**
     * Returns the GPU's Slot ID
     */
    public int getSlotId() {
    	return mSlotId;
    }
    
    /**
     * Returns the GPU's Machine ID
     */
    public int getMachineId() {
    	return mMachineId;
    }
    
    /**
     * Returns the GPU's Rack ID
     */
    public int getRackId() {
    	return mRackId;
    }
}