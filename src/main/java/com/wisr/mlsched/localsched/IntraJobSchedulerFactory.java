package com.wisr.mlsched.localsched;

import org.json.simple.JSONObject;

/**
 * Factory for producing IntraJobScheduler objects
 */
public class IntraJobSchedulerFactory {
	public static IntraJobScheduler createInstance(JSONObject workload_config,
			String policy) {
		switch(policy) {
			case "AAM":
				return new ThemisIntraJobScheduler(workload_config);
			case "Gandiva":
				return new GandivaIntraJobScheduler(workload_config);
			case "SLAQ":
				return new SLAQIntraJobScheduler(workload_config);
			case "Tiresias":
				return new TiresiasIntraJobScheduler(workload_config);
		}
		// TODO: Error log - will show up as NullPointerException right now
		return null;
	}
}