package com.wisr.mlsched.globalsched;

/**
 * Factory for producing InterJobScheduler objects
 */
public class InterJobSchedulerFactory {
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
		}
		// TODO: Error log - will show up as NullPointer Exception right now - should be fine? add an sLog statement?
		return null;
	}
}