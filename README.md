# To compile
mvn compile

# To setup cluster configuration
python scripts/generate_cluster_config.py --racks <num_racks> --machines <num_machines_per_rack> --slots <num_slots_per_machine> --gpus <num_gpus_per_slot> --policy < Policy:Themis/SLAQ/Gandiva/Tiresias > --lease_time <lease_time> --config_file <config_file_name_to_create> \
This will create the cluster configuration file under configuration/cluster/

# To generate a custom workload configuration
python scripts/generate_workload_config.py --workload <model_name>:<number_instances>,<model_name>:<number_instances>.. --workload_rate <inter-arrival-rate> --config_file <config_file_name_to_create> \
This will create the workload configuration file under configuration/jobs/ \
List of model names can be found under configuration/models/. Refer to any of the existing files as a template to create a new model charactertic
  
# To generate model configuration from traces
python scripts/generate_models_from_trace.py \
Make sure that all model name present in the traces have a correpsonding json file in configuration/model_stats/

# To generate workload configuration from traces
python scripts/generate_workloads_from_trace.py

# To Run
mvn exec:java -Dexec.mainClass="com.wisr.mlsched.Simulation" -Dexec.args="<cluster_config_file> <workload_config_file>"
