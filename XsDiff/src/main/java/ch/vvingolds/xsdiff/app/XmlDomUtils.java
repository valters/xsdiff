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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;

public class XmlDomUtils {

    /** XML namespace declaration attribute */
    private static final String ATTR_XMLNS = "xmlns";

    /** delimiter between node name and ns prefix ("xs:element") */
    private static final char NAMESPACE_PREFIX = ':';

    private static final char XPATH_DELIMITER = '/';

    private final XPath xpath = createXPath();

    /** get a namespace aware builder */
    public static DocumentBuilder documentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setNamespaceAware( true );
        final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder;
    }

    /** make XPath with XMLSchema namespacing support */
    public static XPath createXPath() {
        final XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext( new StaticNamespaceContext() );
        return xp;
    }

    public Node findNode( final Document doc, final String xpathExpr ) {
        try {
            final Node node = (Node) xpath.evaluate( xpathExpr, doc, XPathConstants.NODE );
            if( node == null ) {
                System.out.println( "Failed to get node: [" + xpathExpr + "] from [" + doc + "]" );
            }
            return node;
        }
        catch( final XPathExpressionException e ) {
            throw new RuntimeException( "Failed to get node: [" + xpathExpr + "]", e );
        }
    }

    public static long countChars( final String testString, final char match ) {
        return testString.codePoints().filter( ch -> ch == match ).count();
    }

    public static long xpathDepth( final String xpathExpr ) {
        if( Strings.isNullOrEmpty( xpathExpr ) ) {
            return 0; // nothing
        }

        return countChars( xpathExpr, XPATH_DELIMITER );
    }

    /** ask to pretty-print XML (indentation) */
    public static final String XSLT_INDENT_PROP = "{http://xml.apache.org/xslt}indent-amount";

    /** ask transformer to pretty-print the output: works with Java built-in XML engine */
    public static void setTransformerIndent( final Transformer transformer ) {
        try {
            transformer.setOutputProperty(XSLT_INDENT_PROP, "4");
        } catch( final IllegalArgumentException e ) {
            System.err.println( "indent-amount not supported: {}"+ e.toString() ); // ignore error, don't print stack-trace
        }
    }

    public static void setFactoryIndent( final TransformerFactory tf ) {
        try {
            tf.setAttribute("indent-number", new Integer(2));
        } catch( final IllegalArgumentException e ) {
            System.err.println( "indent-number not supported: {}"+ e.toString() ); // ignore error, don't print stack-trace
        }
    }

    public static void outputStandaloneFragment( final Transformer transformer ) {
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
    }

    public static void setIndentFlag( final Transformer transformer ) {
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); // see http://www.w3.org/TR/xslt#output
    }

    public static void setUtfEncoding( final Transformer transformer ) {
        transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
    }

    public static TransformerFactory transformerFactory() throws TransformerFactoryConfigurationError {
        final TransformerFactory tf = TransformerFactory.newInstance();
        XmlDomUtils.setFactoryIndent( tf );
        return tf;
    }

    /** set up transformer to output a standalone "fragment" - suppressing xml declaration */
    public static Transformer newFragmentTransformer( final TransformerFactory tf ) throws TransformerConfigurationException {
        final Transformer transformer = tf.newTransformer();
        setUtfEncoding( transformer );
        setIndentFlag( transformer );
        setTransformerIndent( transformer );
        outputStandaloneFragment( transformer );
        return transformer;
    }

    public static SAXTransformerFactory saxTransformerFactory() throws TransformerFactoryConfigurationError {
        final SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        XmlDomUtils.setFactoryIndent( tf );
        return tf;
    }

    /** set up transformer to output a standalone "fragment" - suppressing xml declaration */
    public static TransformerHandler newFragmentTransformerHandler( final SAXTransformerFactory tf ) throws TransformerConfigurationException {
        final TransformerHandler resultHandler = tf.newTransformerHandler();
        final Transformer transformer = resultHandler.getTransformer();
        setUtfEncoding( transformer );
        setIndentFlag( transformer );
        setTransformerIndent( transformer );
        outputStandaloneFragment( transformer );
        return resultHandler;
    }

    /**
     * Recursively strips the namespaces from a node.
     * @param node the starting node.
     */
    public static Node removeNamespaceRecursive( final Node node, final Document document ) {
        Node newNode = null;

        if( node.getNodeType() == Node.ELEMENT_NODE ) {
            newNode = document.renameNode( node, null, removeNsPrefix( node.getNodeName() ) );
        }

        final NodeList list = node.getChildNodes();
        for( int i = 0; i < list.getLength(); ++i ) {
            removeNamespaceRecursive( list.item( i ), document );
        }

        return newNode;
    }

    /** strip the namespace prefix from node name, if any. this avoid the Document strict validation error, that node should not have prefix when it does not the have associated namespace */
    public static String removeNsPrefix( final String nodeName ) {
        final int colonAt = nodeName.indexOf( NAMESPACE_PREFIX );
        if( colonAt >= 0 ) {
            return nodeName.substring( colonAt+1, nodeName.length() );
        }

        return nodeName;
    }

    /** remove stray "xmlns" default namespace element that seems to get left over even after removing namespacing from nodes */
    public static void removeXmlNsAttribute( final Node node ) {
        final NamedNodeMap attr = node.getAttributes();
        for( int i = 0; i < attr.getLength(); i++ ) {
            final Node item = attr.item( i );
            if( ATTR_XMLNS.equals( item.getNodeName() ) ) {
                attr.removeNamedItem( ATTR_XMLNS );
                return;
            }
        }
    }

    /** simply get one level of xpath higher: if xpath is long enough. do not travel above 2nd level ("/a/b" node) because we don't want whole "file" as context */
    public static String widerContext( final String xpath ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( xpath );
        if( xpathDepth <= 2 ) {
            return xpath; // do nothing
        }

        final int cutTo = xpath.lastIndexOf( XPATH_DELIMITER );
        return xpath.substring( 0, cutTo );
    }

    /** return the "wide" context - get up to top level node (2 levels deep) */
    public static String wideContext( final String xpath ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( xpath );
        if( xpathDepth <= 2 ) {
            return xpath; // do nothing
        }

        final int cutTo = xpath.lastIndexOf( XPATH_DELIMITER );
        return wideContext( xpath.substring( 0, cutTo ) );
    }

}
