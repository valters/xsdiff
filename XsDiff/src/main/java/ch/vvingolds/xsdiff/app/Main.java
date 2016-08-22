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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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

    protected static final String TESTDATA_FOLDER = "./src/test/resources/unit/";

    public static void main( final String[] args ) {
        new App().run();
    }

    /** app bootstrap */
    public static class App {

        /** how to convert the xml to HTML */
        private static final String HTML_TRANSFORMATION_XSL = "xslfilter/tagheader-xsdiff.xsl";

        /** XSL looks for diffreport/diff/node() */
        private static final String XSL_PLACEHOLDER = "diffreport";
        /** XSL looks for diffreport/diff/node() */
        private static final String XSL_DIFF_PLACEHOLDER = "diff";

        public void run() {
            try {
                //runDiff( TESTDATA_FOLDER, "subnode-remove1.xsd", TESTDATA_FOLDER, "subnode-remove2.xsd" );
                //runDiff( TESTDATA_FOLDER, "simple-seq-len1.xsd", TESTDATA_FOLDER, "simple-seq-len2.xsd" );
                //runDiff( TESTDATA_FOLDER, "ext-remove1.xsd", TESTDATA_FOLDER, "ext-remove2.xsd" );
                runDiff( TESTDATA_FOLDER, "simple-add1.xsd", TESTDATA_FOLDER, "simple-add2.xsd" );
                //runDiff( TESTDATA_FOLDER+"a/", TESTDATA_FOLDER+"b/", TESTDATA_FOLDER+"files.lst" );
                System.out.println( "done" );
            }
            catch( final Exception e ) {
                System.out.println("Error, failed to run, exception occurred: " + e );
                e.printStackTrace();
            }
        }

        /** run diff on single file pair */
        public void runDiff( final String folder1, final String file1, final String folder2, final String file2 ) throws Exception {


            final FileSystem fs = FileSystems.getDefault();
            final Path f1 = fs.getPath( folder1, file1 );
            final Path f2 = fs.getPath( folder2, file2 );

            final HtmlContentOutput contentOutput = startOutput( "diff-report-"+file2+".html" );

            printFileComparisonHeader( contentOutput, f1, f2 );

            runDiff( Files.newBufferedReader( f1 ),
                    Files.newBufferedReader( f2 ),
                    contentOutput );

            finishOutput( contentOutput.getHandler() );

        }

        /** run diff on two folders, with a listing file */
        public void runDiff( final String folder1, final String folder2, final String listFilesToCompare ) throws Exception {

            final List<String> fileList = collectLines( listFilesToCompare );

            final HtmlContentOutput contentOutput = startOutput( "diff-report.html" );

            for( final String fileName : fileList ) {
                System.out.println( "compare: " + fileName );

                final FileSystem fs = FileSystems.getDefault();
                final Path f1 = fs.getPath( folder1, fileName );
                final Path f2 = fs.getPath( folder2, fileName );

                printFileComparisonHeader( contentOutput, f1, f2 );

                runDiff( Files.newBufferedReader( f1 ),
                        Files.newBufferedReader( f2 ),
                        contentOutput );
            }

            finishOutput( contentOutput.getHandler() );
        }

        public void printFileComparisonHeader( final HtmlContentOutput contentOutput, final Path f1, final Path f2 ) {
            contentOutput.startFileHeader();
            contentOutput.write( "comparing: "+f1+" with "+f2 );
            contentOutput.endFileHeader();
        }

        public List<String> collectLines( final String listFilesToCompare ) {
            try( final BufferedReader br = Files.newBufferedReader( Paths.get( listFilesToCompare ) ) ) {
                return br.lines().collect( Collectors.toList() );
            }
            catch( final IOException e ) {
                throw new RuntimeException( "Failed to read listing file: " + e, e );
            }
        }

        public void finishOutput( final ContentHandler content ) throws Exception {
            content.endElement("", XSL_DIFF_PLACEHOLDER, XSL_DIFF_PLACEHOLDER);
            content.endElement("", XSL_PLACEHOLDER, XSL_PLACEHOLDER);
            content.endDocument();
        }

        public HtmlContentOutput startOutput(  final String outputFile ) throws Exception {
            final SAXTransformerFactory tf = XmlDomUtils.saxTransformerFactory();

            final TransformerHandler result = XmlDomUtils.newFragmentTransformerHandler( tf );
            result.setResult(new StreamResult(new File(outputFile)));

            final XslFilter filter = new XslFilter();
            final ContentHandler content = filter.xsl(result, HTML_TRANSFORMATION_XSL);

            content.startDocument();
            content.startElement("", XSL_PLACEHOLDER, XSL_PLACEHOLDER, new AttributesImpl());
            content.startElement("", XSL_DIFF_PLACEHOLDER, XSL_DIFF_PLACEHOLDER, new AttributesImpl());

            final HtmlContentOutput contentOutput = new HtmlContentOutput( content );
            return contentOutput;
        }

        public void runDiff( final Reader file1, final Reader file2, final HtmlContentOutput output ) {

            try {

                final DocumentBuilder docBuilder = XmlDomUtils.documentBuilder();
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
