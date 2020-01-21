package network;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.conf.preprocessor.RnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

class Discriminator {
    private static final long[] dLayers = {35, 50};
    private static final double LEARNING_RATE = .1;
    private static final int SEED = 42;
    private final MultiLayerNetwork discriminator;
    private boolean hasBeenInit = false;

    Discriminator() {
        discriminator = getDiscriminator(true);
        discriminator.init();
    }

    public static MultiLayerNetwork getDiscriminator(boolean shouldLearn) {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs((shouldLearn) ? LEARNING_RATE : 0, 0.9))
                .list()
                .layer(new Bidirectional(new LSTM.Builder().activation(Activation.TANH).nIn(13).nOut(dLayers[0]).build()))
                .layer(new DenseLayer.Builder().nIn(dLayers[0]).nOut(dLayers[1])
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .activation(Activation.SIGMOID)
                        .nIn(dLayers[1]).nOut(1).build())
                .inputPreProcessor(1, new RnnToFeedForwardPreProcessor())
                .build());
    }

    INDArray getParams(int i) {
        return discriminator.getLayer(i).params();
    }

    Model[] getLayers() {
        return discriminator.getLayers();
    }

    void init(GeneratorTrainer gan, int startingIndex) {
        if(hasBeenInit) return;
        for(int i=startingIndex; i< startingIndex + discriminator.getLayers().length; i++) {
            discriminator.getLayers()[i-startingIndex].setParams(gan.getParams(i));
        }
        hasBeenInit = true;
    }

    void fit(DataSet data) {
        discriminator.fit(data);
    }
}
