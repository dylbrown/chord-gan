package data;

import data.model.Song;

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
    private final ItemParser itemParser = new ItemParser();
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
                if(song.equals("Jazz 1350")) continue;
                song = unscrambleSong(song);
                String title = song.split("=", 2)[0].replaceAll("\\W+", "");
                File file = new File(baseString + "raw-songs/" + title + ".txt");
                if(file.createNewFile())
                    Files.writeString(file.toPath(), song);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String unscrambleSong(String songString) {
        String[] split = songString.split("==", 3);
        if(split.length < 3) System.out.println(songString);
        String body = split[2].replace("1r34LbKcu7", "");
        StringBuilder deobfuscatedBody = new StringBuilder();
        String beginning = split[0] + "==" + split[1] + "==";
        deobfuscatedBody.append(beginning);
        for(int i=0; i < body.length()-51; i+= 50) {
            deobfuscatedBody.append(deobfuscate(body, i));
        }
        if(deobfuscatedBody.length() < beginning.length() + body.length())
            deobfuscatedBody.append(body.substring(deobfuscatedBody.length()-beginning.length()));
        return deobfuscatedBody.toString();
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

        Song.Builder song = new Song.Builder();
        while(body.length() > 0) {
            body = tryParse(song, body);
        }
        return song.build();
    }

    private String tryParse(Song.Builder song, String body) {
        boolean foundMatch = false;
        for(int i = 1; i < body.length(); i++) {
            if(!foundMatch && itemParser.canParse(body.substring(0, i))) foundMatch = true;
            if(foundMatch && !itemParser.canParse(body.substring(0, i+1))) {
                itemParser.parse(song, body.substring(0, i));
                return body.substring(i);
            }
        }
        return "";
    }

    // Scrambling details from https://github.com/pianosnake/ireal-reader/blob/master/unscramble.js
    private char[] deobfuscate(String body, int offset) {
        char[] substring = new char[50];
        //The first 5 characters have been swapped with the last 5
        for(int i=0; i<5; i++) {
            substring[i] = body.charAt(49-i+offset);
            substring[49-i] = body.charAt(i+offset);
        }
        for(int i=5; i<10; i++) {
            substring[i] = body.charAt(i+offset);
            substring[49-i] = body.charAt(49-i+offset);
        }
        // Same for characters 10-24
        for(int i=10; i<24; i++) {
            substring[i] = body.charAt(49-i+offset);
            substring[49-i] = body.charAt(i+offset);
        }
        substring[24] = body.charAt(24+offset);
        substring[25] = body.charAt(25+offset);
        return substring;
    }
}
