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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private NodeToString printNode = new NodeToString();

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
        System.out.println( "ADDED ; " + details.getXPath() + " ; " + printNode.nodeToString( details.getTarget() ) );
        System.out.println( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( testDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }

    private void printDeletedNode( Document controlDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        System.out.println( "DELETED ; " + details.getXPath() + " ; " + printNode.nodeToString( details.getTarget() ) );
        System.out.println( printNode.printNodeWithParentInfo( xmlDomUtils.findNode( controlDoc, details.getParentXPath() ), details.getParentXPath() ) );
    }


    private void printModifiedNode( Document testDoc, final Comparison comparison ) {
        final Comparison.Detail details = comparison.getControlDetails();
        if( XmlDomUtils.xpathDepth( details.getXPath() ) == 1 ) {
            System.out.println( "MODIFIED ; " + details.getXPath() + "." );
        }
        else {
            System.out.println( "MODIFIED ; " + details.getXPath() + " = was: " + printNode.nodeToString( details.getTarget() ) + ", new value: " + printNode.nodeToString( comparison.getTestDetails().getTarget() ) );
            Node parentNode = xmlDomUtils.findNode( testDoc, comparison.getTestDetails().getParentXPath() );
            System.out.println( printNode.printNodeWithParentInfo( parentNode, details.getParentXPath() ) );
        }
    }

    private static boolean isAdded( final Comparison comparison ) {
        return comparison.getControlDetails().getTarget() == null;
    }

    private static boolean isDeleted( final Comparison comparison ) {
        return comparison.getTestDetails().getTarget() == null;
    }

}
