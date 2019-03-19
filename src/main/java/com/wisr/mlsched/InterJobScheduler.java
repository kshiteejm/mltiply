import java.util.List;

/**
 * Interface for InterJobScheduler types
 */
public abstract class InterJobScheduler {
	public abstract void onResourceAvailable(List<GPU> gpu_set);
}