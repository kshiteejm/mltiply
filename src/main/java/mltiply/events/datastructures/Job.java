package mltiply.events.datastructures;

import mltiply.utils.Function;
import mltiply.utils.SublinearFunction;
import mltiply.utils.SuperlinearFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Job {

  private static Logger LOG = Logger.getLogger(Job.class.getName());

  public int jobId;
  public int maxIterationNum;
  public int currIterationNum;
  public double serialIterationDuration;
  public Function lossFunction;
  public Resource quanta;

  public double jobLossSlope;
  public double startTime;
  public double endTime;
  public Resource maxAlloc;
  public Resource currAlloc;
  public Resource nextMaxAlloc;
  public int nextTaskId;
  public List<Task> runnableTasks;
  public List<Task> runningTasks;
  public List<Task> completedTasks;

  public Job(int jobId, int maxIterationNum, int numDimensions) {
    Random r = new Random();

    this.jobId = jobId;
    this.maxIterationNum = maxIterationNum;
    currIterationNum = 0;
    serialIterationDuration = r.nextInt(91) + 10;
    int rn = r.nextInt(2);
    if (rn == 0) {
      lossFunction = SublinearFunction.getRandomSublinearFunction(maxIterationNum);
    } else {
      lossFunction = SuperlinearFunction.getRandomSuperlinearFunction(maxIterationNum);
    }
    quanta = new Resource(1.0, numDimensions);

    jobLossSlope = 0.0;
    startTime = -1.0;
    endTime = -1.0;
    maxAlloc = new Resource(numDimensions);
    currAlloc = new Resource(numDimensions);
    nextMaxAlloc = new Resource(numDimensions);
    nextTaskId = 0;
    runnableTasks = new ArrayList<Task>();
    runningTasks = new ArrayList<Task>();
    completedTasks = new ArrayList<Task>();
  }

  public Job(Job j) {
    jobId = j.jobId;
    maxIterationNum = j.maxIterationNum;
    currIterationNum = j.currIterationNum;
    serialIterationDuration = j.serialIterationDuration;
    lossFunction = j.lossFunction;
    quanta = new Resource(j.quanta);

    jobLossSlope = j.jobLossSlope;
    startTime = j.startTime;
    endTime = j.endTime;
    maxAlloc = new Resource(j.maxAlloc);
    currAlloc = new Resource(j.currAlloc);
    nextMaxAlloc = new Resource(j.nextMaxAlloc);
    nextTaskId = j.nextTaskId;
    runnableTasks = new ArrayList<Task>(runnableTasks.size());
    for (Task t: runnableTasks)
      runnableTasks.add(new Task(t));
    runningTasks = new ArrayList<Task>(runningTasks.size());
    for (Task t: runningTasks)
      runningTasks.add(new Task(t));
    completedTasks = new ArrayList<Task>(completedTasks.size());
    for (Task t: completedTasks)
      completedTasks.add(new Task(t));
  }

  /* A job is finished if
   * it reaches iteration limit, and
   * there are no running or runnable tasks for that job
   */
  public boolean isFinished() {
    if (currIterationNum >= maxIterationNum && runningTasks.isEmpty() && runnableTasks.isEmpty()) {
      LOG.log(Level.FINE, "Job Finished " + jobId);
      return true;
    } else {
      LOG.log(Level.FINE, "Job " + jobId + ", Current Iteration Num " + currIterationNum +
          ", Total Iterations " + maxIterationNum);
      return false;
    }
  }

  /* A job iteration is over
   * if there are no runnable and running tasks
   */
  public boolean isIterationOver() {
    return runnableTasks.isEmpty() && runningTasks.isEmpty();
  }

  /* initialize the tasks for the next iteration
   * check if current iteration is finished
   * increment current iteration and
   * spawn runnable tasks for next iteration
   */
  public void initNextIteration() {
    assert(isIterationOver());

    if (isFinished())
      return;

    /* currently based on nextMaxAlloc
     * alternative --> find how many cpu's can be alloc'ed
     * -- to the job by simulated execution
     * -- and then decide num tasks
     * - review -
     */
    currIterationNum += 1;
    int num_tasks = nextMaxAlloc.divide(quanta);
    for (int i = 0; i < num_tasks; i++) {
      runnableTasks.add(new Task(jobId, nextTaskId,
          serialIterationDuration/num_tasks, quanta));
      nextTaskId += 1;
    }
    maxAlloc = new Resource(nextMaxAlloc);
  }

  public void finishTask(Task t) {
    assert(runningTasks.contains(t));
    runningTasks.remove(t);
    completedTasks.add(t);
    currAlloc = currAlloc.minus(t.demand);
  }
}
