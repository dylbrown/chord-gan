package data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Song {
    private List<Chord> progression;
    private Song(Song.Builder builder) {
        this.progression = builder.progression;
    }
    public List<Chord> getProgression() {
        return Collections.unmodifiableList(progression);
    }
    public static class Builder implements org.apache.commons.lang3.builder.Builder<Song> {
        private List<Chord> progression = new ArrayList<>();
        private int currEndpoint = 0;

        public void push(Chord c) {
            progression.add(c);
            currEndpoint += 1;
        }

        public List<Chord> getProgression() {
            return progression;
        }

        public Chord getLastChord() {
            return (currEndpoint > 0) ? progression.get(currEndpoint-1) : null;
        }

        public Song build() {
            return new Song(this);
        }
    }
}
