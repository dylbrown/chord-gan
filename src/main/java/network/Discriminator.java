package network;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.stats.StatsListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import static ui.ChordGAN.NUM_FEATURES;

class Discriminator {
    private static final long[] dLayers = {NUM_FEATURES / 2, NUM_FEATURES / 4};
    private static final double LEARNING_RATE = .1;
    private static final int SEED = 423;
    private final MultiLayerNetwork discriminator;
    private boolean hasBeenInit = false;

    Discriminator(StatsListener statsListener) {
        discriminator = getDiscriminator(true);
        discriminator.init();
        discriminator.setListeners(statsListener);
    }

    public static MultiLayerNetwork getDiscriminator(boolean shouldLearn) {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs((shouldLearn) ? LEARNING_RATE : 0, 0.9))
                .list()
                .layer(new Bidirectional(new LSTM.Builder().activation(Activation.TANH).nIn(NUM_FEATURES).nOut(dLayers[0]).dropOut(.8).build()))
                .layer(new LSTM.Builder().activation(Activation.TANH).nIn(dLayers[0] * 2).nOut(dLayers[1]).dropOut(.8).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .activation(Activation.SIGMOID).nIn(dLayers[1]).nOut(1).build())
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
