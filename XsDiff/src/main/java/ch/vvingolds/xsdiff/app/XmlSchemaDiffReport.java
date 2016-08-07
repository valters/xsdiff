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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.DaisyDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private final XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private final NodeToString printNode = new NodeToString();
    private final ContentHtmlOutput output;

    public XmlSchemaDiffReport( final ContentHtmlOutput output ) {
        this.output = output;
    }

    private static boolean isAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getTarget() == null;
    }

    private static boolean isDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getTarget() == null;
    }

    public void runDiff( final Document controlDoc, final Document testDoc ) {

        final Diff xmlDiff = new XmlSchemaDiffBuilder().compare( controlDoc, testDoc );

        output.write( "TYPE ; XPATH ; OLD VALUE" );
        for( final Difference diff : xmlDiff.getDifferences() ) {
            final Comparison comparison = diff.getComparison();
            if( isAdded( comparison ) ) {
                printAddedNode( testDoc, comparison );
            }
            else if( isDeleted( comparison ) ) {
                printDeletedNode( controlDoc, comparison );
            }
            else {
                printModifiedNode( testDoc, controlDoc, comparison );
            }
        }
    }

    private void printAddedNode( final Document testDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getTestDetails();
        output.write( "ADDED <!-- xpath: " + details.getXPath() + " (parent node: "+details.getParentXPath()+" ) -->");
        output.writeLong( printNode.nodeToString( details.getTarget() ) );
        output.writeLong( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( testDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }

    private void printDeletedNode( final Document controlDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        output.write( "DELETED <!-- xpath: " + details.getXPath() + " (parent node: "+details.getParentXPath()+" ) -->" );
        output.writeLong( printNode.nodeToString( details.getTarget() ) );
        output.writeLong( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( controlDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }


    private void printModifiedNode( final Document testDoc, final Document controlDoc, final Comparison comparison ) {

        final Comparison.Detail details = comparison.getControlDetails();
        if( XmlDomUtils.xpathDepth( details.getXPath() ) == 1 ) {
            output.write( "MODIFIED ; " + details.getXPath() + "." );
        }
        else {
            if( comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE ) {
                output.write( ". node order different: " + comparison.getTestDetails().getXPath() );
            }
            else if( comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH ) {
                final int sizeControl = (int)comparison.getControlDetails().getValue();
                final int sizeTest = (int)comparison.getTestDetails().getValue();
                if( sizeTest > sizeControl ) {
                    // nodes added
                    output.write( String.format( ". %s node(s) added: %s <!-- %s -->", sizeTest - sizeControl, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
                }
                else {
                    // nodes removed
                    output.write( String.format( ". %s node(s) removed: %s <!-- %s -->", sizeControl - sizeTest, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
                }

                if( XmlDomUtils.xpathDepth( details.getXPath() ) > 2 ) {
                    final String oldText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, comparison.getControlDetails().getParentXPath() ) );
                    final String newText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() ) );
                    output.write( "~" );
                    daisyDiff( oldText, newText, output.getHandler() );
                    output.write( "~" );
                }

            }
            else if( comparison.getType() == ComparisonType.ATTR_NAME_LOOKUP ) {
                printNewAttr( comparison.getTestDetails() );
            }
            else {
                printNodeDiff( testDoc, comparison );
            }
        }
    }

    /** only info about new attr value */
    private void printNewAttr( final Detail detail ) {
        output.write( "MODIFIED ; new attribute [" + printNode.attrToString( detail.getTarget(), (QName)detail.getValue() ) + "] <!-- xpath: " + detail.getXPath() + " -->" );
    }

    private void printNodeDiff( final Document testDoc, final Comparison comparison ) {
        output.write( "MODIFIED ; " + comparison.toString() + "\n" );
        final String oldText = printNode.nodeToString( comparison.getControlDetails().getTarget() );
        final String newText = printNode.nodeToString( comparison.getTestDetails().getTarget() );
        output.write( "- " + oldText );
        output.write( "+ " + newText );

        output.write( "~" );
        daisyDiff( oldText, newText, output.getHandler() );
        output.write( "~" );

        final Node parentNode = xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() );
        output.writeLong( printNode.printNodeWithParentInfo( parentNode, comparison.getTestDetails().getParentXPath() ) );
    }

    private String daisyDiff( final String oldText, final String newText ) {

        try {
            final SAXTransformerFactory tf = XmlDomUtils.saxTransformerFactory();

            final TransformerHandler resultHandler = XmlDomUtils.newFragmentTransformerHandler( tf );

            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            resultHandler.setResult(new StreamResult( bytes ));

            daisyDiff( oldText, newText, resultHandler );

            return new String( bytes.toByteArray(), StandardCharsets.UTF_8 );
        }
        catch( final Exception e ) {
            return "(failed to daisydiff: "+e+")";
        }
    }

    public void daisyDiff( final String oldText, final String newText, final ContentHandler resultHandler ) {
        try {
            DaisyDiff.diffTag( oldText, newText, resultHandler );
        }
        catch( final Exception e ) {
            output.write( "(failed to daisydiff: "+e+")" );
        }
    }


}
