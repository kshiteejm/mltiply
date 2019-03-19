package com.wisr.mlsched;

import org.json.simple.JSONObject;

public class ConfigUtils {
	/**
	 * Get cluster configuration JSON from configuration file
	 * @param configFile
	 * @return JSONObject
	 */
	public static JSONObject getClusterConfig(String configFile) {
		// TODO: Write implementation
		return null;
	}
	
	/**
	 * Get workload configuration JSON from workload configuration file
	 * @param configFile
	 * @return array of JSONObject
	 */
	public static JSONObject[] getWorkloadConfigs(String workloadFile) {
		// TODO: Write implementation
		return null;
	}
	
	/**
	 * Return a cluster configuration object from the given JSON configuration
	 * @param config
	 * @return ClusterConfiguration object
	 */
	public static ClusterConfiguration getClusterConfig(JSONObject config) {
		// TODO: Write implementation
		return null;
	}
	
	/**
	 * Create a new IntraJobScheduler for the workload and cluster properties
	 * @param workload_config
	 * @param cluster_config
	 * @return instance of IntraJobScheduler
	 */
	public static IntraJobScheduler createJob(JSONObject workload_config,
			JSONObject cluster_config) {
		// TODO: Write implementation
		return null;
	}
	
	/**
	 * Given the cluster configuration JSON, returns the policy
	 * @param config
	 * @return string indicating the policy
	 */
	public static String getClusterPolicy(JSONObject config) {
		// TODO: Write implementation
		return null;
	}
	
	/**
	 * Given a workload configuration for a job,
	 * return the start time for the job
	 * @param workload_config, JSON representing workload configuration.
	 * @return a double representing the start time.
	 */
	public static double getJobStartTime(JSONObject workload_config) {
		// TODO: Write implementation
		return 0.0;
	}
}