package com.wisr.mlsched;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Simulation {
	
	private static double mTime;
	
	public static void main(String args[]) {
		String cluster_config_file = args[0];
		String workload_config_file = args[1];
		
		// Get the configurations
		JSONObject clusterConfig = ConfigUtils.getClusterConfig(cluster_config_file);
		JSONArray workloadConfig = ConfigUtils.getWorkloadConfigs(workload_config_file);
		
		// Initialize simulator time
		mTime = 0;
		
		// Initialize cluster
		Cluster cluster = Cluster.createCluster(clusterConfig);
		
		ClusterEventQueue eventQueue = ClusterEventQueue.getInstance();
		
		for(Object object : workloadConfig) {
			JSONObject config = (JSONObject) object;
			eventQueue.enqueueEvent(new JobArrivalEvent
					(ConfigUtils.getJobStartTime(config), config));
		}
		
		// Start processing
		eventQueue.start();
	}
	
	public static double getSimulationTime() {
		return mTime;
	}
	
	public static void setSimulationTime(double time) {
		mTime = time;
	}
}