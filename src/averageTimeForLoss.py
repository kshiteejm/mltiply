import sys

ipFile = str(sys.argv[1])

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


for key, value in jobDict.iteritems():
	jobStartTime = value['startTime']
	lossValueList = value['lossValueList']
	initialLoss = lossValueList[0]

	lossValueReductionList = [(((initialLoss - x) / initialLoss) * 100) for x in lossValueList]
	
	print lossValueReductionList