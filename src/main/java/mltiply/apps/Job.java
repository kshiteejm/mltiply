package mltiply.apps;

import mltiply.resources.Resources;
import mltiply.utils.Function;
import mltiply.utils.Interval;
import mltiply.utils.SublinearFunction;
import mltiply.utils.SuperlinearFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Job implements Cloneable {

  private static Logger LOG = Logger.getLogger(Job.class.getName());

  public int jobId;
  public int numIterations;
  public int currIterationNum;
  public int resQuota;
  public int currResUse;
  public double serialIterationDuration;
  public Stage currIteration;
  public Function lossFunction;
  public int numTasksUntilNow;
  public List<Task> runnableTasks;
  public List<Task> runningTasks;
  public List<Task> completedTasks;
  public double startTime;
  public double endTime;
  public double jobLossSlope;

  public Job(int jobId, int numIterations) {
    this.jobId = jobId;
    this.numIterations = numIterations;
    currIterationNum = -1;
    jobLossSlope = 0.0;
    Random r = new Random();
    int rn = r.nextInt(2);
    if (rn == 0) {
      lossFunction = SublinearFunction.getRandomSublinearFunction(numIterations);
    } else {
      lossFunction = SuperlinearFunction.getRandomSuperlinearFunction(numIterations);
    }
    resQuota = 0;
    currResUse = 0;
    numTasksUntilNow = 0;
    serialIterationDuration = r.nextInt(91) + 10;
    runnableTasks = new ArrayList<Task>();
    runningTasks = new ArrayList<Task>();
    completedTasks = new ArrayList<Task>();
  }

  public Job clone() {
    Job job = new Job(this.jobId, this.numIterations);
    job.jobLossSlope = this.jobLossSlope;
    job.currIterationNum = this.currIterationNum;
    job.lossFunction = this.lossFunction;
    job.resQuota = this.resQuota;
    job.currResUse = this.currResUse;
    job.numTasksUntilNow = this.numTasksUntilNow;
    job.serialIterationDuration = this.serialIterationDuration;
    job.runnableTasks = new ArrayList<Task>(this.runnableTasks);
    job.runningTasks = new ArrayList<Task>(this.runningTasks);
    job.completedTasks = new ArrayList<Task>(this.completedTasks);
    return job;
  }

  public void initNextIteration() {
    if (isFinished() || resQuota == 0)
      return;

    currIterationNum += 1;
    // currIteration = new Stage(jobId, currIterationNum, serialIterationDuration/resQuota,
    //     1, new Interval(numTasksUntilNow, numTasksUntilNow+resQuota-1));
    // numTasksUntilNow = numTasksUntilNow + resQuota;
    for (int i = 0; i < resQuota; i++) {
      runnableTasks.add(new Task(jobId, currIterationNum, numTasksUntilNow,
          serialIterationDuration/resQuota, 1));
      numTasksUntilNow += 1;
    }
    completedTasks.clear();
  }

  public boolean isFinished() {
    if (currIterationNum < numIterations-1 || !runningTasks.isEmpty()) {
      LOG.log(Level.FINE, "Job " + jobId + ", Current Iteration Num " + currIterationNum +
          ", Total Iterations " + numIterations);
      return false;
    } else {
      LOG.log(Level.FINE, "Job Finished " + jobId);
      return true;
    }
  }

  public boolean isIterationOver() {
    return runnableTasks.isEmpty() && runningTasks.isEmpty() && !isFinished();
  }

  public void scheduleTask(Task task) {
    if (runnableTasks.contains(task)) {
      runnableTasks.remove(task);
      runningTasks.add(task);
      currResUse += task.demands;
    }
  }

  public void completeTask(Task task) {
    if (runningTasks.contains(task)) {
      runningTasks.remove(task);
      completedTasks.add(task);
      currResUse -= task.demands;
    }
  }

  // public int jid; // unique job id
  // public int workers; // number of workers currently assigned to this job
  // public Function loss; // loss as a function of iteration
  // public int iterations; // current SGD iteration number for this job
  // public int iterationsMax; // limit on the maximum number for this iteration
  // public double miniBatchSizeIndicator; // mini-batch size
  // public double schedulingEpoch; // time interval when scheduling decision is taken
  //
  // public Job(int jid, double schedulingEpoch) {
  //   this.jid = jid;
  //   this.workers = 0;
  //   this.iterations = 0;
  //   this.loss = new Function();
  //   this.iterationsMax = 50;
  //   this.miniBatchSizeIndicator = 0.5;
  //   this.schedulingEpoch = schedulingEpoch;
  // }
  //
  // public double getIterationTime() {
  //   return miniBatchSizeIndicator/workers;
  // }
  //
  // public double getLossReduction() {
  //   int numIterations = (int) Math.floor(schedulingEpoch/getIterationTime());
  //   return loss.getDeltaLoss(iterations, iterations + numIterations);
  // }

}
