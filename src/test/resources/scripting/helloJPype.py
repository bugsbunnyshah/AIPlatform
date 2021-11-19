# Boiler plate stuff to start the module
import jpype
import jpype.imports
from jpype.types import *
import pandas as pd
from java.util import Random
from com.appgallabs.dataScience.function.model import CreateModel
from com.appgallabs.dataScience.function.model import InputLayer
from com.appgallabs.dataScience.function.model import OutputLayer
from org.nd4j.linalg.activations import Activation
from org.nd4j.linalg.lossfunctions import LossFunctions
from org.deeplearning4j.nn.weights import WeightInit

df = pd.DataFrame()

inputLayer = InputLayer(Activation.RELU.name());
outputLayer = OutputLayer(Activation.SOFTMAX.name(),
                          LossFunctions.LossFunction.SQUARED_LOSS.name());
inputLayers = [inputLayer]

outputLayers = [outputLayer]

cloudMlFunction = CreateModel(123,
                              0.008,
                              1,WeightInit.XAVIER.name(),
                              inputLayers,
                              outputLayers
                              )
model = cloudMlFunction.execute()