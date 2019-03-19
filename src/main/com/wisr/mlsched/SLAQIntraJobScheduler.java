import java.util.List;

import org.json.simple.JSONObject;

public class SLAQIntraJobScheduler extends IntraJobScheduler {

	public SLAQIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public void prepareBid(List<GPU> offeredGPUs) {
		// TODO: Implement
	}
	
}