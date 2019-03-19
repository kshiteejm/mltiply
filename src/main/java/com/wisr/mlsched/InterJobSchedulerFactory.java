package com.wisr.mlsched;

/**
 * Factory for producing InterJobScheduler objects
 */
public class InterJobSchedulerFactory {
	public static InterJobScheduler createInstance(String policy) {
		switch(policy) {
			case "AAM":
				return new AAMInterJobScheduler();
			case "Gandiva":
				return new GandivaInterJobScheduler();
			case "SLAQ":
				return new SLAQInterJobScheduler();
			case "Tiresias":
				return new TiresiasInterJobScheduler();
		}
		// TODO: Error log
		return null;
	}
}