package mltiply.events.datastructures;

import mltiply.events.simulator.Simulator;
import mltiply.utils.Function;
import mltiply.utils.SublinearFunction;
import mltiply.utils.SuperlinearFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static mltiply.events.simulator.Simulator.AVG_LOAD;
import static mltiply.events.simulator.Simulator.FAIR_KNOB;

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
  public boolean initNextIteration() {
    assert(isIterationOver());

    if (isFinished())
      return false;

    /* currently based on nextMaxAlloc
     * alternative --> find how many cpu's can be alloc'ed
     * -- to the job by simulated execution
     * -- and then decide num tasks
     * - review -
     */
    currIterationNum += 1;
    int num_tasks = (int) Math.floor(nextMaxAlloc.divide(quanta));
    // edge case -> what if num_tasks is zero
    for (int i = 0; i < num_tasks; i++) {
      runnableTasks.add(new Task(jobId, nextTaskId,
          serialIterationDuration/num_tasks, quanta));
      nextTaskId += 1;
    }
    maxAlloc = new Resource(nextMaxAlloc);
    return true;
  }

  public void finishTask(Task t) {
    assert(runningTasks.contains(t));
    runningTasks.remove(t);
    completedTasks.add(t);
    currAlloc = currAlloc.minus(t.demand);
  }

  public void startTask(Task t) {
    assert(runnableTasks.contains(t));
    runnableTasks.remove(t);
    runningTasks.add(t);
    currAlloc = currAlloc.add(t.demand);
  }

  public Resource deficit() {
    return maxAlloc.minus(currAlloc);
  }

  public double expectedAvgShare(Simulator simulator, double time) {
    double clusterCapacity = simulator.cluster.maxAlloc().avgAbsValue();
    double expectedAvgShare = clusterCapacity;
    double fairTime = maxIterationNum*serialIterationDuration*AVG_LOAD / clusterCapacity;
    double remainingIterations = maxIterationNum - currIterationNum;
    if (fairTime > time - startTime)
      if (remainingIterations > 0)
        expectedAvgShare = (remainingIterations * serialIterationDuration)/(fairTime - (time - startTime));
      else
        expectedAvgShare = maxAlloc.avgAbsValue();
    return expectedAvgShare;
  }

  public double fairnessScore(Simulator simulator, double time) {
    double fairnessScore = Double.MAX_VALUE;
    double expectedAvgShare = expectedAvgShare(simulator, time);
    double remainingIterations = maxIterationNum - currIterationNum;
    double clusterCapacity = simulator.cluster.maxAlloc().avgAbsValue();
    if (!(Resource.equals(expectedAvgShare, clusterCapacity) || remainingIterations <= 1))
      fairnessScore = 1/((1 - expectedAvgShare/clusterCapacity) * (remainingIterations - 1) * serialIterationDuration);

    /*
    double fairTime = maxIterationNum*serialIterationDuration/AVG_LOAD;
    double estimatedTime = 0.0;
    int estimatedNextResource = nextMaxAlloc.divide(quanta);
    if (estimatedNextResource == 0) {
      fairnessScore = 1.0;
    } else {
      estimatedTime += ((maxIterationNum - currIterationNum) * serialIterationDuration / estimatedNextResource) + time - startTime;
      if (estimatedTime > 0.0) {
        fairnessScore = estimatedTime / fairTime;
        fairnessScore = Math.pow(Math.E, -fairTime / fairnessScore); // fairTime/fairnessScore needs to be normalized
      }
    }
    */

    return fairnessScore;
  }

  public double qualityScore() {
    double qualityScore = 0.0;
    qualityScore = lossFunction.getDeltaValue(currIterationNum); // ranges from 1.0 to 0.0
    return qualityScore;
  }

  public double mltiplyScore(Simulator simulator, double time) {
    double fairnessKnob = 0.0;
    for (Job job: simulator.runningJobs.values()) {
      fairnessKnob += job.expectedAvgShare(simulator, time);
    }
    if (!Resource.equals(fairnessKnob, 0.0))
      fairnessKnob /= simulator.cluster.maxAlloc().avgAbsValue();
    fairnessKnob = Math.min(fairnessKnob, 1.0);
    double mltiplyScore = fairnessScore(simulator, time);
    if (!Resource.equals(mltiplyScore, Double.MAX_VALUE))
      mltiplyScore = fairnessKnob*fairnessScore(simulator, time) + (1.0-fairnessKnob)*qualityScore();
    return mltiplyScore;
  }
}
