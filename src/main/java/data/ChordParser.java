package data;

import data.model.Chord;
import data.model.Interval;

class ChordParser {
    private String previousRoot;
    Chord parse(String segment) {
        Chord.Builder builder = new Chord.Builder(segment);
        String root = (segment.length() > 1 && (segment.charAt(1) == '#' || segment.charAt(1) == 'b')) ? segment.substring(0,2) : segment.substring(0,1);
        builder.intervalBelowPrevious(Interval.getInterval(previousRoot, root));
        setQuality(builder, segment.substring(root.length()));
        previousRoot = root;
        return builder.build();
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
            String root = (substring.length() > 2 &&
                    (substring.charAt(2) == '#' || substring.charAt(2) == 'b'))
                    ? substring.substring(1,3) : substring.substring(1,2);
            builder.bassAboveChordRoot(Interval.getInterval(root, previousRoot));
            qualityLength = 1 + root.length();
        }
        if(qualityLength > 0)
            setExtensions(builder, substring.substring(qualityLength));
    }

    void reset() {
        previousRoot = null;
    }
}
