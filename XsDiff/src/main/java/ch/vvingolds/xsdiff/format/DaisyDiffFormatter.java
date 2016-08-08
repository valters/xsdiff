package ch.vvingolds.xsdiff.format;

import org.outerj.daisy.diff.DaisyDiff;
import org.xml.sax.ContentHandler;

public class DaisyDiffFormatter {

    final ContentHandler resultHandler;

    public DaisyDiffFormatter( final ContentHandler resultHandler ) {
        super();
        this.resultHandler = resultHandler;
    }

    public void printDiff( final String oldText, final String newText ) {
        try {
            DaisyDiff.diffTag( oldText, newText, resultHandler );
        }
        catch( final Exception e ) {
            // "(failed to daisydiff: "+e+")" );
        }
    }

}
