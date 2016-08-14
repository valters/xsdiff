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

package ch.vvingolds.xsdiff.format;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.google.common.base.Preconditions;

public class WikedDiffFormatter implements ContentHandlerFormatter {

    private final String oldText;
    private final String newText;

    private ContentHandler consumer;

    private static AtomicInteger id = new AtomicInteger(1000);

    private final String diffId;

    public WikedDiffFormatter( final String oldText, final String newText ) {
        this.oldText = oldText;
        this.newText = newText;
        this.diffId = "diff-inline-" + id.incrementAndGet();
    }

    @Override
    public void printDiff( final ContentHandler consumer ) {
        this.consumer = consumer;

        try {
            el("div", "id", diffId );
            el("script", "type", "text/javascript");
              writeRaw( " var wikEdDiff = new WikEdDiff();\n" );
              writeRaw( " var oldText = '"+ Base64.getEncoder().encodeToString( oldText.getBytes() )+"';\n" );
              writeRaw( " var newText = '"+ Base64.getEncoder().encodeToString( newText.getBytes() )+"';\n" );
              writeRaw( " var diffHtml = wikEdDiff.diff( B64.decode( oldText ), B64.decode( newText ) );\n" );
              writeRaw(" $('#"+diffId+"').html( diffHtml );\n" );
            _el("script");
            _el("div");
        }
        catch( final Exception e ) {
            // "(failed to daisydiff: "+e+")" );
        }
    }


    private void el( final String el, final String... attr ) {
        try {
            consumer.startElement( "", el, el, attrs(attr) );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write ["+el+"] element, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    private void _el( final String el ) {
        try {
            consumer.endElement( "", el, el );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write ["+el+"] element, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    private Attributes attrs( final String... attr) {
        Preconditions.checkNotNull( attr );
        Preconditions.checkArgument( 0 == attr.length % 2, "must have even number of params" );

        final AttributesImpl attrs = new AttributesImpl();
        for( int i = 0 ; i < attr.length; ) {
            attrs.addAttribute( "", attr[i], attr[i], "CDATA", attr[i+1] );
            i += 2;
        }
        return attrs;
    }

    public void writeRaw( final String str ) {
        try {
            final char[] chars = str.toCharArray();
            consumer.characters( chars, 0, chars.length );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write paragraph: " + str + ", exception occurred: " + e );
            e.printStackTrace();
        }
    }

}
