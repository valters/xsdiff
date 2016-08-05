package ch.vvingolds.xsdiff.app;

import org.junit.Test;

public class ElemAddDiffTest {

    @Test
    public void shouldCompareSequenceWithMinimumDiff() throws Exception {
        new Main.App().runDiff( Main.TESTDATA_FOLDER, "simple-add1.xsd", Main.TESTDATA_FOLDER, "simple-add2.xsd" );
    }
}
