package ui;

import network.GAN;
import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ChordGAN {
    public static final long SONG_SIZE = 64;
    public static final int NUM_FEATURES = 35;
    public static final int LATENT_DIMENSION = 80;
    public static final long BATCH_SIZE = 128;
    private static final File songsFolder = new File("src/main/resources/csv-songs/128/");
    private static final String resultsFolder = "src/main/resources/results/";

    public static void main(String[] args) {
        UIServer uiServer = UIServer.getInstance();
        File file = new File("src/main/resources/disc.txt");
        file.delete();
        StatsStorage discStorage = new FileStatsStorage(file);
        StatsStorage genStorage = new FileStatsStorage(new File("src/main/resources/gen.txt"));
        GAN gan = new GAN(LATENT_DIMENSION, new StatsListener(discStorage), new StatsListener(genStorage));
        uiServer.attach(discStorage);
        uiServer.attach(genStorage);
        SequenceRecordReader realSongsReader = new CSVSequenceRecordReader();
        try {
            realSongsReader.initialize(new FileSplit(songsFolder));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        SequenceRecordReaderDataSetIterator realSongs = new SequenceRecordReaderDataSetIterator(realSongsReader, 32, 2, NUM_FEATURES);
        NormalizerMinMaxScaler scaler = new NormalizerMinMaxScaler(0, 1);
        scaler.fit(realSongs);
        realSongs.setPreProcessor(scaler);
        try {
            FileUtils.deleteDirectory(new File(resultsFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0; i<100; i++) {
            gan.train(realSongs);
            if(i % 8 == 0) {
                INDArray generated = gan.generate(Nd4j.rand(new long[]{4, LATENT_DIMENSION, ChordGAN.SONG_SIZE}));
                for(int item = 0; item < 4; item++) {
                    saveToCSV(generated, i, item);
                }
            }
            System.out.println("Epoch "+i+" Completed");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void saveToCSV(INDArray generated, int epoch, int item) {
        File file = new File(resultsFolder + epoch + "_" + item + ".csv");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            PrintWriter printWriter = new PrintWriter(file);
            for(int i=0; i < ChordGAN.SONG_SIZE; i++) {
                List<String> stringList = new ArrayList<>(NUM_FEATURES);
                int rootMaxId = 0; double rootMaxVal = -1;
                int bassMaxId = 0; double bassMaxVal = -1;
                for(int j=0; j < 12; j++) {
                    double root = generated.getDouble(item, j, i);
                    double bass = generated.getDouble(item, j+12, i);
                    if(root < rootMaxVal) {
                        rootMaxVal = root;
                        rootMaxId = j;
                    }
                    if(bass < bassMaxVal) {
                        bassMaxVal = bass;
                        bassMaxId = j;
                    }
                }
                for(int j = 0; j<rootMaxId; j++) {
                    stringList.add("0");
                }
                stringList.add("1");
                for(int j = rootMaxId + 1; j<12; j++) {
                    stringList.add("0");
                }
                for(int j = 0; j<bassMaxId; j++) {
                    stringList.add("0");
                }
                stringList.add("1");
                for(int j = bassMaxId + 1; j<12; j++) {
                    stringList.add("0");
                }
                for(int j = 24; j < 35; j++) {
                    double d = generated.getDouble(item, j, i);
                    d += 1;
                    d /= 2;
                    stringList.add(String.valueOf(Math.round(d)));
                }
                printWriter.println(String.join(",", stringList));
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
