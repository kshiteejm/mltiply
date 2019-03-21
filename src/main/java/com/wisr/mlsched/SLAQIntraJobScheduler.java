package com.wisr.mlsched;

import java.util.List;
import org.json.simple.JSONObject;

public class SLAQIntraJobScheduler extends IntraJobScheduler {

	public SLAQIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public List<Bid> prepareBid(List<GPU> offeredGPUs) {
		return null;
		// TODO: Implement
	}
	
}