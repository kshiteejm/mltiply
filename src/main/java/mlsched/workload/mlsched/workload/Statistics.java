package mlsched.workload;

import java.util.ArrayList;

public class Statistics {
	
	public Statistics(int jobId, double jobArrivalTime) {
		this.jobId = jobId;
		this.jobArrivalTime = jobArrivalTime;
	}
	
	public void printStats() {
		System.out.println("JobId=" + jobId + 
				" Arrival Time=" + jobArrivalTime + 
				" Start Time=" + jobStartTime + " End Time=" + 
				jobEndTime);
	}
	
	public int jobId;
	public ArrayList<Double> iterStartTimes = new ArrayList<>();
	public ArrayList<Double> iterEndTimes = new ArrayList<>();
	public double jobArrivalTime;
	public double jobStartTime;
	public double jobEndTime;
}
