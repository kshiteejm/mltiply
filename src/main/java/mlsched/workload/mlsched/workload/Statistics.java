package mlsched.workload;

import java.util.ArrayList;

public class Statistics {

	public Statistics(Integer jobId, double jobArrivalTime) {
		this.jobId = jobId;
		this.jobArrivalTime = jobArrivalTime;
	}
	
	public void printStats() {
		System.out.println("JobId=" + jobId + 
				" ArrivalTime=" + jobArrivalTime + 
				" StartTime=" + jobStartTime + " EndTime=" + 
				jobEndTime + " NumIters=" + lossValues.size());
		for(int i=0; i<iterStartTimes.size(); i++) {
			System.out.println("IterStartTime=" + iterStartTimes.get(i) + 
					" IterEndTime=" + iterEndTimes.get(i) + " LossValue=" + lossValues.get(i));
		}
	}
	
	public Integer jobId;
	public double jobArrivalTime;
	public double jobStartTime;
	public double jobEndTime;
	
	public ArrayList<Double> iterStartTimes = new ArrayList<>();
	public ArrayList<Double> iterEndTimes = new ArrayList<>();
	public ArrayList<Double> lossValues = new ArrayList<>();
	
}
