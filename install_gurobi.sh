cd /nobackup 
wget https://packages.gurobi.com/8.0/gurobi8.0.1_linux64.tar.gz
tar -xvzf gurobi8.0.1_linux64.tar.gz
export GUROBI_HOME=/nobackup/gurobi801/linux64
export PATH=$PATH:$GUROBI_HOME/bin
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${GUROBI_HOME}/lib
export GRB_LICENSE_FILE=/nobackup/gurobi801/gurobi.lic
cd /nobackup/gurobi801/linux64/lib
mvn install:install-file -Dfile=gurobi.jar -DgroupId=com.gurobi -DartifactId=gurobi-jar -Dversion=7.0.2 -Dpackaging=jar
