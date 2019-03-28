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
		
		// Get the current gradient of loss function
		List<JobRank> ranks = new ArrayList<JobRank>();
		for(IntraJobScheduler j : listJobsInGroup) {
			ranks.add(new JobRank(j, j.getLossGradient()));
		}
		// Sort the loss gradients in increasing order
		Collections.sort(ranks, new JobRankComparator());
		// Find the rank of job in it's job group
		int index_of_job = 0;
		for(JobRank jr : ranks) {
			if(job.equals(jr.getJob())) {
				break;
			}
			index_of_job++;
		}
		assert(index_of_job >= 0 && index_of_job < ranks.size()); // sanity check
		double rank = (index_of_job + 1)/ranks.size();
		
		// Get the job quality category
		JobQualityCategory jobCategory = getJobQuality(job, rank);
		
		// Return the promise discount
		return getPromiseDiscountFromQuality(job, jobCategory);
	}
	
	private JobQualityCategory getJobQuality(IntraJobScheduler job, double rank) {
		if(rank < job.getBadJobThreshold()) {
			return JobQualityCategory.JOB_QUALITY_BAD;
		} else if (rank < job.getPromisingJobThreshold()) {
			return JobQualityCategory.JOB_QUALITY_PROMISING;
		} else {
			return JobQualityCategory.JOB_QUALITY_GOOD;
		}
	}
	
	private double getPromiseDiscountFromQuality(IntraJobScheduler job, JobQualityCategory category) {
		switch(category) {
		case JOB_QUALITY_BAD:
			return job.getBadJobDiscount();
		case JOB_QUALITY_PROMISING:
			return job.getPromisingJobDiscount();
		case JOB_QUALITY_GOOD:
			return 1.0;
		}
		return 1.0;
	}
	
	private class JobRank {
		private IntraJobScheduler mJob;
		private double mLossGradient;
		
		public JobRank(IntraJobScheduler job, double loss_gradient) {
			mJob = job;
			mLossGradient = loss_gradient;
		}
		
		public IntraJobScheduler getJob() {
			return mJob;
		}
		
		public double getLossGradient() {
			return mLossGradient;
		}
	}
	
	private class JobRankComparator implements Comparator<JobRank> {
		@Override
		public int compare(JobRank jr1, JobRank jr2) {
			if(jr1.getLossGradient() <= jr2.getLossGradient()) {
				return -1;
			}
			return 1;
		}
		
	}
}