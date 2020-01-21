package network;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class Generator {
    private final long latentDimension;
    private static final long[] gLayers = {35, 50};
    private static final double LEARNING_RATE = .1;
    private static final int SEED = 42;
    private final MultiLayerNetwork generator;

    Generator(long latentDimension) {
        this.latentDimension = latentDimension;
        generator = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(LEARNING_RATE, 0.9))
                .list()
                .layer(0, new LSTM.Builder().activation(Activation.TANH).nIn(latentDimension).nOut(gLayers[0]).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).nIn(gLayers[0]).nOut(13).build())
                .build());
        generator.init();
    }

    Model[] getLayers() {
        return generator.getLayers();
    }

    void updateFrom(GeneratorTrainer gTrainer) {
        for(int i=0; i < getLayers().length; i++) {
            getLayers()[i].setParams(gTrainer.getParams(i));
        }
    }

    INDArray generate(INDArray rand) {
        return generator.output(rand);
    }
}
