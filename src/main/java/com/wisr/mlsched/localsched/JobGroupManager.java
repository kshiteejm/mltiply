package com.wisr.mlsched.localsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.wisr.mlsched.resources.Cluster;

public class JobGroupManager {
	private static JobGroupManager sInstance = null;
	private HashMap<Integer, List<IntraJobScheduler>> mJobMap;
	
	private enum JobQualityCategory {
			JOB_QUALITY_BAD,
			JOB_QUALITY_PROMISING,
			JOB_QUALITY_GOOD
	}
	
	/**
	 * Private constructor
	 */
	private JobGroupManager() {
		mJobMap = new HashMap<Integer, List<IntraJobScheduler>>();
	}
	
	/**
	 * Return the global instance of the JobGroupManager
	 * @return
	 */
	public static JobGroupManager getInstance() {
		if(sInstance == null) {
			sInstance = new JobGroupManager();
		}
		return sInstance;
	}
	
	/**
	 * Track this job under JobGroupManager
	 * @param job
	 */
	public void trackJob(IntraJobScheduler job) {
		List<IntraJobScheduler> listJobGroup = mJobMap.get(job.getJobGroupId());
		if(listJobGroup == null) {
			listJobGroup = new ArrayList<IntraJobScheduler>();
		}
		listJobGroup.add(job);
		mJobMap.remove(job.getJobGroupId());
		mJobMap.put(job.getJobGroupId(), listJobGroup);
	}
	
	/**
	 * Untrack this job from JobGroupManager
	 * @param job
	 */
	public void untrackJob(IntraJobScheduler job) {
		List<IntraJobScheduler> list = mJobMap.get(job.getJobGroupId());
		list.remove(job);
		mJobMap.remove(job.getJobGroupId());
		mJobMap.put(job.getJobGroupId(), list);
	}
	
	/**
	 * Return the promise of the job within the job group
	 * @param job
	 * @return
	 */
	public double getJobPromiseDiscount(IntraJobScheduler job) {
		// Get list of jobs in the Job Group
		List<IntraJobScheduler> listJobsInGroup = mJobMap.get(job.getJobGroupId());
		double jobIterations = job.getmTotalIterationsRemaining();
		double totalJGIterations = 0;
		for(IntraJobScheduler j : listJobsInGroup) {
			totalJGIterations += j.getmTotalIterationsRemaining();
		}
		return 1 - (jobIterations/totalJGIterations);
	}
	
	public List<IntraJobScheduler> getJobsInGroup(IntraJobScheduler job) {
		return mJobMap.get(job.getJobGroupId());
	}
}