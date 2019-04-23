import sys

def getFileData(filename):
    groupStartTime = float('inf')
    groupEndTime = 0
    allJobs = []
    with open(filename) as file:
        #Skip the first 3 lines
        for i in range(3):
            file.readline()
        
        line = file.readline()
        while line:
            token = line.split()
        
            jobStats = {
                "id": round(float(token[0].split('=')[1])),
                "arrival_time": round(float(token[1].split('=')[1]), 4),
                "start_time": round(float(token[2].split('=')[1]), 4),
                "end_time": round(float(token[3].split('=')[1]), 4),
                "num_iterations": round(float(token[4].split('=')[1])),
                "iter_stats": []
            }

            if (jobStats["arrival_time"] < groupStartTime):
                groupStartTime = jobStats["arrival_time"]

            if (jobStats["end_time"] > groupEndTime):
                groupEndTime = jobStats["end_time"]
            
            initial_loss = 0
            for i in range(jobStats["num_iterations"]):
                line = file.readline()
                token = line.split()
            
                iterStats = {
                    "start_time": round(float(token[0].split('=')[1]), 4),
                    "end_time": round(float(token[1].split('=')[1]), 4),
                    "loss_value": round(float(token[2].split('=')[1]), 4)
                }

                if i == 0:
                    initial_loss = iterStats["loss_value"]

                iterStats["loss_reduction_percentage"] = ((initial_loss - iterStats["loss_value"]) / initial_loss ) * 100

                jobStats["iter_stats"].append(iterStats)

            allJobs.append(jobStats)
            line = file.readline()

    groupStats = {
        "start_time": groupStartTime,
        "end_time": groupEndTime,
        "group_stats": allJobs,
        "group_name": filename.replace('.out', '').replace('./logs/', '')
    }

    return groupStats



numFiles = len(sys.argv)

for i in range(1, numFiles + 1):
    filename = sys.argv[1]

    fileData = getFileData(filename)
