package data;

import data.model.Song;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;

class ItemParser {
    private final ChordParser chordParser = new ChordParser();
    // Rules extracted from https://github.com/pianosnake/ireal-reader/blob/master/Parser.js
    private Map<String, BiConsumer<Song.Builder, String>> strings = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>("XyQ", this::fillBar),
            new AbstractMap.SimpleImmutableEntry<>("x", this::repeatPrevious),
            new AbstractMap.SimpleImmutableEntry<>("Kcl", this::repeatPrevious),
            new AbstractMap.SimpleImmutableEntry<>("r|XyQ", this::repeatTwo),
            new AbstractMap.SimpleImmutableEntry<>("n", this::fillBar),
            new AbstractMap.SimpleImmutableEntry<>("p", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("U", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("S", this::setSegno),
            new AbstractMap.SimpleImmutableEntry<>("C", this::setCoda),
            new AbstractMap.SimpleImmutableEntry<>("{", this::startRepeat),
            new AbstractMap.SimpleImmutableEntry<>("}", this::endRepeat),
            new AbstractMap.SimpleImmutableEntry<>("LZ", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("|", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("LZ|", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("[", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("]", this::doEndings),
            new AbstractMap.SimpleImmutableEntry<>("Z", this::doEndings),
            new AbstractMap.SimpleImmutableEntry<>(" ", (b,s)->{})
    );
    private Map<String, BiConsumer<Song.Builder, String>> regexes = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>("\\*\\w", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("<(.*?)>", this::checkRepeats),
            new AbstractMap.SimpleImmutableEntry<>("T(\\d+)", this::setTime),
            new AbstractMap.SimpleImmutableEntry<>("Y+", (b,s)->{}),
            new AbstractMap.SimpleImmutableEntry<>("N(\\d)", this::setRepeatedSectionEnd),
            new AbstractMap.SimpleImmutableEntry<>(
                    "[A-GW]{1}[\\+\\-\\^\\dhob#suadlt]*(\\/[A-G][#b]?)?", chordParser::parse)
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

    private void fillBar(Song.Builder builder, String segment) {
    }

    private void repeatPrevious(Song.Builder builder, String segment) {
    }

    private void repeatTwo(Song.Builder builder, String segment) {
    }

    private void setSegno(Song.Builder builder, String segment) {
    }

    private void setCoda(Song.Builder builder, String segment) {
    }

    private void startRepeat(Song.Builder builder, String segment) {
    }

    private void endRepeat(Song.Builder builder, String segment) {
    }

    private void doEndings(Song.Builder builder, String segment) {
    }

    private void checkRepeats(Song.Builder builder, String segment) {
    }

    private void setTime(Song.Builder builder, String segment) {
    }

    private void setRepeatedSectionEnd(Song.Builder builder, String segment) {
    }
}
