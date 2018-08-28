package mltiply.events.datastructures;

import java.util.logging.Logger;

public class Task {

  private static Logger LOG = Logger.getLogger(Task.class.getName());

  public int taskId;
  public int jobId;
  public double duration;
  public Resource demand;

  public Task(int jobId, int taskId) {
    this.jobId = jobId;
    this.taskId = taskId;
  }

  public Task(int jobId, int taskId, double duration, Resource demand) {
    this.jobId = jobId;
    this.taskId = taskId;
    this.duration = duration;
    this.demand = demand;
  }

  public Task(Task t) {
    this.jobId = t.jobId;
    this.taskId = t.taskId;
    this.duration = t.duration;
    this.demand = new Resource(t.demand);
  }

  @Override
  public String toString() {
    String output = "<" + jobId + ":" + taskId + ">";
    return output;
  }

  public int compareTo(Task other) {
    int out = 0;
    if (jobId != other.jobId)
      if (jobId > other.jobId)
        out = 1;
      else
        out = -1;
    else if (taskId != other.taskId)
      if (taskId > other.taskId)
        out = 1;
      else
        out = -1;
    return out;
  }
}
