package com.wisr.mlsched;

import java.util.List;
import org.json.simple.JSONObject;

public class TiresiasIntraJobScheduler extends IntraJobScheduler {

	public TiresiasIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public List<Bid> prepareBid(List<GPU> offeredGPUs) {
		return null;
		// TODO: Implement
	}
	
}