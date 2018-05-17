import sys
from MNISTNetwork import MNISTNetwork


if __name__ == '__main__':

    net = MNISTNetwork()
    net.readDataset()
    net.buildModel()

    net.train(epochs=10)
    net.saveModelToDisk()