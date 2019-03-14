package mlsched.events;

import mlsched.workload.Job;

public class Event {
	
	public double timeStamp;
	Job j;

	public Event(double timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public Event(double timeStamp, Job j) {
		this.timeStamp = timeStamp;
		this.j = j;
	}
	
	public void eventHandler() {
	}
	
	public void printInfo() {
		System.out.println("Event = " + this.getClass().toString() + " Timestamp = " + this.timeStamp + " JobID = " + this.j.jobId);
	}
}
