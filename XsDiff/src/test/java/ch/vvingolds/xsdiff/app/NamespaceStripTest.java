package ch.vvingolds.xsdiff.app;

import javax.xml.parsers.DocumentBuilder;

import org.junit.Test;
import org.w3c.dom.Document;

public class NamespaceStripTest {

    @Test
    public void shouldCompareSequenceWithMinimumDiff() throws Exception {
        final DocumentBuilder b = XmlDomUtils.documentBuilder();
        final Document doc = b.parse( this.getClass().getClassLoader().getResourceAsStream( "unit/simple-seq.xsd" ) );

        final TransformToString t = new TransformToString();
        final String s = t.nodeToString( t.importWithoutNamespaces( doc.getDocumentElement() ) );
        System.out.println( s );
    }

}
