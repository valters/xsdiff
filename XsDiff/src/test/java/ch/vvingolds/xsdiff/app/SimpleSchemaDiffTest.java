package ch.vvingolds.xsdiff.app;

import org.junit.Test;

public class SimpleSchemaDiffTest {

    @Test
    public void shouldCompareSequenceWithMinimumDiff() throws Exception {
        new Main.App().runDiff( Main.TESTDATA_FOLDER, "simple-schema1.xsd", Main.TESTDATA_FOLDER, "simple-schema2.xsd" );
    }
}