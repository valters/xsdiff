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

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class TransformToString {

    /** ask to pretty-print XML (indentation) */
    public static final String XSLT_INDENT_PROP = "{http://xml.apache.org/xslt}indent-amount";

    /** ask transformer to pretty-print the output: works with Java built-in XML engine */
    public void setTransformerIndent( final Transformer transformer ) {
        try {
            transformer.setOutputProperty(XSLT_INDENT_PROP, "4");
        } catch( IllegalArgumentException e ) {
            System.err.println( "indent-amount not supported: {}"+ e.toString() ); // ignore error, don't print stack-trace
        }
    }

    public String nodeToString( final Node node ) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); // see http://www.w3.org/TR/xslt#output
            transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );

            final StringWriter stw = new StringWriter();
            transformer.transform( new DOMSource( node ), new StreamResult( stw ) );
            return stw.toString();
        }
        catch( TransformerException | TransformerFactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "{failed}";
        }
    }

}
