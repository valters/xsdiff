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

import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

/** Configures XmlUnit DiffBuilder to be XML Schema (XSD) semantics aware */
public class XmlSchemaDiffBuilder {

    public Diff compare( Document controlDoc, Document testDoc ) {

        ElementSelector schemaElementSelector = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed( "element" ).thenUse( ElementSelectors.byNameAndAttributes( "name" ) )
                .elseUse( ElementSelectors.byName )
                .build();

        ElementSelector simpleTypeSelector = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed( "simpleType" ).thenUse( ElementSelectors.byNameAndAttributes( "name" ) )
                .elseUse( schemaElementSelector )
                .build();

        ElementSelector complexTypeSelector = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed( "complexType" ).thenUse( ElementSelectors.byNameAndAttributes( "name" ) )
                .elseUse( simpleTypeSelector )
                .build();


        Diff xmlDiff = DiffBuilder.compare( Input.fromDocument( controlDoc ) )
                .withTest( Input.fromDocument( testDoc ) )
                .ignoreWhitespace()
                .ignoreComments()
                .checkForSimilar()
                .checkForIdentical()
                .withNodeMatcher( new DefaultNodeMatcher( complexTypeSelector ) )
                .withNamespaceContext( new StaticNamespaceContext().prefixToUri() )
                .build();

        return xmlDiff;
    }
}
