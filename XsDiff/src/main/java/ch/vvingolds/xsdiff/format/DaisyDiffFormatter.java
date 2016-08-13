package ch.vvingolds.xsdiff.format;

import org.outerj.daisy.diff.DaisyDiff;
import org.xml.sax.ContentHandler;

public class DaisyDiffFormatter implements ContentHandlerFormatter {

    private final String oldText;
    private final String newText;

    public DaisyDiffFormatter( final String oldText, final String newText ) {
        this.oldText = oldText;
        this.newText = newText;
    }

    @Override
    public void printDiff( final ContentHandler resultHandler ) {
        try {
            DaisyDiff.diffTag( oldText, newText, resultHandler );
        }
        catch( final Exception e ) {
            // "(failed to daisydiff: "+e+")" );
        }
    }

}
