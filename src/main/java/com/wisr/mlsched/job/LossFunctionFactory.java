package com.wisr.mlsched.job;

/**
 * Factory for producing IntraJobScheduler objects
 */
public class LossFunctionFactory {
	public static LossFunction createInstance(String lossFunctionType, 
			int numIterations, int seed) {
		switch(lossFunctionType) {
			case "sublinear":
				return SubLinearLossFunction.getRandomSublinearFunction(numIterations, seed);
			case "superlinear":
				return SubLinearLossFunction.getRandomSublinearFunction(numIterations, seed);
		}
		// TODO: Error log - willl show up as a NullPointerException right now. so should be fine? or just have an sLog and print it as well.
		return null;
	}
}