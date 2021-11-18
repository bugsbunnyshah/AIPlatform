#import jep
#import pandas as pd
from jep import jproxy
from io.bugsbunny.dataScience.function.model import CreateModel
from io.bugsbunny.dataScience.function.model import InputLayer
from io.bugsbunny.dataScience.function.model import OutputLayer
from org.nd4j.linalg.activations import Activation
from org.nd4j.linalg.lossfunctions import LossFunctions
from org.deeplearning4j.nn.weights import WeightInit

#df = pd.DataFrame()
#print(df)
pythonList = ['one', 'two']
javaDeque = jep.jproxy(pythonList, ['java.util.Deque'])

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
print(cloudMlFunction.execute())