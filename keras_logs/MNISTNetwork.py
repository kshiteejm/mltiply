#!/usr/bin/env python

from __future__ import print_function

import numpy as np
import tensorflow as tf
import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten
from keras.layers import Conv2D, MaxPooling2D
from keras.datasets import mnist
from keras.constraints import max_norm

import keras.backend as K


from NeuralNetwork import NeuralNetwork

class MNISTNetwork(NeuralNetwork):
    """An MNIST ConvNet Class."""
    def __init__(self, inputDimensions=(28, 28, 1), labels=map(str, range(10)),
                 jsonFile="mnist.json", weightFile="mnist.h5", logFile='mnist_log.txt'):

        super(MNISTNetwork, self).__init__(inputDimensions, labels, jsonFile=jsonFile,
                                           weightFile=weightFile, logFile=logFile)

        # Number of Filters in Conv Layer
        self.numFilters = 32
        # Size of pooling area for max pooling
        self.poolSize = 2
        # Convolution kernel size
        self.filterSize = 3

        self.numClasses = 10

        self.loss = "categorical_crossentropy"
        self.optimizer = "adam"
        self.metrics = None

        self.jsonFile = jsonFile
        self.weightFile = weightFile

    def readDataset(self):
        """Read the data, shuffle and split it between train and test sets."""
        (x_train, y_train), (x_test, y_test) = mnist.load_data()

        x_train = x_train.reshape(x_train.shape[0], *self.inputDimensions)
        print(x_train.shape[0], self.inputDimensions)
        x_test = x_test.reshape(x_test.shape[0], *self.inputDimensions)
        x_train = x_train.astype("float32")
        x_test = x_test.astype("float32")
        x_train /= 255
        x_test /= 255
        print("x_train shape:", x_train.shape)
        print(x_train.shape[0], "train samples")
        print(x_test.shape[0], "test samples")


        # convert class vectors to binary class matrices
        y_train = keras.utils.to_categorical(y_train, self.numClasses)
        y_test = keras.utils.to_categorical(y_test, self.numClasses)

        self.x_train, self.y_train, self.x_test, self.y_test = x_train, y_train, x_test, y_test

    def buildModel(self):
        """Define neural network model"""
        self.model = Sequential()

        self.model.add(Conv2D(self.numFilters, (self.filterSize, self.filterSize),
                              padding="valid",
                              input_shape=self.inputDimensions))
        self.model.add(Activation("relu"))
        self.model.add(Conv2D(self.numFilters, (self.filterSize, self.filterSize)))
        self.model.add(Activation("relu"))
        self.model.add(MaxPooling2D(pool_size=(self.poolSize, self.poolSize)))
        self.model.add(Dropout(0.25))

        self.model.add(Flatten())
        self.model.add(Dense(128,))
        self.model.add(Activation("relu"))
        self.model.add(Dropout(0.5))
        self.model.add(Dense(self.numClasses))
        self.model.add(Activation("softmax"))

        self.compileModel()

        self.model.summary()

    def compileModel(self, **kwargs):
        loss = kwargs.get('loss', self.loss)
        optimizer = kwargs.get('opt', self.optimizer)
        metrics = kwargs.get('metrics', self.metrics)
        for q in ('loss', 'opt', 'metrics'):
            kwargs.pop(q, None)
        self.model.compile(loss=loss, optimizer=optimizer, metrics=metrics, **kwargs)

    def getGradient(self, x, y):
        if not hasattr(self, 'get_grads'):
            self.get_grads = K.function(inputs=[self.model.inputs[0], self.model.outputs[0],
                                        self.model.targets[0], self.model.sample_weights[0],
                                        K.learning_phase()], 
                                outputs=keras.backend.gradients(self.model.total_loss, 
                                                                self.model.trainable_weights))
        
        return self.get_grads([x, self.model.predict(x), np.expand_dims(y, axis=0),
                               np.ones(10), 1])

    def getAdverserialExamples(self, ip, label):
        pass
