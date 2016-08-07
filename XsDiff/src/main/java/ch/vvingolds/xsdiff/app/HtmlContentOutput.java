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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** provides couple helpers to make html writing easier */
public class HtmlContentOutput {

    private final ContentHandler consumer;

    public HtmlContentOutput( final ContentHandler content ) {
        this.consumer = content;
    }


    public void write( final String str ) {
        try {
        consumer.startElement( "", "p", "p", noattrs() );

        final char[] chars = str.toCharArray();
        consumer.characters( chars, 0, chars.length );

        consumer.endElement( "", "p", "p" );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write paragraph: " + str + ", exception occurred: " + e );
            e.printStackTrace();
        }
    }

    /** transforms line ends to {@code <br>} breaks */
    public void writeLong(final String text) {
        try {
            final char[] c = text.toCharArray();

            final AttributesImpl noattrs = new AttributesImpl();

            for (int i = 0; i < c.length; i++) {
                switch (c[i]) {
                case '\n':
                    consumer.startElement("", "br", "br", noattrs);
                    consumer.endElement("", "br", "br");
                    consumer.characters("\n".toCharArray(), 0, "\n".length());
                    break;
                default:
                    consumer.characters(c, i, 1);
                }
            }
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write paragraph: " + text + ", exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public Attributes noattrs() {
        return new AttributesImpl();
    }


    /** for straight-through write */
    public ContentHandler getHandler() {
        return consumer;
    }

}
