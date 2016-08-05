package ch.vvingolds.xsdiff.app;

import org.junit.Test;

public class SequenceLengthTest {

    @Test
    public void shouldCompareSequenceWithNodesAdded() throws Exception {
        new Main.App().runDiff( Main.TESTDATA_FOLDER, "simple-seq-len1.xsd", Main.TESTDATA_FOLDER, "simple-seq-len2.xsd" );
    }

    @Test
    public void shouldCompareSequenceWithNodesRemoved() throws Exception {
        new Main.App().runDiff( Main.TESTDATA_FOLDER, "simple-seq-len2.xsd", Main.TESTDATA_FOLDER, "simple-seq-len1.xsd" );
    }

}
