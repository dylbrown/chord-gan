package data;

import data.model.Chord;
import data.model.Song;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;

class ItemParser {
    private final ChordParser chordParser = new ChordParser();
    // Rules extracted from https://github.com/pianosnake/ireal-reader/blob/master/Parser.js
    private Map<String, BiConsumer<Song.Builder, String>> strings = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>("XyQ", this::endBar),
            new AbstractMap.SimpleImmutableEntry<>("x", this::repeatPrevious),
            new AbstractMap.SimpleImmutableEntry<>("Kcl", this::repeatPrevious),
            new AbstractMap.SimpleImmutableEntry<>("r|XyQ", this::repeatTwo),
            new AbstractMap.SimpleImmutableEntry<>("r| ", this::repeatTwo),
            new AbstractMap.SimpleImmutableEntry<>("n", this::endBar),
            new AbstractMap.SimpleImmutableEntry<>("p", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("U", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("S", this::setSegno),
            new AbstractMap.SimpleImmutableEntry<>("Q", this::setCoda),
            new AbstractMap.SimpleImmutableEntry<>("{", this::startRepeat),
            new AbstractMap.SimpleImmutableEntry<>("}", this::endRepeat),
            new AbstractMap.SimpleImmutableEntry<>("LZ", this::endBar),
            new AbstractMap.SimpleImmutableEntry<>("|", this::endBar),
            new AbstractMap.SimpleImmutableEntry<>("LZ|", this::endBar),
            new AbstractMap.SimpleImmutableEntry<>("[", this::endBar),
            new AbstractMap.SimpleImmutableEntry<>("]", this::doEndings),
            new AbstractMap.SimpleImmutableEntry<>("Z", this::doEndings),
            new AbstractMap.SimpleImmutableEntry<>(" ", this::space),
            new AbstractMap.SimpleImmutableEntry<>(",", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("s", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("l", (b,s)->{})
    );
    private Map<String, BiConsumer<Song.Builder, String>> regexes = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>("\\*\\w", this::checkSection),
            new AbstractMap.SimpleImmutableEntry<>("<(.*?)>", this::checkRepeats),
            new AbstractMap.SimpleImmutableEntry<>("T(\\d+)", this::setTime),
            new AbstractMap.SimpleImmutableEntry<>("Y+", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("N(\\d)", this::setRepeatedSectionEnd),
            new AbstractMap.SimpleImmutableEntry<>(
                    "[A-GW]{1}[\\*\\+\\-\\^\\dhob#suadlt]*(\\/[A-G]?[#b]?)?", this::parseChord)
    );
    private String lastRegex = null;
    boolean canParse(String segment) {
        if(strings.containsKey(segment)) return true;
        for (String regex: regexes.keySet()) {
            if(segment.matches(regex)) {
                lastRegex = regex;
                return true;
            }
        }
        return false;
    }

    void parse(Song.Builder song, String segment) {
        BiConsumer<Song.Builder, String> fn = strings.get(segment);
        if(fn == null) {
            if(segment.matches(lastRegex)) fn = regexes.get(lastRegex);
        }
        if(fn != null) {
            fn.accept(song, segment);
        }
    }

    private void parseChord(Song.Builder builder, String segment) {
        if(skipSection) return;
        builder.push(chordParser.parse(segment));
    }

    private boolean skipSection = false;
    private void checkSection(Song.Builder builder, String segment) {
        skipSection = segment.equals("*i"); //Intro
    }

    private void space(Song.Builder builder, String segment) {
        if(builder.getProgression().size() % 4 != 0)
            builder.push(Chord.NO_CHORD);
    }

    private void endBar(Song.Builder builder, String segment) {
        while(builder.getProgression().size() % 4 != 0)
            builder.push(Chord.NO_CHORD);
    }

    private void repeatPrevious(Song.Builder builder, String segment) {
        if(skipSection) return;
        endBar(builder, "");
        for(int i = 0; i < 4; i++) {
            builder.push(builder.getProgression().get(builder.getProgression().size()-4));
        }
    }

    private void repeatTwo(Song.Builder builder, String segment) {
        if(skipSection) return;
        endBar(builder, "");
        for(int i = 0; i < 8; i++) {
            builder.push(builder.getProgression().get(builder.getProgression().size()-8));
        }
    }

    private int segno = 0;
    private void setSegno(Song.Builder builder, String segment) {
        segno = builder.getProgression().size();
    }

    private int coda = 0;
    private void setCoda(Song.Builder builder, String segment) {
        coda = builder.getProgression().size();
    }

    private int repeatStart = 0;
    private int repeatEnd = -1;
    private void startRepeat(Song.Builder builder, String segment) {
        if(skipSection) return;
        repeatStart = builder.getProgression().size();
        repeatEnd = -1;
    }

    private void endRepeat(Song.Builder builder, String segment) {
        if(skipSection) return;
        int end = builder.getProgression().size();
        if(repeatEnd != -1) end = repeatEnd;
        for(int i = repeatStart; i < end; i++) {
            builder.push(builder.getProgression().get(i));
        }
    }

    private void doEndings(Song.Builder builder, String segment) {
        if(skipSection) return;
        if(thirdEnding) {
            endRepeat(builder, "");
        } else if(dcFine || dsFine) {
            for(int i = segno; i < fine; i++) {
                builder.push(builder.getProgression().get(i));
            }
            dcFine = dsFine = false;
        } else if(dcCoda || dsCoda) {
            if(coda == 0) coda = builder.getProgression().size();
            for(int i = segno; i < coda; i++) {
                builder.push(builder.getProgression().get(i));
            }
            dcCoda = dsCoda = false;
        } else if(segment.equals("Z") && !skipSection) isOver = true;
        endBar(builder, "");

    }

    private boolean thirdEnding = false;
    private boolean dcFine = false;
    private boolean dcCoda = false;
    private boolean dsCoda = false;
    private boolean dsFine = false;
    private int fine = -1;
    private void checkRepeats(Song.Builder builder, String segment) {
        switch (segment.toLowerCase()) {
            case "<d.c. al 3rd ending>":// TODO: Handle any nth ending SEE Alice in Wonderland
                thirdEnding = true;
                break;
            case "<d.c. al fine>":
                dcFine = true;
                break;
            case "<d.c. al coda>":
                dcCoda = true;
                break;
            case "<d.s. al fine>":
                dsFine = true;
                break;
            case "<d.s. al coda>":
                dsCoda = true;
                break;
            case "<fine>":
                fine = builder.getProgression().size();
                break;
        }
    }

    private void setTime(Song.Builder builder, String segment) {
        builder.setTimeSignature(segment.replaceAll("T", ""));
    }

    private void setRepeatedSectionEnd(Song.Builder builder, String segment) {
        if(segment.equals("N1"))
            repeatEnd = builder.getProgression().size();
    }

    void reset() {
        chordParser.reset();
        lastRegex = null;
        segno = 0; coda = 0; repeatStart = 0; repeatEnd = -1;
        thirdEnding = false; dcFine = false; dcCoda = false; dsCoda = false; dsFine = false;
        fine = -1; isOver = false;
    }
    private boolean isOver = false;
    boolean isOver() {
        return isOver;
    }
}
