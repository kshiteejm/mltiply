package mlsched.workload;

import mlsched.utils.SublinearFunction;
import mlsched.utils.SuperlinearFunction;
import mlsched.utils.Function;

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
	public int resAllocated;
	public int currResUse;
	public double serialIterationDuration;
	public Function lossFunction;
	public int numTasksUntilNow;
	public List<Task> runnableTasks;
	public List<Task> runningTasks;
	public List<Task> completedTasks;
	public double startTime;
	public double endTime;
	public double jobLossSlope;

	// A job will be submitted by the user. It has to run for n iterations
	// and serialIterationDuration captures the runtime of this task when it
	// runs serially.
	public Job(int jobId, int numIterations, double serialIterationDuration) {
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
		resAllocated = 0;
		currResUse = 0;
		numTasksUntilNow = 0;
		endTime = 0.0;
		this.serialIterationDuration = serialIterationDuration;
		runnableTasks = new ArrayList<Task>();
		runningTasks = new ArrayList<Task>();
		completedTasks = new ArrayList<Task>();
	}

	/*
	 * job object clone function typically used during deep copy of any data
	 * structure containing a job
	 */
	public Job clone() {
		Job job = new Job(this.jobId, this.numIterations, this.serialIterationDuration);
		job.jobLossSlope = this.jobLossSlope;
		job.currIterationNum = this.currIterationNum;
		job.lossFunction = this.lossFunction;
		job.resAllocated = this.resAllocated;
		job.currResUse = this.currResUse;
		job.numTasksUntilNow = this.numTasksUntilNow;
		job.runnableTasks = new ArrayList<Task>(this.runnableTasks);
		job.runningTasks = new ArrayList<Task>(this.runningTasks);
		job.completedTasks = new ArrayList<Task>(this.completedTasks);
		return job;
	}
}
