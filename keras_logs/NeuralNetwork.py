from __future__ import print_function, division


import numpy as np
import tensorflow as tf
import keras
from keras import backend as K

from LossLogger import LossLogger

class NeuralNetwork(object):
    '''An abstract Neural Network class.'''

    def __init__(self, inputDimensions, labels, jsonFile=None, weightFile=None, logFile=None):

        self.model = None
        self.inputDimensions = inputDimensions
        self.labels = labels
        self.jsonFile = jsonFile
        self.weightFile = weightFile

        self.x_train = None
        self.y_train = None
        self.x_test = None
        self.y_test = None
        self.optimizer = None

        self.logFile = logFile
        self.LossLogger = LossLogger(self.logFile)


    def loadModelFromFile(self, **kwargs):
        jsonFile = kwargs.get('jsonFile', self.jsonFile)
        weightFile = kwargs.get('weightFile', self.weightFile)
        self.jsonFile, self.weightFile = jsonFile, weightFile
        with open(jsonFile, "r") as f:
            self.model = keras.models.model_from_json(f.read())
        self.model.load_weights(weightFile)
        self.model.summary()

    def saveModelToDisk(self, **kwargs):
        jsonFile = kwargs.get('jsonFile', self.jsonFile)
        weightFile = kwargs.get('weightFile', self.weightFile)
        self.jsonFile, self.weightFile = jsonFile, weightFile
        with open(jsonFile, "w") as f:
            print(self.model.to_json(), file=f)
        self.model.save_weights(weightFile)
        print("Saved to disk!")

    def buildModel(self):
        raise NotImplementedError("Please use a concrete NeuralNetwork class!")

    def compileModel(self, **kwargs):
        raise NotImplementedError("Please use a concrete NeuralNetwork class!")

    def readDataset(self):
        raise NotImplementedError("Please use a concrete NeuralNetwork class!")

    def train(self, **kwargs):
        x_train = kwargs.get('x_train', self.x_train)
        y_train = kwargs.get('y_train', self.y_train)
        kwargs.pop('x_train', None)
        kwargs.pop('y_train', None)
        self.x_train, self.y_train = x_train, y_train
        self.model.fit(x_train, y_train, callbacks=[self.LossLogger], **kwargs)
        print('Model Trained!')
 
    def test(self, **kwargs):
        x_test = kwargs.get('x_test', self.x_test)
        y_test = kwargs.get('y_test', self.y_test)
        kwargs.pop('x_test', None)
        kwargs.pop('y_test', None)
        self.x_test, self.y_test = x_test, y_test
        self.model.evaluate(x_test, y_test, **kwargs)

    def checkLayerIndex(self, l):
        if l >= len(self.model.layers):
            raise IndexError("%d out of bounds"%l)

        if isinstance(l, keras.layers.core.Activation):
            raise ValueError("Invalid layer index, layer %d is of type %s!"%
                             (l, type(self.model.layers[l])))

    def getWeights(self, l):
        self.checkLayerIndex(l)
        return self.model.layers[l].get_weights()

    def getLayerType(self, l):
        self.checkLayerIndex(l)
        return type(self.model.layers[l])

    def getActivation(self, inputImages, l):
        self.checkLayerIndex(l)
        get_activation = K.function((self.model.layers[0].input, K.learning_phase()),
                                    (self.model.layers[l].output,))
        activations = get_activation([inputImages, 0])
        return activations

    def getPrediction(self, inputImage):
        K.set_learning_phase(1)
        probs = self.model.predict(inputImage)
        labelIdx = int(np.argmax(probs))
        return self.labels[labelIdx], probs[labelIdx]

    def getConfig(self):
        conf = self.model.get_config()
        return [(i, d['class_name'] if d['class_name'] != 'Activation'
                 else d['config']['activation']) for i, d in enumerate(conf)]

    def getTestExample(self, idx):
        if not hasattr(self, 'x_test'):
            self.readDataset()
        return self.x_test[idx], self.y_test[idx]
        # return np.expand_dims(self.x_test[idx], axis=0), np.expand_dims(self.y_test[idx], axis=0)
    
    def getGradient(self, x, y):
        raise NotImplementedError('Please use a concrete NeuralNetwork class!')

    def getAdverserialExamples(self, ip, label):
        raise NotImplementedError('Please use a concrete NeuralNetwork class!')
