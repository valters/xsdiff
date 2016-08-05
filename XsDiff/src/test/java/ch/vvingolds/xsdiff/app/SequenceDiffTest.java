package ch.vvingolds.xsdiff.app;

import org.junit.Test;

public class SequenceDiffTest {

    @Test
    public void shouldCompareSequenceWithMinimumDiff() throws Exception {
        new Main.App().runDiff( Main.TESTDATA_FOLDER, "simple-seq1.xsd", Main.TESTDATA_FOLDER, "simple-seq2.xsd" );
    }
}
