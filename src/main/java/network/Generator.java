package network;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.stats.StatsListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Nesterovs;

import static ui.ChordGAN.NUM_FEATURES;

public class Generator {
    private static final double LEARNING_RATE = .1;
    private static final int SEED = 420;
    private final MultiLayerNetwork generator;

    Generator(long latentDimension, StatsListener listener) {
        generator = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(LEARNING_RATE, 0.9))
                .list()
                .layer(0, new LSTM.Builder().activation(Activation.TANH).nIn(latentDimension).nOut((latentDimension + NUM_FEATURES) / 2).dropOut(.8).build())
                .layer(1, new LSTM.Builder().activation(Activation.TANH).nIn((latentDimension + NUM_FEATURES) / 2).nOut(NUM_FEATURES).build())
                .build());
        generator.init();
        //generator.setListeners(listener);
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
