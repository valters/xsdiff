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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public class NodeToString {

    private final TransformToString transform = new TransformToString();

    /** remove all possible (invisible) whitespace: including line breaks. used to see if document is actually empty and alternative printer should be used. */
    public String trim( final String str ) {
        return CharMatcher.invisible().trimFrom( Strings.nullToEmpty( str ) );
    }

    public String nodeToString( final Node node ) {
        final String str = trim( transform.nodeToStringClean( node ) );
        if( ! Strings.isNullOrEmpty( str ) ) {
            return str;
        }
        return altPrint( node ); // is probably attribute
    }

    /** simple print when clever fails */
    private String altPrint( final Node node ) {
        return "<span>"+String.valueOf( node )+"</span>";
    }

    private String printAttributes( final NamedNodeMap attributes ) {
        if( attributes == null || attributes.getLength() == 0 ) {
            return ""; // nothing
        }

        final StringBuilder b = new StringBuilder();
        for( int i = 0; i < attributes.getLength(); i++ ) {
            b.append( "@" ).append( attributes.item( i ) );
        }
        return b.toString();
    }

    public String printNodeParentInfo( final Node node ) {
        final Node parentNode = node.getParentNode();
        if( parentNode == null ) {
            return "(no parent node)";
        }

        if( parentNode.getOwnerDocument() == null ) {
            return "(attached to root)";
        }

        return printNodeSignature( parentNode );
    }

    public String printNodeSignature( final Node node ) {
        return "<"+node.getNodeName() + " " + printAttributes( node.getAttributes() ) +">";
    }


    public String printNodeWithParentInfo( final Node parentNode, final String parentXpath ) {
        final StringBuilder b = new StringBuilder();
        if( XmlDomUtils.xpathDepth( parentXpath )  < 2 ) {
            b.append( "<-- " ).append( printNodeParentInfo( parentNode ) );
            return b.append( parentXpath ).append( ". -->" ).toString();
        }

        b.append( printNodeParentInfo( parentNode ) );
        return b.append( "\n        " )
                .append( nodeToString( parentNode ) ).append( "  <!-- by xpath: " ).append( parentXpath ).append( " -->\n\n" )
                .toString();
    }

    public String attrToString( final Node node, final QName value ) {
        if( value == null ) {
            return "(null)";
        }
        if( Strings.isNullOrEmpty( value.getNamespaceURI() ) ) {
            return String.valueOf( node.getAttributes().getNamedItem( value.getLocalPart() ) );
        }
        else {
            return String.valueOf( node.getAttributes().getNamedItemNS( value.getNamespaceURI(), value.getLocalPart() ) );
        }
    }

}
