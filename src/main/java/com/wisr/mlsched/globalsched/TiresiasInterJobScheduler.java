package com.wisr.mlsched.globalsched;

import java.util.List;

import com.wisr.mlsched.resources.GPU;

public class TiresiasInterJobScheduler extends InterJobScheduler {

	@Override
	public void onResourceAvailable(List<GPU> gpu_set) {
		perGPUResourceAllocator(gpu_set);
	}
	
}