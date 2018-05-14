package mltiply.apps;

import mltiply.resources.Resources;

public class Task implements Comparable<Task> {

  public int taskId;
  public int stageId;
  public int jobId;
  public double duration;
  public int demands;

  public Task(int jobId, int stageId, int taskId) {
    this.jobId = jobId;
    this.stageId = stageId;
    this.taskId = taskId;
  }

  public Task(int jobId, int stageId, int taskId, double duration, int demands) {
    this.jobId = jobId;
    this.stageId = stageId;
    this.taskId = taskId;
    this.duration = duration;
    this.demands = demands;
  }

  // public Task(int jobId, int stageId, int taskId, double duration, Resources demands) {
  //   this.jobId = jobId;
  //   this.stageId = stageId;
  //   this.taskId = taskId;
  //   this.duration = duration;
  //   this.demands = demands;
  // }

  public Task(Task t) {
    this.jobId = t.jobId;
    this.stageId = t.stageId;
    this.taskId = t.taskId;
    this.duration = t.duration;
    this.demands = t.demands;
  }

  @Override
  public String toString() {
    String output = "<" + this.jobId + ":" + this.stageId + ":" + this.taskId + ">";
    return output;
  }

  public int compareTo(Task other) {
    int out = 0;
    if (jobId != other.jobId)
      if (jobId > other.jobId)
        out = 1;
      else
        out = -1;
    else if (stageId != other.stageId)
      if (stageId > other.stageId)
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
