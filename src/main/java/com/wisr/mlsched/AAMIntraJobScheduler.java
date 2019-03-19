import java.util.List;

import org.json.simple.JSONObject;

public class AAMIntraJobScheduler extends IntraJobScheduler {

	public AAMIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public void prepareBid(List<GPU> offeredGPUs) {
		// TODO: Implement
	}
	
}