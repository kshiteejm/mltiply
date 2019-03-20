package mlsched.utils;

public interface TrainingSpeedFunction {
	public double getValue(int numParameterServers, int numWorkers);
}
