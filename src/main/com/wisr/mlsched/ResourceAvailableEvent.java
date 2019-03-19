import java.util.List;

public class ResourceAvailableEvent extends ClusterEvent {

	private List<GPU> mResources;
	public ResourceAvailableEvent(long timestamp, List<GPU> resources) {
		super(timestamp);
		mResources = resources;
		setPriority(ClusterEvent.EventType.RESOURCE_AVAILABLE);
	}

	@Override
	public void handleEvent() {
		Cluster.getInstance().getScheduler().onResourceAvailable(mResources);
	}
	
}