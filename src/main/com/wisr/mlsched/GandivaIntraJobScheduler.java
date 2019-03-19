import java.util.List;

import org.json.simple.JSONObject;

public class GandivaIntraJobScheduler extends IntraJobScheduler {

	public GandivaIntraJobScheduler(JSONObject config) {
		super(config);
	}

	@Override
	public void prepareBid(List<GPU> offeredGPUs) {
		// TODO: Implement
	}
	
}