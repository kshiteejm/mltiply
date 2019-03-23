package com.wisr.mlsched.events;

import com.wisr.mlsched.localsched.IntraJobScheduler;
import com.wisr.mlsched.localsched.IntraJobSchedulerFactory;
import com.wisr.mlsched.resources.Cluster;

import org.json.simple.JSONObject;

/**
 * Event abstraction for Job Arrival
 */
public class JobArrivalEvent extends ClusterEvent {

	private JSONObject mJobConfiguration; // Configuration
	public JobArrivalEvent(double timestamp, JSONObject configuration) {
		super(timestamp);
		mJobConfiguration = configuration;
		setPriority(ClusterEvent.EventType.JOB_ARRIVAL);
	}

	@Override
	public void handleEvent() {
		super.handleEvent();
		IntraJobScheduler job = IntraJobSchedulerFactory.createInstance(mJobConfiguration, 
				Cluster.getInstance().getPolicy());
		Cluster.getInstance().addJob(job);
	}
}