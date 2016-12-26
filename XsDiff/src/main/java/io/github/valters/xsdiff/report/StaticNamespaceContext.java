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

package io.github.valters.xsdiff.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class StaticNamespaceContext implements NamespaceContext {

    /** prefix used when importing the XML_SCHEMA namespace. MOXy uses "xsd", JAXB RI uses "xs" */
    public static final String SCHEMA_NS_PREFIX = "xs";

    HashMap<String,String> namespaceToPrefix = new HashMap<String, String>();
    HashMap<String,String> prefixToNamespace = new HashMap<String, String>();

    public StaticNamespaceContext() {
        super();
        addNs( XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI );
        addNs( XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI );
        addNs( SCHEMA_NS_PREFIX, XMLConstants.W3C_XML_SCHEMA_NS_URI );
    }

    private void addNs( String prefix, String namespace) {
        namespaceToPrefix.put( namespace, prefix );
        prefixToNamespace.put( prefix, namespace );
    }

    public String getNamespaceURI( String prefix ) {
        return prefixToNamespace.get( prefix );
    }

    public String getPrefix( String namespaceURI ) {
        return namespaceToPrefix.get( namespaceURI );
    }

    @SuppressWarnings("rawtypes")
    public Iterator getPrefixes( String namespaceURI ) {
        return prefixToNamespace.keySet().iterator();
    }

    public Map<String, String> prefixToUri() {
        return prefixToNamespace;
    }

}
