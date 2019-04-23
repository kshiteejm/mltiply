package mlsched.workload;

import mlsched.utils.SublinearFunction;
import mlsched.utils.SuperlinearFunction;
import mlsched.utils.AsynchronousTrainingSpeedFunction;
import mlsched.utils.TrainingSpeedFunction;
import mlsched.scheduler.IntraJobScheduler;
import mlsched.utils.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Job {
	
	public enum State {
		WAITING_FOR_RESOURCES,
		RESOURCE_ALLOCATED,
		RUNNING
	};
	
	private static Logger LOG = Logger.getLogger(Job.class.getName());
	public Integer jobId;
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
	public double predLossRed = 0;
	public TrainingSpeedFunction trainingSpeedFunction;
	public int workerResourceRequirement; // TODO: just considering one resource for now
	public int parameterServerResourceRequirement;
	public int numWorkersAllocated = 0;
	public int parameterServersAllocated = 0;
	public int numWorkersShare = 0;
	public int numParameterServersShare = 0;
	public double marginalGain = 0;
	
	// A job will be submitted by the user. It has to run for n iterations
	// and serialIterationDuration captures the runtime of this task when it
	// runs serially.
	public Job(int jobId, int numIterations, double serialIterationDuration, 
			int maxParallelism, String lossFunction, String synchronicity) {
		this.jobId = jobId;
		this.numIterations = numIterations;
		currIterationNum = 0;
		jobLossSlope = 0.0;
		if (lossFunction.equals("sublinear")) {
			this.lossFunction = SublinearFunction.getRandomSublinearFunction(numIterations);
		} else {
			this.lossFunction = SuperlinearFunction.getRandomSuperlinearFunction(numIterations);
		}
		if (synchronicity.equals("synchronous")) {
			this.trainingSpeedFunction = new AsynchronousTrainingSpeedFunction();
		} else {
//			this.trainingSpeedFunction = new AsynchronousTrainingSpeedFunction();
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
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		
		if(!(o instanceof Job)) {
			return false;
		}
		
		Job j = (Job) o;
		return j.jobId.equals(this.jobId);
	}
}
