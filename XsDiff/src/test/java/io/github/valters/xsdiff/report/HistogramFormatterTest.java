package io.github.valters.xsdiff.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import io.github.valters.xsdiff.format.HistogramDiffFormatter;

public class HistogramFormatterTest {

    @Test
    public void shouldDetectRemovedAndAddedLine() {
        final String a = "a\n b,c\n d. e\n f";
        final String b = "a\n\n d. e\n x, y. z\n f";

        final HistogramDiffFormatter fixture = new HistogramDiffFormatter( a, b );
        final DiffTestFixture.DummyOutput output = DiffTestFixture.output();
        fixture.printDiff( output );

        assertThat( output.getResults().size(), is( 6 ) );
        assertThat( output.getResults().get( 0 ).getType(), is( DiffTestFixture.OperationType.NO_CHANGE ) );
        assertThat( output.getResults().get( 1 ).getType(), is( DiffTestFixture.OperationType.REMOVE_TEXT ) );
        assertThat( output.getResults().get( 1 ).getText(), is( " b,c\n" ) );
        assertThat( output.getResults().get( 2 ).getType(), is( DiffTestFixture.OperationType.ADD_TEXT ) );
        assertThat( output.getResults().get( 2 ).getText(), is( "\n" ) );
        assertThat( output.getResults().get( 3 ).getType(), is( DiffTestFixture.OperationType.NO_CHANGE ) );
        assertThat( output.getResults().get( 4 ).getType(), is( DiffTestFixture.OperationType.ADD_TEXT ) );
        assertThat( output.getResults().get( 4 ).getText(), is( " x, y. z\n" ) );
        assertThat( output.getResults().get( 5 ).getType(), is( DiffTestFixture.OperationType.NO_CHANGE ) );
    }
}
