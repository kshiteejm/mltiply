package mlsched.workload;

import java.io.FileReader;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import mlsched.cluster.Cluster;
import mlsched.events.JobArrived;
import mlsched.scheduler.EqualShareScheduler;
import mlsched.scheduler.SLAQ;
import mlsched.simulator.Main;

public class Workload {
	
	public static void parseWorkload (String Filename) {
		
		try {
			
			Object obj = new JSONParser().parse(new FileReader(Filename));
			JSONObject jo = (JSONObject) obj;
			
			Main.randSeed = jo.get("seed") != null ? 
					 Long.valueOf(jo.get("seed").toString()) : System.currentTimeMillis();
			
			Main.cluster = new Cluster(Integer.valueOf(jo.get("numGPUs").toString()));
			
			String policy = jo.get("schedPolicy").toString();
			
			switch(policy) {
			
				case "equal": Main.interJobScheduler = new EqualShareScheduler(false);  
					break;
					
				case "slaq": Main.interJobScheduler = new SLAQ(true);
					Main.schedulingInterval = jo.get("schedulingInterval") != null ? 
							 Double.valueOf(jo.get("schedulingInterval").toString()) : 2;
					break;
					
				default: System.out.println("Unsupported Scheduling Policy!"); 
					System.exit(-1);
			}
			
			Main.epochScheduling = Main.interJobScheduler.schedulingEpoch;
					
			JSONArray jobs = (JSONArray) jo.get("jobs"); 
			Iterator jobItr = jobs.iterator();
			
			Integer jobId = 0, maxParallel = Integer.MAX_VALUE;
			
			while(jobItr.hasNext()) {
				JSONObject job = (JSONObject) jobItr.next();
				
				int numJobs = Integer.valueOf(job.get("numJobs").toString());
				double jobArrivalTime = Double.valueOf(job.get("arrivalTime").toString());
				int numIter = Integer.valueOf(job.get("numIterations").toString());
				double serialRun = Double.valueOf(job.get("serialIterDuration").toString());
				String lossFunction = job.get("lossFunction").toString();
				
				for(int i = 0; i < numJobs; i++) {
					Job j = new Job(jobId, numIter, serialRun, maxParallel, lossFunction);
					
					System.out.println("JobID = " + j.jobId + " lossFunction = " + lossFunction);
					
					Main.eventQueue.add(new JobArrived(jobArrivalTime, j));
					jobId += 1;
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
