package com.wisr.mlsched;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Simulation {
	
	private static double mTime;
	private static final Level mLogLevel = Level.INFO;
	
	public static void main(String args[]) {
		String cluster_config_file = args[0];
		String workload_config_file = args[1];
		Logger log = Logger.getLogger(Simulation.class.getSimpleName());
		log.setLevel(mLogLevel);
		
		// Get the configurations
		JSONObject clusterConfig = ConfigUtils.getClusterConfig(cluster_config_file);
		JSONArray workloadConfig = ConfigUtils.getWorkloadConfigs(workload_config_file);
		log.info("Starting Simulation");
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
	
	public static Level getLogLevel() {
		return mLogLevel;
	}
}