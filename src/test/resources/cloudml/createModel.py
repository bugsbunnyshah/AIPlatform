from io.bugsbunny.dataScience.function.model import CreateModel
from io.bugsbunny.dataScience.function.model import InputLayer
from io.bugsbunny.dataScience.function.model import OutputLayer
from org.nd4j.linalg.activations import Activation
from org.nd4j.linalg.lossfunctions import LossFunctions
from org.deeplearning4j.nn.weights import WeightInit

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