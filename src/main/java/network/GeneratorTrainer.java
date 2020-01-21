package network;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Nesterovs;

class GeneratorTrainer {
    private static final long SEED = 42;
    private static final double LEARNING_RATE = .1;
    private MultiLayerNetwork gan;
    private final int gLength;

    GeneratorTrainer(Discriminator realDiscriminator, Generator generator) {
        MultiLayerNetwork discriminator = Discriminator.getDiscriminator(false);
        discriminator.init();
        gLength = generator.getLayers().length;
        int totalLayers = gLength + discriminator.getLayers().length;

        org.deeplearning4j.nn.conf.layers.Layer[] layerConfigs = new org.deeplearning4j.nn.conf.layers.Layer[totalLayers];
        for(int i=0; i < totalLayers; i++) {
            if(i < gLength) {
                layerConfigs[i] = generator.getLayers()[i].conf().getLayer();
            }else{
                layerConfigs[i] = discriminator.getLayers()[i-gLength].conf().getLayer();
            }
        }
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(LEARNING_RATE, 0.9))
                .list(layerConfigs)
                .build();
        gan = new MultiLayerNetwork(config);
        gan.init();
        realDiscriminator.init(this, gLength);
        generator.updateFrom(this);
    }

    INDArray getParams(int i) {
        return gan.getLayer(i).params();
    }

    void updateFrom(Discriminator discriminator) {
        for(int i = gLength; i < gan.getLayers().length; i++) {
            gan.getLayer(i).setParams(discriminator.getParams(i - gLength));
        }
    }

    void fit(DataSet data) {
        gan.fit(data);
    }
}
