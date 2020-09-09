package data;

import data.model.Chord;
import data.model.Interval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChordParserTest {

    ChordParser parser;

    @BeforeEach
    void setUp() {
        parser = new ChordParser();
    }
    @Test
    void parseSlashChord() {
        Chord result = parser.parse("C/E");
        assertEquals(result.getBassAboveChordRoot(), Interval.M3);
        result = parser.parse("Db^7/Gb");
        assertEquals(result.getBassAboveChordRoot(), Interval.P4);
        result = parser.parse("Ab-7#11");
        assertEquals(result.getBassAboveChordRoot(), Interval.P1);
    }
}