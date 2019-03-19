/**
 * Representation of the cluster's configuration
 */
public class ClusterConfiguration {
	int mRacks;
	int mMachinesPerRack;
	int mSlotsPerMachine;
	int mGPUsPerSlot;
	
	public ClusterConfiguration(int racks, int machines_per_rack,
			int slots_per_machine, int gpus_per_slot) {
		mRacks = racks;
		mMachinesPerRack = machines_per_rack;
		mSlotsPerMachine = slots_per_machine;
		mGPUsPerSlot = gpus_per_slot;
	}
}