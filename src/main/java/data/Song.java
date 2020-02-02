package data;

public class Song {
    private Chord[] progression = new Chord[64];
    private int currEndpoint = 0;
    public void push(Chord c) {
        progression[currEndpoint] = c;
        currEndpoint++;
    }

    public Chord[] getProgression() {
        return progression;
    }
}
