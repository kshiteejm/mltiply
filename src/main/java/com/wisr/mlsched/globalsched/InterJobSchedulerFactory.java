package com.wisr.mlsched.globalsched;

import java.util.logging.Logger;

/**
 * Factory for producing InterJobScheduler objects
 */
public class InterJobSchedulerFactory {
	private static Logger sLog = Logger.getLogger(InterJobSchedulerFactory.class.getSimpleName());
	
	public static InterJobScheduler createInstance(String policy) {
		switch(policy) {
			case "Themis":
				return new ThemisInterJobScheduler();
			case "Gandiva":
				return new GandivaInterJobScheduler();
			case "SLAQ":
				return new SLAQInterJobScheduler();
			case "Tiresias":
				return new TiresiasInterJobScheduler();
			case "SJF":
				return new SJFInterJobScheduler();
		}
		sLog.severe("Policy not defined");
		return null;
	}
}