package mlsched.utils;

import java.util.Random;

import mlsched.simulator.Main;

public class AsynchronousTrainingSpeedFunction implements TrainingSpeedFunction {
	double a, b, c, d;
	public AsynchronousTrainingSpeedFunction() {
		this.a = Main.r.nextDouble();
		this.b = Main.r.nextDouble();
		this.c = Main.r.nextDouble();
		this.d = Main.r.nextDouble();
	}
	
	public double getValue(int numParameterServers, int numWorkers) {
		double value = a + b * (numWorkers / numParameterServers) + c * numWorkers + d * numParameterServers;
		value = 1 / value;
		value = numWorkers * value;
		return value;
	}
}
