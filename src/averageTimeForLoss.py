import sys
import math
import numpy as np
import matplotlib.pyplot as plt

from scipy.optimize import curve_fit

def func(x, a, b, c):
	return a * pow(b,x) + c

# def func(x, a, b):
# 	return a*x + b

ipFiles = [str(sys.argv[i]) for i in range(1, len(sys.argv))]

for ipFile in ipFiles:

	jobDict = {}
	with open(ipFile, 'r') as f:
		line = f.readline()
		while 'JobId' not in line:
			line = f.readline().strip()

		while line:		
			
			if 'JobId' in line:
				parts = line.split()
				jobStats = {}

				jobid = parts[0].split('=')[1]
				
				arrTime = float(parts[1].split('=')[1])
				startTime = float(parts[2].split('=')[1])
				endTime = float(parts[3].split('=')[1])
				numIters = int(parts[4].split('=')[1])

				jobStats['arrTime'] = arrTime
				jobStats['startTime'] = startTime
				jobStats['endTime'] = endTime
				jobStats['numIters'] = numIters

				startTimeList = []
				endTimeList = []
				lossValueList = []

				line = f.readline().strip()
				while 'IterStartTime' in line:
					parts = line.split()
					startTimeList.append(float(parts[0].split('=')[1]))
					endTimeList.append(float(parts[1].split('=')[1]))
					lossValueList.append(float(parts[2].split('=')[1]))
					line = f.readline().strip()

				jobStats['startTimeList'] = startTimeList
				jobStats['endTimeList'] = endTimeList
				jobStats['lossValueList'] = lossValueList

				jobDict[jobid] = jobStats

			else:
				break

	xdata = np.arange(75.0, 95.0, 0.1)
	yDataList = [0.0 for x in xdata]

	for key, value in jobDict.iteritems():
		jobStartTime = value['startTime']
		
		lossValueList = value['lossValueList']
		initialLoss = 1.0

		lossValueReductionList = [(((initialLoss - x) / initialLoss) * 100) for x in lossValueList]

		endTimeList = value['endTimeList']
		timeTakenList = [(x - jobStartTime) for x in endTimeList]

		popt, pcov = curve_fit(func, lossValueReductionList, timeTakenList, maxfev=1000000)

		
		ydata = []
		for x in xdata:
			ydata.append(func(x, *popt))

		if not math.isnan(ydata[0]):
			yDataList = [(yDataList[i] + ydata[i]) for i in range(len(ydata))]

	ydata = np.divide(yDataList, len(jobDict))

	plt.plot(xdata, ydata, label = ipFile)

plt.grid(True)
plt.legend()
plt.show()