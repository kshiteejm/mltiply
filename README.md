# To compile
mvn compile

# To Run
mvn exec:java -Dexec.mainClass="com.wisr.mlsched.Simulation" -Dexec.args="<cluster_config_file> <workload_config_file>"
