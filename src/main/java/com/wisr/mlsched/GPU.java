package com.wisr.mlsched;

/**
 * GPU represents the location of GPU and
 * information about which job holds the GPU.
 */
public class GPU {
	private final long NOT_LEASED = -1; // Constant for not leased
	
	private GPULocation mGpuLocation;
	private long mLeaseStart; // Start time of GPU lease
    private long mLeaseDurationMs; // Duration of GPU lease
    private IntraJobScheduler mJobUsingGPU; // Job currently using the GPU
    
    /**
     * Constructor for GPU object
     * @param location
     */
    public GPU(GPULocation location) {
    	mGpuLocation = location;
    	mLeaseStart = NOT_LEASED;
    	mLeaseDurationMs = NOT_LEASED;
    	mJobUsingGPU = null;
    }
    
    /**
     * Assign the GPU to a job for a time specified by lease
     * @param lease_duration_ms
     * @param job
     */
    public void assignGPU(long lease_duration_ms, IntraJobScheduler job) {
    	// TODO: assert that already not leased to another job
    	mLeaseDurationMs = lease_duration_ms;
    	mLeaseStart = System.currentTimeMillis();
    	mJobUsingGPU = job;
    }
    
    /**
     * Check if the GPU has been leased to a job
     * @return true if GPU has been leased, false otherwise
     */
    public boolean isLeased() {
    	return mLeaseStart != NOT_LEASED;
    }
    
    /**
     * Check if the job's lease on the GPU has expired
     * @return true if leased has expired, false otherwise
     */
    public boolean hasLeaseExpired() {
    	if (System.currentTimeMillis() > mLeaseStart + mLeaseDurationMs) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Returns the job that has leased this GPU
     * @return instance of IntraJobScheduler that holds lease to the GPU
     */
    public IntraJobScheduler getJob() {
    	return mJobUsingGPU;
    }
    
    /**
     * Returns the location for this GPU
     * @return instance of GPULocation
     */
    public GPULocation getLocation() {
    	return mGpuLocation;
    }
    
    /**
     * Mark this GPU as not belonging to any job.
     */
    public void markLeaseEnd() {
    	mLeaseStart = NOT_LEASED;
    	mLeaseDurationMs = NOT_LEASED;
    	mJobUsingGPU = null;
    }
}