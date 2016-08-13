package ch.vvingolds.xsdiff.format;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/** does not contribute any output: used to avoid nulls */
public class EmptyDiff implements ContentHandlerFormatter, DiffOutputFormatter {

    public static final EmptyDiff INSTANCE = new EmptyDiff();

    /** please use {@link #INSTANCE} */
    private EmptyDiff() {
        super();
    }

    private static final String EMPTY_CONTENT = "(empty)";

    @Override
    public void printDiff( final DiffOutput output ) {
        output.clearPart( EMPTY_CONTENT );
    }

    @Override
    public void printDiff( final ContentHandler resultHandler ) {
        try {
            resultHandler.characters( EMPTY_CONTENT.toCharArray(), 0, EMPTY_CONTENT.length() );
        }
        catch( final SAXException e ) {
            ;
        }
    }

}
