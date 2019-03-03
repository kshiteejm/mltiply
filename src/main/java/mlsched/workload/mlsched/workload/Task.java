package mlsched.workload;

public class Task implements Comparable<Task> {
	public int jobId;	
	public int taskId;
	public double duration;
	public int demands;

	public Task(int jobId, int taskId) {
		this.jobId = jobId;
		this.taskId = taskId;
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
	      if (taskId > t.taskId)
	        out = 1;
	      else
	        out = -1;
	    return out;
	}
}
