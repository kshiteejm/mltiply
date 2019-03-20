package mlsched.utils;

import java.util.Random;

import mlsched.simulator.Main;

public class SynchronousTrainingSpeedFunction implements TrainingSpeedFunction {
	double a, b, c, d;
	public SynchronousTrainingSpeedFunction() {
		Random r = new Random(Main.randSeed);
		this.a = r.nextDouble();
		this.b = r.nextDouble();
		this.c = r.nextDouble();
		this.d = r.nextDouble();
	}
	
	public double getValue(int numParameterServers, int numWorkers) {
		double value = a + b * (numWorkers / numParameterServers) + c * numWorkers + d * numParameterServers;
		value = 1 / value;
		value = numWorkers * value;
		return value;
	}
}
