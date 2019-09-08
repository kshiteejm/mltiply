package com.wisr.mlsched.localsched;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.job.Bid;
import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

import org.json.simple.JSONObject;

public class SJFIntraJobScheduler extends IntraJobScheduler {

	private static Logger sLog; // Instance of logger
	
	public SJFIntraJobScheduler(JSONObject config) {
		super(config);
		sLog = Logger.getLogger(Cluster.class.getSimpleName());
		sLog.setLevel(Simulation.getLogLevel());
	}

	@Override
	public List<Bid> prepareBid(List<GPU> offeredGPUs) {
		// We should get a bid only 1 GPU at a time
		if(getGPUsAvailableForNextIteration().size() >= mMaxParallelism) {
			// Already have enough GPUs. No need to bid
			return null;
		}
		if(offeredGPUs.size() != 1) {
			sLog.severe("Offered incorrect # GPUs: " + 
					Integer.toString(offeredGPUs.size()));
			return null;
		}
		Set<GPU> potentialNewGPUSet = new HashSet<GPU>(getGPUsAvailableForNextIteration());
		potentialNewGPUSet.addAll(offeredGPUs);
		// Prepare a bid to prioritize job based on it's total running time
		double runningTime = (mTotalIterationsRemaining*mTimePerIteration)*
				getPlacementSlowdown(potentialNewGPUSet)/potentialNewGPUSet.size();
		runningTime = -1*runningTime; // shorter running time should have higher benefit
		List<Bid> bidList = new ArrayList<Bid>();
		sLog.info("JobGroup:" + Integer.toString(getJobGroupId())
		+ " Job:" + Integer.toString(getJobId()) + 
		" Bid:" + Double.toString(runningTime));
		bidList.add(new Bid(offeredGPUs, runningTime, this));
		return bidList;
	}
	
}