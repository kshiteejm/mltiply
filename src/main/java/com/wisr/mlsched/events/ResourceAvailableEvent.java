package com.wisr.mlsched.events;

import java.util.List;

import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

/**
 * Implementation of event when resources are available.
 */
public class ResourceAvailableEvent extends ClusterEvent {

	private List<GPU> mResources;
	public ResourceAvailableEvent(double timestamp, List<GPU> resources) {
		super(timestamp);
		mResources = resources;
		setPriority(ClusterEvent.EventType.RESOURCE_AVAILABLE);
	}

	@Override
	public void handleEvent() {
		super.handleEvent();
		Cluster.getInstance().getScheduler().onResourceAvailable(mResources);
	}
	
}