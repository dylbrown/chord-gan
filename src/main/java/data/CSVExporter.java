package data;

import data.model.Chord;
import data.model.Song;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class CSVExporter {
    private final String root;
    private Map<Integer, File> folderBySize = new HashMap<>();

    CSVExporter(String path) {
        this.root = path;
    }

    void exportCSV(Song song) throws IOException {
        File folder = getFolderBySize(song.getProgression().size());
        String encodedName = URLEncoder.encode(song.getTitle(), StandardCharsets.UTF_8);
        File songFile = new File(folder.getPath()+"/"+encodedName+".csv");
        if(!songFile.exists() && songFile.createNewFile()){
            PrintWriter writer = new PrintWriter(songFile);
            boolean sendChord = true;
            for (Chord chord : song.getProgression()) {
                if(sendChord){
                    writer.println(chordToCSV(chord));
                    sendChord = false;
                }else sendChord = true;
            }
            writer.close();
        }
    }

    private String chordToCSV(Chord chord) {
        int interval  = chord.getIntervalBelowPrevious().ordinal();
        int bass = chord.getBassAboveChordRoot().ordinal();
        StringBuilder line = new StringBuilder();
        for(int i = 0; i<interval; i++) {
            line.append("0,");
        }
        line.append("1,");
        for(int i = interval + 1; i<12; i++) {
            line.append("0,");
        }
        for(int i = 0; i<bass; i++) {
            line.append("0,");
        }
        line.append("1,");
        for(int i = bass + 1; i<12; i++) {
            line.append("0,");
        }
        line.append(chord.getChordTones().subList(1,12).stream()
                .map(tone->(tone) ? "1" : "0").collect(Collectors.joining(",")));
        line.append(",1");
        return line.toString();
    }

    private File getFolderBySize(int size) {
        File file = folderBySize.computeIfAbsent(size, (s) -> new File(root + s + "/"));
        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();
        return file;
    }
}
