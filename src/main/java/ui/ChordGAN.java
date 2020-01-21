package ui;

import network.GAN;

@SuppressWarnings("FieldCanBeLocal")
public class ChordGAN {
    public static final long SONG_SIZE = 42;
    public static void main(String[] args) {
        GAN gan = new GAN(13);
        for(int i = 0; i < 10; i++) {
            // gan.train();
            // Display Results
        }
    }
}
