package com.wisr.mlsched.localsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
		Map<Integer, List<Bid>> gpuBenefit = new HashMap<Integer, List<Bid>>();
		List<Bid> bids = new ArrayList<Bid>();
		Queue<String> q = new LinkedList<String>();
		Collections.shuffle(offeredGPUs, new Random(0));
		q.add("1");
		//q.add("0");
		//q.add(Integer.toString(getJobId()%2));
		/*if(Cluster.getInstance().getRunningJobs().size() == 5) {
			System.out.println("Offered GPUs: " + offeredGPUs.size());
		}*/
		while (q.size() > 0) {
			String s1 = q.peek();
			q.remove();
			Bid bid = prepareBidWithGPUs(s1, offeredGPUs);
			if(bid != null) {
				List<Bid> bidsForGPUSize = gpuBenefit.get(getGPUsInString(s1));
				if(bidsForGPUSize == null) {
					bidsForGPUSize = new ArrayList<Bid>();
				}
				bidsForGPUSize.add(bid);
				gpuBenefit.put(getGPUsInString(s1), bidsForGPUSize);
			}
			if(getGPUsInString(s1) < mMaxParallelism && s1.length() < offeredGPUs.size()) {
				String s2 = s1;
				q.add(s1 + "0");
				q.add(s2 + "1");	
			}
		}
		// Return only the top bids for each GPU size
		for(Integer gpuSize : gpuBenefit.keySet()) {
			List<Bid> bidsForGPUSize = gpuBenefit.get(gpuSize);
			if(bidsForGPUSize != null) {
				Collections.sort(bidsForGPUSize, new PerGPUBidComparator());
				double largestPlacementScore = bidsForGPUSize.get(0).mNewPscore;
				bids.addAll(bidsForGPUSize.stream()
					    .filter(p -> p.mNewPscore == largestPlacementScore).collect(Collectors.toList()));
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
		//System.out.println(paddedBitMask);
		Set<GPU> potentialNewGPUSet = new HashSet<GPU>(getGPUsAvailableForNextIteration());
		potentialNewGPUSet.addAll(gpusForBid);
		
		double newSpeedup = potentialNewGPUSet.size() * getPlacementSlowdown(potentialNewGPUSet);
		double newTs = (Simulation.getSimulationTime() - mJobStartTime) + 
				(mTotalIterationsRemaining*mTimePerIteration)/newSpeedup;
		double ratio = newTs/getIdealEstimate();
		if(Double.compare(ratio, 1) < 0) {
			// no point in making bid
			return null;
		}
		double error_percent = mErrorRandom.nextInt(2*mErrorPercent) - mErrorPercent;
		double absolute_error = ratio*error_percent/100;
		//System.out.println("Old Val: " + ratio);
		ratio = ratio+absolute_error;
		//System.out.println("New Val: " + ratio);
		
		double oldSpeedup = getGPUsAvailableForNextIteration().size() * getPlacementSlowdown(getGPUsAvailableForNextIteration());
		double oldTs = (Simulation.getSimulationTime() - mJobStartTime) + 
				(mTotalIterationsRemaining*mTimePerIteration)/oldSpeedup;
		double oldratio = oldTs/getIdealEstimate();
		if(Double.compare(ratio, oldratio) >= 0 && !Double.isInfinite(oldratio)) {
			return null;
		}
		Bid bid = new Bid(gpusForBid, ratio, oldratio, this, newSpeedup, oldSpeedup);
		return bid;
	}
	
	@Override
	public boolean willParticipateInBid() {
		return getGPUsAvailableForNextIteration().size() < mMaxParallelism;
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
	
	protected class PerGPUBidComparator implements Comparator<Bid> {

		@Override
		public int compare(Bid bid1, Bid bid2) {
			int comp = Double.compare(bid1.mNewPscore, bid2.mNewPscore);
			return -1*comp;
		}
		
	}
}