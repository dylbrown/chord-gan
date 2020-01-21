package network;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

public class GAN {
    private static final int D_STEPS = 1;
    private final long latentDimension;
    private Discriminator discriminator;
    private Generator generator;
    private GeneratorTrainer gTrainer;

    public GAN(long latentDimension) {
        this.latentDimension = latentDimension;
        discriminator = new Discriminator();
        generator = new Generator(latentDimension);
        gTrainer = new GeneratorTrainer(discriminator, generator);
    }

    public void train(DataSetIterator iterator) {
        while (iterator.hasNext()) {
            train(iterator.next());
        }
    }

    private void train(DataSet dataSet) {
        for(int j=0; j<D_STEPS; j++) {
            // The Training data is given as a parameter
            // Get random set of noise, and pass it to the generator
            INDArray rand = Nd4j.rand(new long[]{dataSet.numExamples(), latentDimension});
            INDArray generated = generator.generate(rand);

            //Label the generated data as fake (0)
            INDArray fake = Nd4j.zeros(dataSet.numExamples());
            DataSet generatedSet = new DataSet(generated, fake);

            // Train discriminator on set of actual data and generated data
            DataSet allData = DataSet.merge(Arrays.asList(dataSet, generatedSet));
            discriminator.fit(allData);
        }
        //Update Generator Trainer with new discriminator
        gTrainer.updateFrom(discriminator);

        // Get random set of noise, label it as real so that the
        // generator tries to make things that output as real
        INDArray rand = Nd4j.rand(new long[]{dataSet.numExamples(), latentDimension});
        INDArray real = Nd4j.zeros(dataSet.numExamples());
        DataSet randomSet = new DataSet(rand, real);

        // Train generator
        gTrainer.fit(randomSet);

        // Update generator with new values
        generator.updateFrom(gTrainer);
    }
}
