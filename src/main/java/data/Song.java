package data;

public class Song {
    private Chord[] progression = new Chord[64];
    private int currEndpoint = 0;
    void push(Chord c) {
        progression[currEndpoint] = c;
        currEndpoint += 1;
    }

    public Chord[] getProgression() {
        return progression;
    }

    public Chord getLastChord() {
        return (currEndpoint > 0) ? progression[currEndpoint-1] : null;
    }
}
