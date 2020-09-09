package data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Song {
    private String title;
    private final String timeSignature;
    private List<Chord> progression;
    private Song(Song.Builder builder) {
        this.progression = builder.progression;
        this.timeSignature = builder.timeSignature;
        this.title = builder.title;
    }
    public List<Chord> getProgression() {
        return Collections.unmodifiableList(progression);
    }

    public String getTimeSignature() {
        return timeSignature;
    }

    public String getTitle() {
        return title;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<Song> {
        private List<Chord> progression = new ArrayList<>();
        private String timeSignature = "44";
        private String title;

        public void push(Chord... c) {
            push(Arrays.asList(c));
        }

        void push(List<Chord> c) {
            progression.addAll(c);
        }

        public List<Chord> getProgression() {
            return  Collections.unmodifiableList(progression);
        }

        public Song build() {
            return new Song(this);
        }

        public void setTimeSignature(String time) {
            this.timeSignature = time;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
