package data;

import java.util.Arrays;

class Chord {
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
        Builder() {
            Arrays.fill(chordTones, false);
        }
        Builder intervalBelowPrevious(Interval i) {
            intervalBelowPrevious = i;
            return this;
        }
        Builder bassAboveChordRoot(Interval i) {
            bassAboveChordRoot = i;
            return this;
        }
        Builder addChordTones(Interval... intervals) {
            for (Interval i : intervals) {
                chordTones[i.ordinal()] = true;
            }
            return this;
        }

        Builder removeChordTones(Interval... intervals) {
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
