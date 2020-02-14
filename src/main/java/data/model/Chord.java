package data.model;

import java.util.Arrays;

public class Chord {
    public static final Chord NO_CHORD = new Chord.Builder().build();
    private Interval intervalBelowPrevious;
    private Interval bassAboveChordRoot;
    private boolean[] chordTones;

    private Chord(Builder builder) {
        this.intervalBelowPrevious = builder.intervalBelowPrevious;
        this.bassAboveChordRoot = builder.bassAboveChordRoot;
        this.chordTones = builder.chordTones;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder implements org.apache.commons.lang3.builder.Builder<Chord> {
        private Interval intervalBelowPrevious = Interval.P1;
        private Interval bassAboveChordRoot = Interval.P1;
        private boolean[] chordTones = new boolean[12];
        public Builder() {
            Arrays.fill(chordTones, false);
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
                chordTones[i.ordinal()] = true;
            }
            return this;
        }

        public Builder removeChordTones(Interval... intervals) {
            for (Interval i : intervals) {
                chordTones[i.ordinal()] = false;
            }
            return this;
        }
        @Override
        public Chord build() {
            return new Chord(this);
        }
    }
}
