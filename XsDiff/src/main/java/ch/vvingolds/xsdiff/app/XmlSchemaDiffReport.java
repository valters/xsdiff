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

import java.util.Collections;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import ch.vvingolds.xsdiff.format.DaisyDiffFormatter;
import ch.vvingolds.xsdiff.format.HistogramDiffFormatter;
import ch.vvingolds.xsdiff.format.SemanticDiffFormatter;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private final XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private final NodeToString printNode = new NodeToString();
    private final HtmlContentOutput output;
    private final SemanticDiffFormatter semanticDiff;

    public XmlSchemaDiffReport( final HtmlContentOutput output ) {
        this.output = output;
        this.semanticDiff = new SemanticDiffFormatter( output );
    }

    private static boolean isAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getTarget() == null;
    }

    private static boolean isDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getTarget() == null;
    }

    public void runDiff( final Document controlDoc, final Document testDoc ) {

        final Diff xmlDiff = new XmlSchemaDiffBuilder().compare( controlDoc, testDoc );

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

        output.write( "++ semantic adds ; removes --" );
        semanticDiff.printDiff();
    }

    private void printAddedNode( final Document testDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getTestDetails();
        final Node parentNode = xmlDomUtils.findNode( testDoc, details.getParentXPath() );

        final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, details.getXPath() ) );
        output.startSpan(  "ADDED <!-- xpath: " + details.getXPath() + " (parent node: "+printNode.printNodeSignature( parentNode )+" - "+details.getParentXPath()+" ) -->");
        output.writeRaw( "+ " );
        output.endSpan();
        output.addedPart( nodeText );
        output.newline();

        if( ! semanticDiff.markNodeAdded( details.getParentXPath(), nodeText, testDoc ) ) {
            output.write( "! holder for "+  details.getParentXPath() + " did not exist(?)");
            final String parentText = printNode.printNodeWithParentInfo( parentNode, details.getParentXPath() );
            output.writeLong( parentText );
            semanticDiff.markPartAdded( parentText, Collections.singletonList( nodeText ) );
        }
    }

    private void printDeletedNode( final Document controlDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        final Node parentNode = xmlDomUtils.findNode( controlDoc, details.getParentXPath() );
        output.startSpan(  "DELETED <!-- xpath: " + details.getXPath() + " (parent node: "+printNode.printNodeSignature( parentNode )+" - "+details.getParentXPath()+" ) -->" );
        output.writeRaw( "- " );
        output.endSpan();

        final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, details.getXPath() ) );
        output.removedPart( nodeText );
        output.newline();

        if( ! semanticDiff.markNodeRemoved( details.getParentXPath(), nodeText, controlDoc ) ) {
            output.write( "! (debug) holder for "+  details.getParentXPath() + " did not exist(?)");
            final String parentText = printNode.printNodeWithParentInfo( parentNode, details.getParentXPath() );
            output.writeLong( parentText );
            semanticDiff.markPartRemoved( parentText, Collections.singletonList( nodeText ) );
        }
    }

    private void printModifiedNode( final Document testDoc, final Document controlDoc, final Comparison comparison ) {

        final Comparison.Detail details = comparison.getControlDetails();
        if( XmlDomUtils.xpathDepth( details.getXPath() ) == 1 ) {
            output.write( "MODIFIED ; " + details.getXPath() + "." );
        }
        else {
            if( comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE ) {
                output.startSpan( ". node order different: " + comparison.getTestDetails().getXPath() );
                output.writeRaw( " * " );
                output.endSpan();
            }
            else if( comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH ) {
                printChildNodesChanged( testDoc, controlDoc, comparison );
            }
            else if( comparison.getType() == ComparisonType.ATTR_NAME_LOOKUP ) {
                printNewAttr( comparison.getTestDetails() );
            }
            else {
                printNodeDiff( testDoc, comparison );
            }
        }
    }

    public void printChildNodesChanged( final Document testDoc, final Document controlDoc, final Comparison comparison ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( comparison.getTestDetails().getXPath() );
        final boolean shouldTakeParent = xpathDepth > 2;

        final int sizeControl = (int)comparison.getControlDetails().getValue();
        final int sizeTest = (int)comparison.getTestDetails().getValue();
        if( sizeTest > sizeControl ) {
            // nodes added
            output.startSpan( String.format( ". %s node(s) added: %s <!-- %s -->", sizeTest - sizeControl, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
            output.writeRaw( " * " );
            output.endSpan();

            semanticDiff.addChangeHolder( semanticDiff.opAdded(), comparison.getTestDetails().getXPath(), holderNodeText( testDoc, comparison.getTestDetails() ) );
        }
        else {
            // nodes removed
            output.startSpan( String.format( ". %s node(s) removed: %s <!-- %s -->", sizeControl - sizeTest, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
            output.writeRaw( " * " );
            output.endSpan();

            semanticDiff.addChangeHolder( semanticDiff.opRemoved(), comparison.getControlDetails().getXPath(), holderNodeText( controlDoc, comparison.getControlDetails() ) );
        }

        if( shouldTakeParent ) {
            final String oldText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, comparison.getControlDetails().getParentXPath() ) );
            final String newText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() ) );
            output.write( "~ daisy" );
            new DaisyDiffFormatter( output.getHandler() ).printDiff( oldText, newText );
            output.write( "~" );

            output.write( "# histogram" );
            new HistogramDiffFormatter( output ).printDiff( oldText, newText );
            output.write( "#" );

        }
    }

    /** this one is clever enough to expand node text up to parent node scope, to provide interesting context when changes are printed */
    private String holderNodeText( final Document doc, final Detail details ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( details.getXPath() );
        final boolean shouldTakeParent = xpathDepth > 2;
        final String xpathExpr = shouldTakeParent ? details.getParentXPath() : details.getXPath();
        final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( doc, xpathExpr ) );
        return nodeText;
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
        new DaisyDiffFormatter( output.getHandler() ).printDiff( oldText, newText );
        output.write( "~" );

        final Node parentNode = xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() );
        output.writeLong( printNode.printNodeWithParentInfo( parentNode, comparison.getTestDetails().getParentXPath() ) );
    }

}
