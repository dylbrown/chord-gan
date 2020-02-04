package data;

public enum Interval {
    P1, m2, M2, m3, M3, P4, TT, P5, m6, M6, m7, M7;

    public static Interval getInterval(String previousRoot, String root) {
        if(previousRoot.equals("") || root.equals("W")) return P1;
        int downOffset = (getNum(previousRoot) - getNum(root) + 12) % 12;
        for (Interval interval : Interval.values()) {
            if(interval.ordinal() == downOffset) return interval;
        }
        return null;
    }

    private static int getNum(String note) {
        switch(note) {
            default:
            case "C": return 0;
            case "C#":
            case "Db": return 1;
            case "D": return 2;
            case "D#":
            case "Eb": return 3;
            case "E": return 4;
            case "F": return 5;
            case "F#":
            case "Gb": return 6;
            case "G": return 7;
            case "G#":
            case "Ab": return 8;
            case "A": return 9;
            case "A#":
            case "Bb": return 10;
            case "B": return 11;
        }
    }
}