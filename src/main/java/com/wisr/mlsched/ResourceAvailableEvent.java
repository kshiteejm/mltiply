package com.wisr.mlsched;

import java.util.List;

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