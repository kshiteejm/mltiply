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

  /* A job has
   * jobId - unique job id
   * numIterations - a limit on the total number of iterations after which job finishes
   * currIterationNum - the current iteration number in the range [0, numIterations - 1]
   * resQuota - the InterJobScheduler decided resource quota
   * currResUse - the number of resources currently being used by the job
   * serialIterationDuration - the serial duration of each iteration of the job
   * currIteration -
   * lossFunction - the loss value as a function of iteration number
   * numTasksUntilNow - the total number of tasks the job has until now
   * runnableTasks - the runnable tasks
   * runningTasks - the running tasks
   * startTime - the absolute simulation time at which the job started
   * endTime - the absolute simulation time at which the job ended
   * jobLossSlope - the slope of the loss function at current iteration num
   */
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

  /* job object clone function
   * typically used during deep copy of any datastructure containing a job
   */
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

  /* initialize the tasks for the next iteration
   * check if current iteration is finished
   * increment current iteration and
   * spawn runnable tasks for next iteration
   */
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
    completedTasks.clear(); // is this required?
  }

  /* A job is finished if
   * it reaches iteration limit, and
   * there are no running tasks for that job
   */
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

  /* A job iteration is over
   * if there are no runnable and running tasks
   */
  public boolean isIterationOver() {
    return runnableTasks.isEmpty() && runningTasks.isEmpty();
  }

  public void scheduleTask(Task task) {
    if (runnableTasks.contains(task)) {
      runnableTasks.remove(task);
      runningTasks.add(task);
      currResUse += task.demands;
    }
  }

  public boolean finishTask(Task task) {
    if (runningTasks.contains(task)) {
      runningTasks.remove(task);
      completedTasks.add(task);
      currResUse -= task.demands;
      return true;
    } else {
      return false;
    }
  }

  public void finishTasks(List<Task> tasks) {
    for (Task task: tasks) {
      finishTask(task);
    }
  }

  public double getTotalRunningTime(double currTime) {
    double remainingTime = 0.0;
    double avgIterationTime = serialIterationDuration/resQuota;
    remainingTime = avgIterationTime * (numIterations - currIterationNum + 1);
    return (currTime - startTime + remainingTime);
  }

  public double getFairRunningTime(double fairQuota) {
    double fairTime = 0.0;
    double avgIterationTime = serialIterationDuration/fairQuota;
    fairTime = avgIterationTime * numIterations;
    return fairTime;
  }

  public double getFairnessIndex(double fairQuota, double currTime) {
    return getFairRunningTime(fairQuota)/getTotalRunningTime(currTime);
  }
}
