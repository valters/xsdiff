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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;

/** @deprecated not useful */
@Deprecated
public class NodeToStringDom {

    /**
     * Converts a Node into a String using the DOM, level 3, Load/Save serializer.
     *
     * @param node the node to be written to a string
     *
     * @return the string representation of the node
     */
    public static String nodeToString( final Node node ) {
        final StringWriter writer = new StringWriter();
        writeNode( node, writer );
        writer.append( "simple:" ).append( String.valueOf( node ) );
        return writer.toString();
    }

    /**
     * Writes a Node out to a Writer using the DOM, level 3, Load/Save serializer. The written content is encoded using
     * the encoding specified in the writer configuration.
     *
     * @param node the node to write out
     * @param output the writer to write the XML to
     */
    public static void writeNode( final Node node, final Writer output ) {
        writeNode( node, output, null );
    }

    /**
     * Writes a Node out to a Writer using the DOM, level 3, Load/Save serializer. The written content is encoded using
     * the encoding specified in the writer configuration.
     *
     * @param node the node to write out
     * @param output the writer to write the XML to
     * @param serializerParams parameters to pass to the {@link DOMConfiguration} of the serializer
     *         instance, obtained via {@link LSSerializer#getDomConfig()}. May be null.
     */
    public static void writeNode( final Node node, final Writer output, final Map<String, Object> serializerParams ) {
        if( node == null ) {
            try {
                output.append( "[null]" );
            }
            catch( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        final DOMImplementationLS domImplLS = getLSDOMImpl( node );

        final LSSerializer serializer = getLSSerializer( domImplLS, serializerParams );

        final LSOutput serializerOut = domImplLS.createLSOutput();
        serializerOut.setCharacterStream( output );

        serializer.write( node, serializerOut );
    }

    /**
     * Get the DOM Level 3 Load/Save {@link DOMImplementationLS} for the given node.
     *
     * @param node the node to evaluate
     * @return the DOMImplementationLS for the given node
     */
    public static DOMImplementationLS getLSDOMImpl( final Node node ) {
        DOMImplementation domImpl;
        if( node instanceof Document ) {
            domImpl = ( (Document) node ).getImplementation();
        }
        else {
            domImpl = node.getOwnerDocument().getImplementation();
        }

        final DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl.getFeature( "LS", "3.0" );
        return domImplLS;
    }

    /**
     * Obtain a the DOM, level 3, Load/Save serializer {@link LSSerializer} instance from the
     * given {@link DOMImplementationLS} instance.
     *
     * <p>
     * The serializer instance will be configured with the parameters passed as the <code>serializerParams</code>
     * argument. It will also be configured with an {@link LSSerializerFilter} that shows all nodes to the filter,
     * and accepts all nodes shown.
     * </p>
     *
     * @param domImplLS the DOM Level 3 Load/Save implementation to use
     * @param serializerParams parameters to pass to the {@link DOMConfiguration} of the serializer
     *         instance, obtained via {@link LSSerializer#getDomConfig()}. May be null.
     *
     * @return a new LSSerializer instance
     */
    public static LSSerializer getLSSerializer( final DOMImplementationLS domImplLS, final Map<String, Object> serializerParams ) {
        final LSSerializer serializer = domImplLS.createLSSerializer();

        serializer.setFilter( new LSSerializerFilter() {

            @Override
            public short acceptNode( final Node arg0 ) {
                return FILTER_ACCEPT;
            }

            @Override
            public int getWhatToShow() {
                return SHOW_ALL;
            }
        } );

        if( serializerParams != null ) {
            final DOMConfiguration serializerDOMConfig = serializer.getDomConfig();
            for( final String key : serializerParams.keySet() ) {
                serializerDOMConfig.setParameter( key, serializerParams.get( key ) );
            }
        }

        return serializer;
    }

}
