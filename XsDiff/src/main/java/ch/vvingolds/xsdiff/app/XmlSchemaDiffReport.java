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

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import ch.vvingolds.xsdiff.format.NodeChangesHolder;
import ch.vvingolds.xsdiff.format.SemanticDiffFormatter;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private final XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private final NodeToString printNode = new NodeToString();
    private final HtmlContentOutput output;
    private final SemanticDiffFormatter semanticDiff;

    public XmlSchemaDiffReport( final HtmlContentOutput output ) {
        this.output = output;
        this.semanticDiff = new SemanticDiffFormatter();
    }

    private static boolean isAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getTarget() == null;
    }

    private static boolean isDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getTarget() == null;
    }

    private static boolean isAttrAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getValue() == null;
    }

    private static boolean isAttrDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getValue() == null;
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
        semanticDiff.printDiff( output );
    }

    private void printAddedNode( final Document testDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getTestDetails();
        final Node parentNode = xmlDomUtils.findNode( testDoc, details.getParentXPath() );

        final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, details.getXPath() ) );
        output.startSpan(  "ADDED <!-- xpath: " + details.getXPath() + " (parent node: "+printNode.printNodeSignature( parentNode )+" - "+details.getParentXPath()+" ) -->");
        output.writeRaw( "+ " );
        output.endSpan();
// don't need to output anything
//~        output.addedPart( nodeText );
//~        output.newline();

        if( ! semanticDiff.markNodeAdded( XmlDomUtils.wideContext( details.getParentXPath() ), nodeText, testDoc ) ) {
            semanticDiff.markNodeAdded( details.getXPath(), nodeText, testDoc ); // make sure change is not lost
        }
    }

    private void printDeletedNode( final Document controlDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        final Node parentNode = xmlDomUtils.findNode( controlDoc, details.getParentXPath() );
        output.startSpan(  "DELETED <!-- xpath: " + details.getXPath() + " (parent node: "+printNode.printNodeSignature( parentNode )+" - "+details.getParentXPath()+" ) -->" );
        output.writeRaw( "- " );
        output.endSpan();

        final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, details.getXPath() ) );
