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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XmlDomUtils {

    private static final char XPATH_DELIMITER = '/';

    private XPath xpath = createXPath();

    /** get a namespace aware builder */
    public DocumentBuilder documentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setNamespaceAware( true );
        final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder;
    }

    /** make XPath with XMLSchema namespacing support */
    public XPath createXPath() {
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
        catch( XPathExpressionException e ) {
            throw new RuntimeException( "Failed to get node: [" + xpathExpr + "]", e );
        }
    }

    public static long countChars( String testString, char match ) {
        // CharMatcher.is('a').countIn("aaaab");
        return testString.codePoints().filter( ch -> ch == match ).count();
    }

    public static long xpathDepth( String xpathExpr ) {
        return countChars( xpathExpr, XPATH_DELIMITER );
    }
}
