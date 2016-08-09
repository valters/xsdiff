package ch.vvingolds.xsdiff.format;

import org.outerj.daisy.diff.DaisyDiff;
import org.xml.sax.ContentHandler;

public class DaisyDiffFormatter implements ContentHandlerFormatter {

    private String oldText;
    private String newText;

    public DaisyDiffFormatter createDiff( final String oldText, final String newText ) {
        this.oldText = oldText;
        this.newText = newText;

        return this;
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
