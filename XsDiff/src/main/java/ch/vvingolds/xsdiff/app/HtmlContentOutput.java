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

import com.google.common.base.Preconditions;

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
        return attrs( "class", cssClass );
    }

    private Attributes titleAttr( final String title ) {
        return attrs( "title", title );
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

    /**
            <div class="tabs" data-toggle="tabslet" data-animation="false">
              <ul class="horizontal">
                <li><a href="#tab-1">information</a></li>
                <li><a href="#tab-2">tab</a></li>
                <li><a href="#tab-3">tab</a></li>
              </ul>
              <div id="tab-1"><span>Just include the plugin and add the data attribute to your html tag!</span></div>
              <div id="tab-2"><span>TAB 2</span></div>
              <div id="tab-3"><span>TAB 3</span></div>
            </div>
     */

    public void writeTab( final Consumer<DiffOutput> semanticOutput,
            final Consumer<DiffOutput> histogramOutput,
            final Consumer<ContentHandler> daisyOutput,
            final Consumer<ContentHandler> wikedOutput ) {

        el("div", "class", "tabs", "data-toggle", "tabslet", "data-animation", "true" );
          el( "ul", "class", "horizontal" );
            el( "li" );
              el( "a", "href", "#tab-1" ); writeRaw("semantic"); _el("a");
            _el("li");
            el( "li" );
              el( "a", "href", "#tab-2" ); writeRaw("histogram"); _el("a");
            _el("li");
            el( "li" );
              el( "a", "href", "#tab-3" ); writeRaw("daisy"); _el("a");
            _el( "li" );
            el( "li" );
              el( "a", "href", "#tab-4" ); writeRaw("wikEd"); _el("a");
            _el( "li" );
          _el( "ul" );

          el("div", "id", "tab-1" );
            el("span"); semanticOutput.accept( this ); _el("span");
          _el("div");

          el("div", "id", "tab-2" );
            histogramOutput.accept( this );
          _el("div");

          el("div", "id", "tab-3" );
            daisyOutput.accept( consumer );
          _el("div");

          el("div", "id", "tab-4" );
            wikedOutput.accept( consumer );
          _el("div");

          _el("div");

    }
}
