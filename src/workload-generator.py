import sys
import argparse
import json
import numpy as np
import scipy.stats

parser = argparse.ArgumentParser()

parser.add_argument('-p', '--prob', type=str,
	choices=['uniform', 'normal', 'poisson', 'zipf', 'pareto'],
	default='uniform', 
	help='probability distribution to use for arrival time generation')

parser.add_argument('-sp', '--policy', type=str,
	choices=['slaq', 'optimus', 'equal'],
	default='equal', 
	help='scheduling policy to use')

parser.add_argument('-g', '--ngpu', type=int,
	default='80',
	help='total number of job groups')

parser.add_argument('-r', '--seed', type=int,
	default='7',
	help='seed to use for random number generation')

parser.add_argument('-t', '--total', type=int,
	default='40',
	help='total number of job groups')

parser.add_argument('-j', '--jobspg', type=int,
	default='5',
	help='jobs per job group')

parser.add_argument('-o', '--out', type=str,
	default='out.json',
	help='output json file')

parser.add_argument('-s', '--start', type=float,
	default='0.0',
	help='start time for arrival time generation')

parser.add_argument('-e', '--end', type=float,
	default='100.0',
	help='end time for arrival time generation')

parser.add_argument('-n', '--iter', type=int,
	default='10',
	help='total iterations for one job')

parser.add_argument('-d', '--dur', type=float,
	default='20',
	help='serial iteration duration for one job')

parser.add_argument('-w', '--worker', type=int,
	default='1',
	help='worker resource requirement for one job')

parser.add_argument('-ps', '--paramserver', type=int,
	default='1',
	help='parameter server resource requirement for one job')

args = parser.parse_args()

totalJobGroups = args.total
jobsPerGroup = args.jobspg
nIterations = args.iter
serialIterationDuration = args.dur
workerResourceRequirement = args.worker
parameterServerResourceRequirement = args.paramserver

outFile = args.out

probDistribution = args.prob
startTime = args.start
endTime = args.end

# funcName = 'np.random.%s' % (probDistribution)

if probDistribution == 'uniform':
	times = np.random.uniform(startTime, endTime, totalJobGroups)
elif probDistribution == 'normal':
	times = np.random.normal(loc=(startTime + endTime) / 2.0, size=totalJobGroups)
elif probDistribution == 'poisson':
	times = np.random.poisson((startTime + endTime) / 2.0, totalJobGroups)
elif probDistribution == 'zipf':
	times = np.random.zipf(1.1, totalJobGroups)
elif probDistribution == 'pareto':
	times = scipy.stats.pareto.rvs(0.1, size=totalJobGroups)

jobs = []
for time in times:
	job = {}
	job['numJobs'] = jobsPerGroup
	job['arrivalTime'] = time
	job['numIterations'] = nIterations
	job['serialIterDuration'] = serialIterationDuration
	job['synchronicity'] = 'synchronous'
	job['workerResourceRequirement'] = workerResourceRequirement
	job['parameterServerResourceRequirement'] = parameterServerResourceRequirement

	r = np.random.uniform(0.0, 1.0)
	if r < 0.5:
		job['lossFunction'] = 'superlinear'
	else:
		job['lossFunction'] = 'sublinear'

	jobs.append(job)

ngpu = args.ngpu
seed = args.seed
policy = args.policy

jsonDict = {}
jsonDict['seed'] = seed
jsonDict['numGPUs'] = ngpu
jsonDict['schedPolicy'] = policy
jsonDict['jobs'] = jobs

with open(outFile, 'w') as file:
    json.dump(jsonDict, file)
