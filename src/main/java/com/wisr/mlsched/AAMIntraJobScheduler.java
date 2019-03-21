package com.wisr.mlsched;

import java.util.List;
import org.json.simple.JSONObject;

public class AAMIntraJobScheduler extends IntraJobScheduler {

	public AAMIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public List<Bid> prepareBid(List<GPU> offeredGPUs) {
		return null;
		// TODO: Implement
	}
	
}