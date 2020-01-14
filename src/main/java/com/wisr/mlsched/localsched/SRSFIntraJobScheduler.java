package com.wisr.mlsched.localsched;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.job.Bid;
import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

import org.json.simple.JSONObject;

public class SRSFIntraJobScheduler extends IntraJobScheduler {

	private static Logger sLog; // Instance of logger

	public SRSFIntraJobScheduler(JSONObject config) {
		super(config);
		sLog = Logger.getLogger(Cluster.class.getSimpleName());
		sLog.setLevel(Simulation.getLogLevel());
	}

	@Override
	public List<Bid> prepareBid(List<GPU> offeredGPUs) {
		// We should get a bid only 1 GPU at a time
		if (getGPUsAvailableForNextIteration().size() >= mMaxParallelism) {
			// Already have enough GPUs. No need to bid
			return null;
		}
		if (offeredGPUs.size() != 1) {
			sLog.severe("Offered incorrect # GPUs: "
					+ Integer.toString(offeredGPUs.size()));
			return null;
		}

		List<Bid> bidList = new ArrayList<Bid>();
		
		// Compute remaining GPU service
		double bidValue = (mTotalExpectedIterations - mTotalIterationsRemaining)*mTimePerIteration;
		// Submit negative of bid value so that lowest service remaining is prioritized
		bidList.add(new Bid(offeredGPUs, -1 * bidValue, this));
		return bidList;
	}

}