/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package ch.vvingolds.xsdiff.app;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.XslFilter;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** entry point */
public class Main {

    protected static final String TESTDATA_FOLDER = "../testdata/";

    public static void main( final String[] args ) {
        new App().run();
    }

    /** app bootstrap */
    public static class App {

        private final XmlDomUtils xmlDomUtils = new XmlDomUtils();

        public void run() {
            try {
                runDiff( TESTDATA_FOLDER, "simple-schema1.xsd", TESTDATA_FOLDER, "simple-schema2.xsd" );
            }
            catch( final Exception e ) {
                System.out.println("Error, failed to run, exception occurred: " + e );
                e.printStackTrace();
            }
        }

        public void runDiff( final String folder1, final String file1, final String folder2, final String file2 ) throws Exception {

            final SAXTransformerFactory tf = XmlDomUtils.saxTransformerFactory();

            final TransformerHandler result = XmlDomUtils.newFragmentTransformerHandler( tf );
            final String outputFile = "diff-report.html";
            result.setResult(new StreamResult(new File(outputFile)));

            final XslFilter filter = new XslFilter();
            final ContentHandler content = filter.xsl(result, "xslfilter/tagheader.xsl");

            content.startDocument();
            content.startElement("", "diffreport", "diffreport", new AttributesImpl());
            content.startElement("", "diff", "diff", new AttributesImpl());

            final ContentHtmlOutput contentOutput = new ContentHtmlOutput( content );

            final FileSystem fs = FileSystems.getDefault();
            final Path f1 = fs.getPath( folder1, file1 );
            final Path f2 = fs.getPath( folder2, file2 );

            contentOutput.write( "comparing: "+f1+" with "+f2 );

            runDiff( Files.newBufferedReader( f1 ),
                    Files.newBufferedReader( f2 ),
                    contentOutput );

            content.endElement("", "diff", "diff");
            content.endElement("", "diffreport", "diffreport");
            content.endDocument();

        }

        public void runDiff( final Reader file1, final Reader file2, final ContentHtmlOutput output ) {

            try {

                final DocumentBuilder docBuilder = xmlDomUtils.documentBuilder();
                final Document controlDoc = docBuilder.parse( new InputSource( file1 ) );
                final Document testDoc = docBuilder.parse( new InputSource( file2 ) );

                new XmlSchemaDiffReport( output ).runDiff( controlDoc, testDoc );
            }
            catch( final ParserConfigurationException e ) {
                throw new RuntimeException( "Failed to parse: ", e );
            }
            catch( final SAXException e ) {
                throw new RuntimeException( "Failed to parse: ", e );
            }
            catch( final IOException e ) {
                throw new RuntimeException( "Failed to parse: ", e );
            }
        }
    }
}
