package data;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class IRealParser {
    private static final String baseString = "src/main/resources/";
    private static final File source = new File(baseString+"jazz1350.txt");
    private static final File decoded = new File(baseString+"jazz1350_decoded.txt");
    private static final File rawSongs = new File(baseString + "raw-songs/");
    public static void main(String[] args) {
        IRealParser parser = new IRealParser();
        parser.decode();
        parser.createRawSongs();
        parser.createSongCSVs();
    }

    private void decode() {
        if(decoded.exists()) return;
        try {
            String s = Files.readString(source.toPath());
            Files.writeString(decoded.toPath(), URLDecoder.decode(s, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRawSongs() {
        if(rawSongs.exists()) return;
        //noinspection ResultOfMethodCallIgnored
        rawSongs.mkdir();
        try {
            String s = Files.readString(decoded.toPath()).replace("irealb://", "");
            for (String song : s.split("==0=0===")) {
                String title = song.split("=", 2)[0].replaceAll("\\W+", "");
                File file = new File(baseString + "raw-songs/" + title + ".txt");
                if(file.createNewFile())
                    Files.writeString(file.toPath(), song);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createSongCSVs() {
        for (File rawSongFile : Objects.requireNonNull(rawSongs.listFiles())) {
            try {
                String songString = Files.readString(rawSongFile.toPath());
                Song song = createSong(songString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Song createSong(String songString) {
        String body = songString.split("==", 3)[2];
        StringBuilder deobfuscatedBody = new StringBuilder();
        for(int i=0; i < body.length(); i+= 50) {
            deobfuscatedBody.append(deobfuscate(body, i));
        }
        body = deobfuscatedBody.toString();
        return null;
    }

    // Scrambling details from https://github.com/pianosnake/ireal-reader/blob/master/unscramble.js
    private char[] deobfuscate(String body, int offset) {
        char[] substring = new char[50];
        //The first 5 characters have been swapped with the last 5
        for(int i=0; i<5; i++) {
            substring[i] = body.charAt(50-i+offset);
            substring[49-i] = body.charAt(i+offset);
        }
        for(int i=5; i<10; i++) {
            substring[i] = body.charAt(i+offset);
            substring[49-i] = body.charAt(49-i+offset);
        }
        // Same for characters 10-24
        for(int i=10; i<24; i++) {
            substring[i] = body.charAt(50-i+offset);
            substring[49-i] = body.charAt(i+offset);
        }
        return substring;
    }
}
