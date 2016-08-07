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
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private final XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private final NodeToString printNode = new NodeToString();

    private static boolean isAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getTarget() == null;
    }

    private static boolean isDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getTarget() == null;
    }

    public void runDiff( final Document controlDoc, final Document testDoc ) {

        final Diff xmlDiff = new XmlSchemaDiffBuilder().compare( controlDoc, testDoc );

        System.out.println( "TYPE ; XPATH ; OLD VALUE" );
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
        System.out.println( "ADDED <!-- xpath: " + details.getXPath() + " (parent node: "+details.getParentXPath()+" ) -->");
        System.out.println( printNode.nodeToString( details.getTarget() ) );
        System.out.println( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( testDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }

    private void printDeletedNode( final Document controlDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        System.out.println( "DELETED <!-- xpath: " + details.getXPath() + " (parent node: "+details.getParentXPath()+" ) -->\n" );
        System.out.println( printNode.nodeToString( details.getTarget() ) );
        System.out.println( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( controlDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }


    private void printModifiedNode( final Document testDoc, final Document controlDoc, final Comparison comparison ) {

        final Comparison.Detail details = comparison.getControlDetails();
        if( XmlDomUtils.xpathDepth( details.getXPath() ) == 1 ) {
            System.out.println( "MODIFIED ; " + details.getXPath() + "." );
        }
        else {
            if( comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE ) {
                System.out.println( ". node order different: " + comparison.getTestDetails().getXPath() );
            }
            else if( comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH ) {
                final int sizeControl = (int)comparison.getControlDetails().getValue();
                final int sizeTest = (int)comparison.getTestDetails().getValue();
                if( sizeTest > sizeControl ) {
                    // nodes added
                    System.out.println( String.format( ". %s node(s) added: %s <!-- %s -->", sizeTest - sizeControl, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
                }
                else {
                    // nodes removed
                    System.out.println( String.format( ". %s node(s) removed: %s <!-- %s -->", sizeControl - sizeTest, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
                }

                if( XmlDomUtils.xpathDepth( details.getXPath() ) > 2 ) {
                    final String oldText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, comparison.getControlDetails().getParentXPath() ) );
                    final String newText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() ) );
                    System.out.println( "~" );
                    System.out.println( daisyDiff( oldText, newText) );
                    System.out.println( "~" );
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
        System.out.println( "MODIFIED ; new attribute [" + printNode.attrToString( detail.getTarget(), (QName)detail.getValue() ) + "] <!-- xpath: " + detail.getXPath() + " -->" );
    }

    private void printNodeDiff( final Document testDoc, final Comparison comparison ) {
        System.out.println( "MODIFIED ; " + comparison.toString() + "\n" );
        final String oldText = printNode.nodeToString( comparison.getControlDetails().getTarget() );
        final String newText = printNode.nodeToString( comparison.getTestDetails().getTarget() );
        System.out.println( "- " + oldText );
        System.out.println( "+ " + newText );

        System.out.println( "~" );
        System.out.println( daisyDiff( oldText, newText) );
        System.out.println( "~" );

        final Node parentNode = xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() );
        System.out.println( printNode.printNodeWithParentInfo( parentNode, comparison.getTestDetails().getParentXPath() ) );
    }

    private String daisyDiff( final String oldText, final String newText ) {

        try {
            final SAXTransformerFactory tf = XmlDomUtils.saxTransformer();

            final TransformerHandler resultHandler = XmlDomUtils.newFragmentTransformerHandler( tf );

            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            resultHandler.setResult(new StreamResult( bytes ));

            DaisyDiff.diffTag( oldText, newText, resultHandler );

            return new String( bytes.toByteArray(), StandardCharsets.UTF_8 );
        }
        catch( final Exception e ) {
            return "(failed to daisydiff: "+e+")";
        }
    }


}
