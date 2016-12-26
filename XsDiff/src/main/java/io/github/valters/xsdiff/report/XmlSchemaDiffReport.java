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

import io.github.valters.xsdiff.format.SemanticDiffFormatter;

/** XML Schema (XSD) comparison/report generator */
public class XmlSchemaDiffReport {

    private final HtmlContentOutput output;

    public XmlSchemaDiffReport( final HtmlContentOutput output ) {
        this.output = output;
    }

    public void runDiff( final Document controlDoc, final Document testDoc ) {


        final XmlDiff xmlDiff = new XmlDiff( controlDoc, testDoc, new XmlSchemaDiffBuilder() );
        final SemanticDiffFormatter semanticDiff = new SemanticDiffFormatter();
        xmlDiff.run( output, semanticDiff );

        semanticDiff.printDiff( output );
    }

}
