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
		// TODO: Error log
		return null;
	}
}