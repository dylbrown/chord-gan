package data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chord {
    public static final Chord NO_CHORD = new Chord.Builder(" ").build();
    private final String source;
    private final Interval intervalBelowPrevious;
    private final Interval bassAboveChordRoot;
    private final List<Boolean> chordTones;

    private Chord(Builder builder) {
        this.source = builder.source;
        this.intervalBelowPrevious = builder.intervalBelowPrevious;
        this.bassAboveChordRoot = builder.bassAboveChordRoot;
        this.chordTones = builder.chordTones;
    }

    @Override
    public String toString() {
        return source;
    }

    public Interval getIntervalBelowPrevious() {
        return intervalBelowPrevious;
    }

    public Interval getBassAboveChordRoot() {
        return bassAboveChordRoot;
    }

    public List<Boolean> getChordTones() {
        return chordTones;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder implements org.apache.commons.lang3.builder.Builder<Chord> {
        private final String source;
        private Interval intervalBelowPrevious = Interval.P1;
        private Interval bassAboveChordRoot = Interval.P1;
        private List<Boolean> chordTones = new ArrayList<>(Collections.nCopies(12, false));
        public Builder(String segment) {
            this.source = segment;
        }
        public Builder intervalBelowPrevious(Interval i) {
            intervalBelowPrevious = i;
            return this;
        }
        public Builder bassAboveChordRoot(Interval i) {
            bassAboveChordRoot = i;
            return this;
        }
        public Builder addChordTones(Interval... intervals) {
            for (Interval i : intervals) {
                chordTones.set(i.ordinal(), true);
            }
            return this;
        }

        public Builder removeChordTones(Interval... intervals) {
            for (Interval i : intervals) {
                chordTones.set(i.ordinal(), false);
            }
            return this;
        }
        @Override
        public Chord build() {
            return new Chord(this);
        }
    }
}
