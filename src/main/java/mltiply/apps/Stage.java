package mltiply.apps;

import mltiply.resources.Resources;
import mltiply.utils.Interval;

public class Stage {

  public int stageId;
  public int jobId;
  public double duration;
  public int demands;
  public Interval tasks;

  public Stage(int jobId, int stageId, double duration, int demands, Interval tasks) {
    this.jobId = jobId;
    this.stageId = stageId;
    this.duration = duration;
    this.demands = demands;
    this.tasks = new Interval(tasks.begin, tasks.end);
  }

}
