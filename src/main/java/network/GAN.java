package network;

import org.deeplearning4j.ui.stats.StatsListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import ui.ChordGAN;

import java.util.Arrays;

public class GAN {
    private static final int D_STEPS = 1;
    private final long latentDimension;
    private Discriminator discriminator;
    private Generator generator;
    private GeneratorTrainer gTrainer;
    private boolean firstTime = true;

    public GAN(long latentDimension, StatsListener statsListener, StatsListener listener) {
        this.latentDimension = latentDimension;
        discriminator = new Discriminator(statsListener);
        generator = new Generator(latentDimension, listener);
        gTrainer = new GeneratorTrainer(discriminator, generator);
    }

    public void train(DataSetIterator iterator) {
        iterator.reset();
        while (iterator.hasNext()) {
            DataSet dataset = iterator.next();
            INDArray real = Nd4j.rand(new long[]{dataset.numExamples(), 1, ChordGAN.SONG_SIZE}).div(10.0);
            dataset.setLabels(real);
            INDArray maskOut = Nd4j.zeros(dataset.numExamples(), ChordGAN.SONG_SIZE-1);
            INDArray lastLabel = Nd4j.ones(dataset.numExamples(), 1);
            INDArray outMask = Nd4j.hstack(maskOut, lastLabel);
            dataset.setLabelsMaskArray(outMask);
            train(dataset, outMask);
        }
    }

    private void train(DataSet dataSet, INDArray outMask) {
        for(int j=0; j< ((firstTime) ? 2 : D_STEPS); j++) {
            // The Training data is given as a parameter
            // Get random set of noise, and pass it to the generator
            INDArray rand = Nd4j.rand(new long[]{dataSet.numExamples(), latentDimension, ChordGAN.SONG_SIZE});
            INDArray generated = generator.generate(rand);

            //Label the generated data as fake (1)
            INDArray fake = Nd4j.rand(new long[]{dataSet.numExamples(), 1, ChordGAN.SONG_SIZE}).div(10.0).div(10.0).add(.9);
            DataSet generatedSet = new DataSet(generated, fake, null, outMask);

            // Train discriminator on set of actual data and generated data
            DataSet allData = DataSet.merge(Arrays.asList(dataSet, generatedSet));
            //allData.sortByLabel();
            discriminator.fit(allData);
        }
        firstTime = false;
        //Update Generator Trainer with new discriminator
        gTrainer.updateFrom(discriminator);

        // Get random set of noise, label it as real(0) so that the
        // generator tries to make things that output as real
        INDArray rand = Nd4j.rand(new long[]{dataSet.numExamples() * 2, latentDimension, ChordGAN.SONG_SIZE});
        INDArray real = Nd4j.zeros(dataSet.numExamples() * 2, 1, ChordGAN.SONG_SIZE);
        INDArray maskOut = Nd4j.zeros(dataSet.numExamples() * 2, ChordGAN.SONG_SIZE-1);
        INDArray lastLabel = Nd4j.ones(dataSet.numExamples() * 2, 1);
        INDArray doubleMask = Nd4j.hstack(maskOut, lastLabel);
        DataSet randomSet = new DataSet(rand, real, null, doubleMask);

        // Train generator
        gTrainer.fit(randomSet);

        // Update generator with new values
        generator.updateFrom(gTrainer);
    }

    public INDArray generate(INDArray source) {
        return generator.generate(source);
    }
}
