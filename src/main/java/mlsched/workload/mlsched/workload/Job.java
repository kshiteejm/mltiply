package mlsched.workload;

import mlsched.utils.SublinearFunction;
import mlsched.utils.SuperlinearFunction;
import mlsched.scheduler.IntraJobScheduler;
import mlsched.utils.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Job {
	
	public enum State {
		WAITING_FOR_RESOURCES,
		RUNNING
	};
	
	private static Logger LOG = Logger.getLogger(Job.class.getName());
	public int jobId;
	public int numIterations;
	public int currIterationNum;
	public int logicalFairShare;
	public int currIterAllocation;
	public int nextIterAllocation;
	public double serialIterationDuration;
	public Function lossFunction;
	public int numTasksUntilNow;
	public List<Task> runnableTasks;
	public List<Task> runningTasks;
	public List<Task> completedTasks;
	public double startTime;
	public double endTime;
	public double jobLossSlope;
	public IntraJobScheduler intraJobScheduler;
	public int maxParallelism;
	public int minParallelism = 1;
	public State jobState = State.WAITING_FOR_RESOURCES;
	
	// A job will be submitted by the user. It has to run for n iterations
	// and serialIterationDuration captures the runtime of this task when it
	// runs serially.
	public Job(int jobId, int numIterations, double serialIterationDuration, int maxParallelism) {
		this.jobId = jobId;
		this.numIterations = numIterations;
		currIterationNum = 0;
		jobLossSlope = 0.0;
		Random r = new Random();
		int rn = r.nextInt(2);
		if (rn == 0) {
			lossFunction = SublinearFunction.getRandomSublinearFunction(numIterations);
		} else {
			lossFunction = SuperlinearFunction.getRandomSuperlinearFunction(numIterations);
		}
		logicalFairShare = 0;
		currIterAllocation = 0;
		nextIterAllocation = 0;
		numTasksUntilNow = 0;
		endTime = 0.0;
		this.serialIterationDuration = serialIterationDuration;
		runnableTasks = new ArrayList<Task>();
		runningTasks = new ArrayList<Task>();
		completedTasks = new ArrayList<Task>();
		this.intraJobScheduler = new IntraJobScheduler();
		this.maxParallelism = maxParallelism;
	}
}
