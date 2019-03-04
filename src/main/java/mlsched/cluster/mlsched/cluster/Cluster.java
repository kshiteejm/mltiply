package mlsched.cluster;

public class Cluster {
	public int numGPUs;
	public int availableGPUs;
	
	public Cluster(int numGPUs) {
		this.numGPUs = numGPUs;
		this.availableGPUs = numGPUs;
	}
}