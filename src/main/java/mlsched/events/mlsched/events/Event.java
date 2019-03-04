package mlsched.events;

import mlsched.workload.Job;

public class Event implements Comparable<Event> {
	public double timeStamp;
	Job j;

	public Event(double timeStamp, Job j) {
		this.timeStamp = timeStamp;
		this.j = j;
	}
	
	public void eventHandler() {
	}	
	
	@Override
	public int compareTo(Event e) {

		if (timeStamp > e.timeStamp)
			return 1;
		else
			return -1;

	}
}