// don't need to output anything
//~        output.removedPart( nodeText );
//~        output.newline();

        if( ! semanticDiff.markNodeRemoved( XmlDomUtils.wideContext( details.getParentXPath() ), nodeText, controlDoc ) ) {
            semanticDiff.markNodeRemoved( details.getXPath(), nodeText, controlDoc ); // make sure change is not lost
        }
    }

    private void printModifiedNode( final Document testDoc, final Document controlDoc, final Comparison comparison ) {

        final Comparison.Detail details = comparison.getControlDetails();
        if( XmlDomUtils.xpathDepth( details.getXPath() ) == 1 ) {
            output.startSpan( "MODIFIED ; " + details.getXPath() + "." );
            output.writeRaw( " _ " );
            output.endSpan();
        }
        else {
            if( comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE ) {
                output.startSpan( ". node order different: " + comparison.getTestDetails().getXPath() );
                output.writeRaw( " * " );
                output.endSpan();
            }
            else if( comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH ) {
                printChildNodesChanged( comparison, controlDoc, testDoc );
            }
            else if( comparison.getType() == ComparisonType.ATTR_NAME_LOOKUP || comparison.getType() == ComparisonType.ATTR_VALUE ) {
                printAttrChanged( comparison, controlDoc, testDoc );
            }
            else {
                printNodeDiff( comparison, testDoc );
            }
        }
    }

    public void printChildNodesChanged( final Comparison comparison, final Document controlDoc, final Document testDoc ) {
        final String parentNodeXpath = comparison.getTestDetails().getXPath();

        final int sizeControl = (int)comparison.getControlDetails().getValue();
        final int sizeTest = (int)comparison.getTestDetails().getValue();
        if( sizeTest > sizeControl ) {
            // nodes added
            output.startSpan( String.format( ". %s node(s) added: %s <!-- %s -->", sizeTest - sizeControl, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), parentNodeXpath ) );
            output.writeRaw( " * " );
            output.endSpan();
        }
        else {
            // nodes removed
            output.startSpan( String.format( ". %s node(s) removed: %s <!-- %s -->", sizeControl - sizeTest, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), parentNodeXpath ) );
            output.writeRaw( " * " );
            output.endSpan();
        }

        final NodeChangesHolder changeHolder = semanticDiff.updateHolder( semanticDiff.addChangeHolder( XmlDomUtils.wideContext( parentNodeXpath ) ), NodeChangesHolder.OpType.ADDED, holderNodeText( testDoc, comparison.getTestDetails() ) );
        semanticDiff.updateHolder( changeHolder, NodeChangesHolder.OpType.REMOVED, holderNodeText( controlDoc, comparison.getControlDetails() ) );
        changeHolder.attachAutoDiffs();
        semanticDiff.updateHolder( semanticDiff.addChangeHolder( XmlDomUtils.wideContext( comparison.getControlDetails().getXPath() ) ), NodeChangesHolder.OpType.REMOVED, holderNodeText( controlDoc, comparison.getControlDetails() ) );
    }

    /** this one is clever enough to expand node text up to parent node scope, to provide interesting context when changes are printed */
    private String holderNodeText( final Document doc, final Detail details ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( details.getXPath() );
        final boolean shouldTakeParent = xpathDepth > 2;
        final String xpathExpr = shouldTakeParent ? XmlDomUtils.wideContext( details.getParentXPath() ) : details.getXPath();
        final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( doc, xpathExpr ) );
        return nodeText;
    }

    /** only info about new attr value
     * @param controlDoc
     * @param testDoc*/
    private void printAttrChanged( final Comparison comparison, final Document controlDoc, final Document testDoc ) {
        if( isAttrAdded( comparison ) ) {
            final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() ) );
            final String attributeText = printNode.attrToString( comparison.getTestDetails().getTarget(), (QName)comparison.getTestDetails().getValue() );
            output.startSpan( "MODIFIED ; new attribute [" + attributeText + "] <!-- xpath: " + comparison.getTestDetails().getXPath() + " -->" );
            output.writeRaw( " . " );
            output.endSpan();
            final String parentNodeXpath = XmlDomUtils.wideContext( comparison.getTestDetails().getXPath() );
            final NodeChangesHolder changeHolder = semanticDiff.updateHolder( semanticDiff.addChangeHolder( parentNodeXpath ), NodeChangesHolder.OpType.ADDED, holderNodeText( testDoc, comparison.getTestDetails() ) );
            changeHolder.addedAttribute( nodeText, attributeText );
            // add the second part of parent text for the conventional diffs
            semanticDiff.updateHolder( changeHolder, NodeChangesHolder.OpType.REMOVED, holderNodeText( controlDoc, comparison.getControlDetails() ) );
            changeHolder.attachAutoDiffs();

        }
        else if( isAttrDeleted( comparison ) ) {
            final String controlNodeText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, comparison.getControlDetails().getParentXPath() ) );
            final String controlAttributeText = printNode.attrToString( comparison.getControlDetails().getTarget(), (QName)comparison.getControlDetails().getValue() );
            output.startSpan( "MODIFIED ; removed attribute [" + controlAttributeText + "] <!-- xpath: " + comparison.getControlDetails().getXPath() + " -->" );
            output.writeRaw( " . " );
            output.endSpan();

            final String parentNodeXpath = XmlDomUtils.wideContext( comparison.getControlDetails().getXPath() );
            final NodeChangesHolder changeHolder = semanticDiff.updateHolder( semanticDiff.addChangeHolder( parentNodeXpath ), NodeChangesHolder.OpType.REMOVED, holderNodeText( controlDoc, comparison.getControlDetails() ) );
            changeHolder.removedAttribute( controlNodeText, controlAttributeText );
            // add the second part of parent text for the conventional diffs
            semanticDiff.updateHolder( changeHolder, NodeChangesHolder.OpType.ADDED, holderNodeText( testDoc, comparison.getTestDetails() ) );
        }
        else {
            // modified in place
            final String nodeText = printNode.nodeToString( xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() ) );
            final String attributeText = printNode.attrToString( (Attr)comparison.getTestDetails().getTarget() );
            output.startSpan( "MODIFIED ; changed attribute [" + attributeText + "] <!-- xpath: " + comparison.getTestDetails().getXPath() + " -->" );
            output.writeRaw( " . " );
            output.endSpan();
            final String parentNodeXpath = XmlDomUtils.wideContext( comparison.getTestDetails().getXPath() );
            final NodeChangesHolder changeHolder = semanticDiff.updateHolder( semanticDiff.addChangeHolder( parentNodeXpath ), NodeChangesHolder.OpType.ADDED, holderNodeText( testDoc, comparison.getTestDetails() ) );
            changeHolder.addedAttribute( nodeText, attributeText );

            final String controlNodeText = printNode.nodeToString( xmlDomUtils.findNode( controlDoc, comparison.getControlDetails().getParentXPath() ) );
            final String controlAttributeText = printNode.attrToString( (Attr)comparison.getControlDetails().getTarget() );
            changeHolder.removedAttribute( controlNodeText, controlAttributeText );
            // add the second part of parent text for the conventional diffs
            semanticDiff.updateHolder( changeHolder, NodeChangesHolder.OpType.REMOVED, holderNodeText( controlDoc, comparison.getControlDetails() ) );
            changeHolder.attachAutoDiffs();
        }

    }

    private void printNodeDiff( final Comparison comparison, final Document testDoc ) {
        final String oldText = printNode.nodeToString( comparison.getControlDetails().getTarget() );
        final String newText = printNode.nodeToString( comparison.getTestDetails().getTarget() );

        printFullNodeDiff( testDoc, comparison, oldText, newText );
    }

    private void printFullNodeDiff( final Document testDoc, final Comparison comparison, final String oldText, final String newText ) {
        output.startSpan( "NODE MODIFIED ["+comparison.getType()+"] ; " + comparison.toString() + "\n" );
        output.writeRaw( " . " );
        output.endSpan();

//        output.write( "- " + oldText );
//        output.write( "+ " + newText );
//
//        output.write( "~" );
//        new DaisyDiffFormatter( oldText, newText ).printDiff( output.getHandler() );
//        output.write( "~" );
//
//        printParentInfo( testDoc, comparison.getTestDetails().getParentXPath() );
    }

}
