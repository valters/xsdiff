package io.github.valters.xsdiff.report;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ElemAddDiffTest {

    protected static final String TESTDATA_FOLDER = "./src/test/resources/unit/";

    private final FileSystem fs = FileSystems.getDefault();
    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUp() throws Exception {
        docBuilder = XmlDomUtils.documentBuilder();
    }

    @Test
    public void shouldCompareWithMinimumDiff() throws Exception {

        final Document controlDoc = docBuilder.parse( testFile( "simple-add1.xsd" ) );
        final Document testDoc = docBuilder.parse( testFile( "simple-add2.xsd" ) );

        new XmlSchemaDiffReport( HtmlContentOutput.startOutput( "diff-report-add.html" ) ).runDiff( controlDoc, testDoc  );
    }

    @Test
    public void shouldCompareSequenceWithMinimumDiff() throws Exception {
        final Document controlDoc = docBuilder.parse( testFile( "simple-seq1.xsd" ) );
        final Document testDoc = docBuilder.parse( testFile( "simple-seq2.xsd" ) );

        new XmlSchemaDiffReport( HtmlContentOutput.startOutput( "diff-report-seq.html" ) ).runDiff( controlDoc, testDoc  );
    }

    @Test
    public void shouldCompareSequenceWithNodesAdded() throws Exception {
        final Document controlDoc = docBuilder.parse( testFile( "simple-seq-len1.xsd" ) );
        final Document testDoc = docBuilder.parse( testFile( "simple-seq-len2.xsd" ) );

        new XmlSchemaDiffReport( HtmlContentOutput.startOutput( "diff-report-seqlen-add.html" ) ).runDiff( controlDoc, testDoc  );
    }

    @Test
    public void shouldCompareSequenceWithNodesRemoved() throws Exception {
        final Document controlDoc = docBuilder.parse( testFile( "simple-seq-len2.xsd" ) );
        final Document testDoc = docBuilder.parse( testFile( "simple-seq-len1.xsd" ) );

        new XmlSchemaDiffReport( HtmlContentOutput.startOutput( "diff-report-seqlen-rm.html" ) ).runDiff( controlDoc, testDoc  );
    }

    @Test
    public void shouldCompareSchemaWithMinimumDiff() throws Exception {
        final Document controlDoc = docBuilder.parse( testFile( "simple-schema1.xsd" ) );
        final Document testDoc = docBuilder.parse( testFile( "simple-schema2.xsd" ) );

        new XmlSchemaDiffReport( HtmlContentOutput.startOutput( "diff-report-schema.html" ) ).runDiff( controlDoc, testDoc  );
    }

    private InputSource testFile( final String fileName ) throws IOException {
        return new InputSource( Files.newBufferedReader( fs.getPath( TESTDATA_FOLDER, fileName  ) ) );
    }
}
