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

package io.github.valters.xsdiff.format;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/** does not contribute any output: used to avoid nulls */
public class EmptyDiff implements ContentHandlerFormatter, DiffOutputFormatter {

    public static final EmptyDiff INSTANCE = new EmptyDiff();

    /** please use {@link #INSTANCE} */
    private EmptyDiff() {
        super();
    }

    private static final String EMPTY_CONTENT = "(empty)";

    @Override
    public void printDiff( final DiffOutput output ) {
        output.clearPart( EMPTY_CONTENT );
    }

    @Override
    public void printDiff( final ContentHandler resultHandler ) {
        try {
            resultHandler.characters( EMPTY_CONTENT.toCharArray(), 0, EMPTY_CONTENT.length() );
        }
        catch( final SAXException e ) {
            ;
        }
    }

}
