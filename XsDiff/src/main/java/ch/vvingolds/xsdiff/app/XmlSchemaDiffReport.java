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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private NodeToString printNode = new NodeToString();

    private static boolean isAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getTarget() == null;
    }

    private static boolean isDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getTarget() == null;
    }

    public void runDiff( Document controlDoc, Document testDoc ) {

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
                printModifiedNode( testDoc, comparison );
            }
        }
    }

    private void printAddedNode( Document testDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getTestDetails();
        System.out.println( "ADDED <!-- xpath: " + details.getXPath() + " (parent node: "+details.getParentXPath()+" ) -->");
        System.out.println( printNode.nodeToString( details.getTarget() ) );
        System.out.println( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( testDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }

    private void printDeletedNode( Document controlDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        System.out.println( "DELETED <!-- xpath: " + details.getXPath() + " (parent node: "+details.getParentXPath()+" ) -->\n" );
        System.out.println( printNode.nodeToString( details.getTarget() ) );
        System.out.println( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( controlDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }


    private void printModifiedNode( Document testDoc, final Comparison comparison ) {

        final Comparison.Detail details = comparison.getControlDetails();
        if( XmlDomUtils.xpathDepth( details.getXPath() ) == 1 ) {
            System.out.println( "MODIFIED ; " + details.getXPath() + "." );
        }
        else {
            if( comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE ) {
                System.out.println( ". node order different: " + comparison.getTestDetails().getXPath() );
            }
            else if( comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH ) {
                int sizeControl = (int)comparison.getControlDetails().getValue();
                int sizeTest = (int)comparison.getTestDetails().getValue();
                if( sizeTest > sizeControl ) {
                    System.out.println( String.format( ". %s node(s) added: %s <!-- %s -->", sizeTest - sizeControl, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
                } else {
                    System.out.println( String.format( ". %s node(s) removed: %s <!-- %s -->", sizeControl - sizeTest, printNode.printNodeSignature( comparison.getTestDetails().getTarget() ), comparison.getTestDetails().getXPath() ) );
                }
            } 
            else if( comparison.getType() == ComparisonType.ATTR_NAME_LOOKUP ) {
                printNewAttr( comparison.getTestDetails() );
            }
            else {
                printNodeDiff( testDoc, comparison, details );
            }
        }
    }

    /** only info about new attr value */
    private void printNewAttr( final Detail detail ) {
        System.out.println( "MODIFIED ; new attribute [" + printNode.attrToString( detail.getTarget(), (QName)detail.getValue() ) + "] <!-- xpath: " + detail.getXPath() + " -->" );
    }

    private void printNodeDiff( Document testDoc, final Comparison comparison, final Comparison.Detail details ) {
        System.out.println( "MODIFIED ; " + comparison.toString() + "\n" );
        System.out.println( "- " + printNode.nodeToString( details.getTarget() ) );
        System.out.println( "+ " + printNode.nodeToString( comparison.getTestDetails().getTarget() ) );
   
        Node parentNode = xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() );
        System.out.println( printNode.printNodeWithParentInfo( parentNode, details.getParentXPath() ) );
    }

}
