package com.wisr.mlsched.localsched;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import com.wisr.mlsched.Simulation;
import com.wisr.mlsched.job.Bid;
import com.wisr.mlsched.resources.Cluster;
import com.wisr.mlsched.resources.GPU;

import org.json.simple.JSONObject;

public class ThemisIntraJobScheduler extends IntraJobScheduler {

	private static Logger sLog; // Instance of logger

	public ThemisIntraJobScheduler(JSONObject config) {
		super(config);
		sLog = Logger.getLogger(Cluster.class.getSimpleName());
		sLog.setLevel(Simulation.getLogLevel());
	}

	@Override
	public List<Bid> prepareBid(List<GPU> offeredGPUs) {
		if (getGPUsAvailableForNextIteration().size() >= mMaxParallelism) {
			// Already have enough GPUs. No need to bid
			return null;
		}
		List<Bid> bids = new ArrayList<Bid>();
		Queue<String> q = new LinkedList<String>();
		q.add("1");
		/*if(Cluster.getInstance().getRunningJobs().size() == 5) {
			System.out.println("Offered GPUs: " + offeredGPUs.size());
		}*/
		while (q.size() > 0) {
			String s1 = q.peek();
			q.remove();
			Bid bid = prepareBidWithGPUs(s1, offeredGPUs);
			if(bid != null) {
				/*if(Cluster.getInstance().getRunningJobs().size() == 5) {
					System.out.println("Debugging Bid :: " + bid.toString());
				}*/
				bids.add(bid);
			}
			if(getGPUsInString(s1) < mMaxParallelism && s1.length() < offeredGPUs.size()) {
				String s2 = s1;
				q.add(s1 + "0");
				q.add(s2 + "1");	
			}
		}
		return bids;
	}
	
	private int getGPUsInString(String s) {
		char[] bitmaskArray = s.toCharArray();
		int count = 0;
		for(int i=0;i<bitmaskArray.length;i++) {
			if(bitmaskArray[i] == '1') {
				count++;
			}
		}
		return count;
	}
	
	private Bid prepareBidWithGPUs(String bitmask, List<GPU>offeredGPUs) {
		String paddedBitMask = padLeftZeros(bitmask, offeredGPUs.size());
		List<GPU> gpusForBid = new ArrayList<GPU>();
		char[] bitmaskArray = paddedBitMask.toCharArray();
		for(int i=0;i<bitmaskArray.length;i++) {
			if(bitmaskArray[i] == '1') {
				gpusForBid.add(offeredGPUs.get(i));
			}
		}
		if(getGPUsAvailableForNextIteration().size() + gpusForBid.size() > mMaxParallelism) {
			// No point of making this bid
			return null;
		}
		Set<GPU> potentialNewGPUSet = new HashSet<GPU>(getGPUsAvailableForNextIteration());
		potentialNewGPUSet.addAll(gpusForBid);
		//double oldJobSpeedUp = getGPUsAvailableForNextIteration().size() * 
			//	getPlacementSlowdown(getGPUsAvailableForNextIteration());
		//double expectedJobSpeedUp = potentialNewGPUSet.size() * getPlacementSlowdown(potentialNewGPUSet);
		//double ratio = expectedJobSpeedUp/oldJobSpeedUp;
		//double expectedJobSpeedUp = gpusForBid.size() * getPlacementSlowdown(potentialNewGPUSet)/
			//	getPlacementSlowdown(getGPUsAvailableForNextIteration());
		//System.out.println(offeredGPUs.size());
		//System.out.println(expectedJobSpeedUp);
		//double oldSpeedup = getGPUsAvailableForNextIteration().size()*getPlacementSlowdown(getGPUsAvailableForNextIteration());
		//double oldTs = (Simulation.getSimulationTime() - mJobStartTime) + 
		//		(mTotalIterationsRemaining*mTimePerIteration)/oldSpeedup;
		double newSpeedup = potentialNewGPUSet.size() * getPlacementSlowdown(potentialNewGPUSet);
		double newTs = (Simulation.getSimulationTime() - mJobStartTime) + 
				(mTotalIterationsRemaining*mTimePerIteration)/newSpeedup;
		double ratio = newTs/getIdealEstimate();
		//double ratio = newTs/oldTs;
		if(Double.compare(ratio, 1) <= 0) {
			// no point in making bid
			//System.out.println("No point in making bid");
			return null;
		}
		/*if(Double.compare(expectedJobSpeedUp, 1.0) <= 0) {
			// No point in making this bid
			return null;
		}*/
		//double newExpectedRunningTime = (Simulation.getSimulationTime() - mJobStartTime)
		//		+ (getmTotalIterationsRemaining() * mTimePerIteration) / expectedJobSpeedUp;
		//double expectedGain = newExpectedRunningTime/(getIdealEstimate()*oldRatio);
		//return new Bid(gpusForBid, expectedGain, this);
		
		double oldSpeedup = getGPUsAvailableForNextIteration().size() * getPlacementSlowdown(getGPUsAvailableForNextIteration());
		double oldTs = (Simulation.getSimulationTime() - mJobStartTime) + 
				(mTotalIterationsRemaining*mTimePerIteration)/oldSpeedup;
		double oldratio = oldTs/getIdealEstimate();
		if(Double.compare(ratio, oldratio) >= 0 && !Double.isInfinite(oldratio)) {
			/*System.out.println("Old: " + oldratio);
			System.out.println("New: " + ratio);
			System.out.println("NewGPUSetSize: " + Integer.toString(potentialNewGPUSet.size()) + 
				" Slowdown: " + Double.toString(getPlacementSlowdown(potentialNewGPUSet)));
			System.out.println("CurrentGPUSetSize: " + Integer.toString(getGPUsAvailableForNextIteration().size()) + 
				" Slowdown: " + Double.toString(getPlacementSlowdown(getGPUsAvailableForNextIteration())));*/
			//System.out.println("No point in making bid 2");
			return null;
		}
		Bid bid = new Bid(gpusForBid, ratio, oldratio, this, newSpeedup, oldSpeedup);
		//bid.setNewRatio(newTs/getIdealEstimate());
		return bid;
	}
	
	private String padLeftZeros(String inputString, int length) {
	    if (inputString.length() >= length) {
	        return inputString;
	    }
	    StringBuilder sb = new StringBuilder();
	    while (sb.length() < length - inputString.length()) {
	        sb.append('0');
	    }
	    sb.append(inputString);
	 
	    return sb.toString();
	}
}