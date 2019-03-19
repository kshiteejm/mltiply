import org.json.simple.JSONObject;

/**
 * Event abstraction for Job Arrival
 */
public class JobArrivalEvent extends ClusterEvent {

	private JSONObject mJobConfiguration; // Configuration
	public JobArrivalEvent(long timestamp, JSONObject configuration) {
		super(timestamp);
		mJobConfiguration = configuration;
		setPriority(ClusterEvent.EventType.JOB_ARRIVAL);
	}

	@Override
	public void handleEvent() {
		IntraJobScheduler job = IntraJobSchedulerFactory.createInstance(mJobConfiguration, 
				Cluster.getInstance().getPolicy());
		Cluster.getInstance().addJob(job);
	}
}