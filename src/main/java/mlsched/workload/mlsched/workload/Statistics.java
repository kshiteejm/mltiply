package mlsched.workload;

import java.util.ArrayList;

public class Statistics {
	
//	public Statistics(int jobId) {
//		this.jobId = jobId;
//	}

	public Statistics(Integer jobId, double jobArrivalTime) {
		this.jobId = jobId;
		this.jobArrivalTime = jobArrivalTime;
	}
	
	public void printStats() {
		System.out.println("JobId=" + jobId + 
				" Arrival Time=" + jobArrivalTime + 
				" Start Time=" + jobStartTime + " End Time=" + 
				jobEndTime);
	}
	
	public Integer jobId;
	public double jobArrivalTime;
	public double jobStartTime;
	public double jobEndTime;
	
	public ArrayList<Double> iterStartTimes = new ArrayList<>();
	public ArrayList<Double> iterEndTimes = new ArrayList<>();
	
}
