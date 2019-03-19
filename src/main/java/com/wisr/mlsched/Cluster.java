package com.wisr.mlsched;

import java.util.List;
import org.json.simple.JSONObject;
import java.util.ArrayList;

/**
 * Cluster represents the set of GPUs, the global scheduler to manage
 * the distribution of GPUs to jobs and a set of jobs.
 */
public class Cluster {

	private List<GPU> mGpusInCluster; // List of GPUs belong to cluster
	private List<IntraJobScheduler> mRunningJobs; // List of running jobs in cluster
	private InterJobScheduler mScheduler; // Instance of inter-job scheduler
	private String mPolicy; // Policy of Cluster

	private static Cluster sInstance = null; // Singleton Instance of Cluster

	/**
	 * Creates an instance of the required cluster from the given configuration.
	 * This methods creates GPU objects and initializes the running jobs and scheduler.
	 * @param config
	 */
	private Cluster(ClusterConfiguration config) {
		// Create GPUs based on configuration
		mGpusInCluster = new ArrayList<GPU>();
		for(int i=0;i<config.getRacks();i++) {
			for(int j=0;j<config.getMachinesPerRack();j++) {
				for(int k=0;k<config.getSlotsPerMachine();k++) {
					for(int l=0;l<config.getGPUsPerSlot();l++) {
						mGpusInCluster.add(new GPU(new GPULocation(l, k, j, i)));
					}
				}
			}
		}
		mRunningJobs = new ArrayList<IntraJobScheduler>();
		mPolicy = config.getPolicy();
		mScheduler = InterJobSchedulerFactory.createInstance(mPolicy);
	}
	
	/**
	 * Clients will call this API to get the singleton instance of the Cluster object
	 * @return An instance of the Cluster object
	 */
	public static Cluster getInstance() {
		if (sInstance == null) {
			// TODO: Error log here. No one should call this before cluster is created
			return null;
		}
		return sInstance;
	}
	
	/**
	 * Client will call this API to create a cluster with given GPU configuration.
	 * This method will internally create a singleton instance of the cluster.
	 * @param config
	 */
	public static Cluster createCluster(JSONObject config) {
		// TODO: Malformed config exception handling
		// TODO: Should be able to create only one instance globally
		ClusterConfiguration clusterConfig = ConfigUtils.getClusterConfig(config);
		sInstance = new Cluster(clusterConfig);
		return sInstance;
	}
	
	/**
	 * Get the set of GPUs in the cluster.
	 * @return A List consisting of GPU objects
	 */
	public List<GPU> getGPUsInCluster() {
		return mGpusInCluster;
	}
	
	/**
	 * Get the list of jobs currently running on the cluster
	 * @return A list consisting of IntraJobScheduler objects
	 */
	public List<IntraJobScheduler> getRunningJobs() {
		return mRunningJobs;
	}
	
	/**
	 * Add a job to list of running jobs
	 * @param job
	 */
	public void addJob(IntraJobScheduler job) {
		mRunningJobs.add(job);
	}
	
	/**
	 * Remove a job from list of running jobs
	 * @param job
	 */
	public void removeJob(IntraJobScheduler job) {
		// TODO: Sanity checking. Review return type
		mRunningJobs.remove(job);
	}
	
	/**
	 * Get an instance of the inter-job scheduler
	 * @return the singleton instance of InterJobScheduler
	 */
	public InterJobScheduler getScheduler() {
		return mScheduler;
	}
	
	/**
	 * Returns the policy with which the cluster is operating
	 * @return
	 */
	public String getPolicy() {
		return mPolicy;
	}
}