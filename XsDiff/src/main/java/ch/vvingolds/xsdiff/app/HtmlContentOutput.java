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

import java.util.function.Consumer;

import org.outerj.daisy.diff.tag.TagSaxDiffOutput;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import ch.vvingolds.xsdiff.format.DiffOutput;

/** provides couple helpers to make html writing easier */
public class HtmlContentOutput implements DiffOutput {

    private final ContentHandler consumer;
    private final TagSaxDiffOutput diffOutput;

    public HtmlContentOutput( final ContentHandler content ) {
        this.consumer = content;
        this.diffOutput = new TagSaxDiffOutput( content );
    }


    public void write( final String str ) {
        try {
            consumer.startElement( "", "p", "p", noattrs() );

            writeRaw( str );

            consumer.endElement( "", "p", "p" );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write paragraph: " + str + ", exception occurred: " + e );
            e.printStackTrace();
        }
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

    /** transforms line ends to {@code <br>} breaks */
    public void writeLong(final String text) {
        try {
            final char[] c = text.toCharArray();

            for (int i = 0; i < c.length; i++) {
                switch (c[i]) {
                case '\n':
                    newline();
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

    @Override
    public void newline() {
        try {
            consumer.startElement("", "br", "br", noattrs() );
            consumer.endElement("", "br", "br");
            consumer.characters("\n".toCharArray(), 0, "\n".length());
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write new line break, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public static Attributes noattrs() {
        return new AttributesImpl();
    }


    /** for straight-through write */
    public ContentHandler getHandler() {
        return consumer;
    }

    /** invoke lambda function on self: contentOutput.handler( content -> content.doSomething() ); */
    public void handler( final Consumer<ContentHandler> block ) {
        block.accept( consumer );
    }


    @Override
    public void clearPart( final String text ) {
        try {
            diffOutput.addClearPart( text );
        }
        catch( final Exception e ) {
            System.err.println( "Failed to write added text: [" + text + "], exception occurred: " + e );
            e.printStackTrace();
        }
    }


    @Override
    public void removedPart( final String text ) {
        try {
            diffOutput.addRemovedPart( text );
        }
        catch( final Exception e ) {
            System.err.println( "Failed to write added text: [" + text + "], exception occurred: " + e );
            e.printStackTrace();
        }
    }


    @Override
    public void addedPart( final String text ) {
        try {
            diffOutput.addAddedPart( text );
        }
        catch( final Exception e ) {
            System.err.println( "Failed to write added text: [" + text + "], exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public void startFileHeader() {
        startDiv( "diff-topbar" );
    }

    public void endFileHeader() {
        endDiv();
    }

    private void startDiv( final String cssClass ) {
        try {
            consumer.startElement( "", "div", "div", classAttr(cssClass) );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write div element, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public void endDiv() {
        try {
            consumer.endElement( "", "div", "div" );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write div element, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public void startSpan( final String title ) {
        try {
            consumer.startElement( "", "span", "span", titleAttr( title ) );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write div element, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public void endSpan() {
        try {
            consumer.endElement( "", "span", "span" );
        }
        catch( final SAXException e ) {
            System.err.println( "Failed to write div element, exception occurred: " + e );
            e.printStackTrace();
        }
    }

    private Attributes classAttr( final String cssClass ) {
        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute( "", "class", "class", "CDATA", cssClass );
        return attrs;
    }

    private Attributes titleAttr( final String title ) {
        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute( "", "title", "title", "CDATA", title );
        return attrs;
    }

}
