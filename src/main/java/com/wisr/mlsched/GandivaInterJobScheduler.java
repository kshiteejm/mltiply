package com.wisr.mlsched;

import java.util.List;

public class GandivaInterJobScheduler extends InterJobScheduler {

	@Override
	public void onResourceAvailable(List<GPU> gpu_set) {
		perGPUResourceAllocator(gpu_set);
	}
}