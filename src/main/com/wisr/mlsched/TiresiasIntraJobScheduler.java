import java.util.List;

import org.json.simple.JSONObject;

public class TiresiasIntraJobScheduler extends IntraJobScheduler {

	public TiresiasIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public void prepareBid(List<GPU> offeredGPUs) {
		// TODO: Implement
	}
	
}