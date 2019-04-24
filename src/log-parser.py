import sys
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties

def getFileData(filename):
    groupStartTime = float('inf')
    groupEndTime = 0
    allJobs = []
    numJobs = 0
    sumJct = 0
    with open(filename) as file:
        #Skip the first 3 lines
        for i in range(3):
            file.readline()
        
        line = file.readline()
        while line:
            token = line.split()

            numJobs += 1
            jobStats = {
                "id": int(round(float(token[0].split('=')[1]))),
                "arrival_time": round(float(token[1].split('=')[1]), 4),
                "start_time": round(float(token[2].split('=')[1]), 4),
                "end_time": round(float(token[3].split('=')[1]), 4),
                "num_iterations": int(round(float(token[4].split('=')[1]))),
                "iter_stats": []
            }

            sumJct += (jobStats["end_time"] - jobStats["arrival_time"])

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
        "avgjct": sumJct / numJobs,
        "group_name": filename.replace('.out', '').replace('./logs/', '')
    }

    return groupStats



numArgs = len(sys.argv)
numAlgos = 3
numExperiments = (numArgs - 2) / numAlgos

comparisonAttribute = sys.argv[1]

fig, ax = plt.subplots()
ind = np.arange(numExperiments)
width = 0.2

ax.set_title('Comparison of Average Job Completion Time')

tick_labels = []
equal = []
slaq = []
optimus = []

fileStartIndex = 2
for i in range(fileStartIndex, numArgs):
    filename = sys.argv[i]
    print(filename)
    fileData = getFileData(filename)

    if (comparisonAttribute == 'makespan'):
        val = fileData["end_time"] - fileData["start_time"]
    elif (comparisonAttribute == 'averagejct'):
        val = fileData["avgjct"]
    
    if ((i - fileStartIndex) % numAlgos == 0):
        equal.append(val)
        tick_labels.append(fileData["group_name"].replace("_equal", "").replace("equal_", ""))
    elif ((i - fileStartIndex) % numAlgos == 1):
        slaq.append(val)
    else:
        optimus.append(val)
    
    print("filename ", filename, "total time ", fileData["end_time"] - fileData["start_time"])

g1 = ax.bar(ind, equal, width, color='r')
g2 = ax.bar(ind + width, slaq, width, color='g')
g3 = ax.bar(ind + 2*width, optimus, width, color='b')

fontP = FontProperties()
fontP.set_size('small')


ax.set_xticks(ind + (numAlgos * width)/2)
ax.set_xticklabels(tick_labels)
ax.legend((g1[0], g2[0], g3[0]), ('Equal', 'SLAQ', 'Optimus'), prop=fontP, loc='upper right')
ax.autoscale_view()

plt.xlabel('Job Group Distribution')
plt.ylabel('Time in seconds')


plt.show()
