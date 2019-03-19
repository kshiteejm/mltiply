import java.util.List;

public abstract class InterJobScheduler {
	public abstract void onResourceAvailable(List<GPU> gpu_set);
}