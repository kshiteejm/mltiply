import sys
import keras

from MNISTNetwork import MNISTNetwork


if __name__ == '__main__':

    net = MNISTNetwork(weightFile='mnist_adam.h5', jsonFile='mnist_adam.json', logFile='mnist_adam.txt')
    net.optimizer = 'adam'
    net.readDataset()
    net.buildModel()
    net.train(epochs=100)
    net.saveModelToDisk()

    net = MNISTNetwork(weightFile='mnist_sgd.h5', jsonFile='mnist_sgd.json', logFile='mnist_sgd.txt')
    net.optimizer = keras.optimizers.SGD(lr=0.01) #CAN VARY!
    net.readDataset()
    net.buildModel()
    net.train(epochs=100)
    net.saveModelToDisk()

    net = MNISTNetwork(weightFile='mnist_sgd_mom.h5', jsonFile='mnist_sgd_mom.json', logFile='mnist_sgd_mom.txt')
    net.optimizer = keras.optimizers.SGD(lr=0.01, momentum=0.005) #CAN VARY!
    net.readDataset()
    net.buildModel()
    net.train(epochs=100)
    net.saveModelToDisk()

    net = MNISTNetwork(weightFile='mnist_nest.h5', jsonFile='mnist_nest.json', logFile='mnist_nest.txt')
    net.optimizer = keras.optimizers.SGD(lr=0.01, momentum=0.005, nesterov=True) #CAN VARY!
    net.readDataset()
    net.buildModel()
    net.train(epochs=100)
    net.saveModelToDisk()
    