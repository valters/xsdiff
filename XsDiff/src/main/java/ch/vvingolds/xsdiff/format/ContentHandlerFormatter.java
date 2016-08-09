package ch.vvingolds.xsdiff.format;

import org.xml.sax.ContentHandler;

/** formatter provides low-level output directly to SaX ContentHandler */
public interface ContentHandlerFormatter {

    void printDiff( final ContentHandler resultHandler );
}
