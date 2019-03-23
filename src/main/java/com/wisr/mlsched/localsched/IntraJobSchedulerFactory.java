package com.wisr.mlsched.localsched;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

/**
 * Factory for producing IntraJobScheduler objects
 */
public class IntraJobSchedulerFactory {
	private static Logger sLog = Logger.getLogger(IntraJobSchedulerFactory.class.getSimpleName());
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
		sLog.severe("Policy not defined");
		return null;
	}
}