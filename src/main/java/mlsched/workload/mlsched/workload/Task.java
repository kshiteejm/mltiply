package mlsched.workload;

public class Task implements Comparable<Task> {
	public int jobId;	
	public String taskId;
	public double duration;
	public int demands;

	public Task(int jobId, String taskId, double duration) {
		this.jobId = jobId;
		this.taskId = taskId;
		this.duration = duration;
	}

	@Override
	public String toString() {
		String output = "<" + jobId + ":" + taskId + ":" + taskId + ">";
		return output;
	}

	public int compareTo(Task t) {
	    int out = 0;
	    if (jobId != t.jobId)
	      if (jobId > t.jobId)
	        out = 1;
	      else
	        out = -1;
	    else if (taskId != t.taskId)
	      out = taskId.compareTo(t.taskId);   
	    return out;
	}
}
