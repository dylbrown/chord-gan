package data;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
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
        deobfuscatedBody.append(split[0]).append("==").append(split[1]).append("==");
        for(int i=0; i < body.length()-51; i+= 50) {
            deobfuscatedBody.append(deobfuscate(body, i));
        }
        if(deobfuscatedBody.length() < body.length())
            deobfuscatedBody.append(body.substring(deobfuscatedBody.length()));
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

        Song song = new Song();
        while(body.length() > 0) {
            body = tryParse(song, body);
        }
        return song;
    }

    private String tryParse(Song song, String body) {
        boolean foundMatch = false;
        for(int i = 1; i < body.length(); i++) {
            if(!foundMatch && canParse(body.substring(0, i))) foundMatch = true;
            if(foundMatch && !canParse(body.substring(0, i+1))) {
                parse(song, body.substring(0, i));
                return body.substring(i);
            }
        }
        return "";
    }

    private List<String> strings = Arrays.asList("XyQ", "x", "Kcl", "r|XyQ", "n", "p", "U", "S", "Q", "{", "}", "LZ|", "|", "LZ", "[", "]", "Z", " ");
    private List<String> regexes = Arrays.asList("\\*\\w", "<(.*?)>", "T(\\d+)", "Y+", "N(\\d)", "[A-GW][+\\-^\\dhob#suadlt]*(/[A-G][#b]?)?");
    private boolean canParse(String segment) {
        if(strings.contains(segment)) return true;
        for (String regex: regexes) {
            if(segment.matches(regex)) return true;
        }
        return false;
    }

    private String previousRoot = "";
    private void parse(Song song, String segment) {
        if(segment.matches("[A-GW][+\\-^\\dhob#suadlt]*(/[A-G][#b]?)?")) {
            Chord.Builder builder = new Chord.Builder();
            String root = (segment.length() > 1 && (segment.charAt(1) == '#' || segment.charAt(1) == 'b')) ? segment.substring(0,2) : segment.substring(0,1);
            builder.intervalBelowPrevious(Interval.getInterval(previousRoot, root));
            setQuality(builder, segment.substring(root.length()));
            previousRoot = root;
            song.push(builder.build());
        }
    }

    private void setQuality(Chord.Builder builder, String substring) {
        builder.addChordTones(Interval.P1);
        if(substring.length() == 0){
            builder.addChordTones(Interval.M3, Interval.P5);
            return;
        }
        int qualityLength = 0;
        if(substring.startsWith("-^")){
            builder.addChordTones(Interval.m3, Interval.P5, Interval.M7);
            qualityLength = 2;
        } else if(substring.startsWith("-")) {
            builder.addChordTones(Interval.m3, Interval.P5, Interval.M7);
            qualityLength = 1;
        } else if(substring.startsWith("+")) {
            builder.addChordTones(Interval.M3, Interval.m6);
            qualityLength = 1;
        } else if(substring.startsWith("^7")) {
            builder.addChordTones(Interval.M3, Interval.P5, Interval.M7);
            qualityLength = 2;
        } else if(substring.startsWith("o")) {
            builder.addChordTones(Interval.m3, Interval.TT, Interval.M6);
            qualityLength = 1;
        } else if(substring.startsWith("h")) {
            builder.addChordTones(Interval.m3, Interval.TT, Interval.m7);
            qualityLength = 1;
        }

        // Digits
        if(qualityLength == 0) builder.addChordTones(Interval.M3, Interval.P5);
        setDigits(builder, substring.substring(qualityLength));
    }

    private void setDigits(Chord.Builder builder, String substring) {
        if(substring.length() == 0) return;
        int qualityLength = 0;
        if(substring.startsWith("7")) {
            builder.addChordTones(Interval.m7);
            qualityLength = 1;
        } else if(substring.startsWith("69")) {
            builder.addChordTones(Interval.M6, Interval.M2);
            qualityLength = 2;
        } else if(substring.startsWith("6")) {
            builder.addChordTones(Interval.M6);
            qualityLength = 1;
        } else if(substring.startsWith("9")) {
            builder.addChordTones(Interval.m7, Interval.M2);
            qualityLength = 1;
        } else if(substring.startsWith("11")) {
            builder.addChordTones(Interval.m7, Interval.P4);
            qualityLength = 2;
        } else if(substring.startsWith("13")) {
            builder.addChordTones(Interval.M7, Interval.M6);
            qualityLength = 2;
        }
        setExtensions(builder, substring.substring(qualityLength));
    }

    private void setExtensions(Chord.Builder builder, String substring) {
        if(substring.length() == 0) return;
        int qualityLength = 0;
        if(substring.startsWith("b")) {
            if(substring.startsWith("9", 1)) {
                builder.addChordTones(Interval.m2);
                qualityLength = 2;
            } else if(substring.startsWith("13", 1)) {
                builder.addChordTones(Interval.m6);
                qualityLength = 3;
            }
        }else if(substring.startsWith("#")) {
            if(substring.startsWith("9", 1)) {
                builder.addChordTones(Interval.m3);
                qualityLength = 2;
            } else if(substring.startsWith("11", 1)) {
                builder.addChordTones(Interval.TT);
                qualityLength = 3;
            }
        }else if(substring.startsWith("sus")) {
            builder.removeChordTones(Interval.M3);
            qualityLength = 3;
            if(substring.startsWith("2", 3)) {
                builder.addChordTones(Interval.M2);
                qualityLength = 4;
            }else if(substring.startsWith("4", 3)){
                builder.addChordTones(Interval.P4);
                qualityLength = 4;
            }else{
                builder.addChordTones(Interval.P4);
            }
        }else if(substring.startsWith("/")) {
            String root = (substring.charAt(2) == '#' || substring.charAt(2) == 'b') ? substring.substring(1,3) : substring.substring(1,2);
            builder.bassAboveChordRoot(Interval.getInterval(root, previousRoot));
            qualityLength = 1 + root.length();
        }

        setExtensions(builder, substring.substring(qualityLength));
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
