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

import org.outerj.daisy.diff.output.TextDiffOutput;
import org.outerj.daisy.diff.tag.TagSaxDiffOutput;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** provides couple helpers to make html writing easier */
public class HtmlContentOutput implements TextDiffOutput {

    private final ContentHandler consumer;
    private final TagSaxDiffOutput diffOutput;

    public HtmlContentOutput( final ContentHandler content ) {
        this.consumer = content;
        this.diffOutput = new TagSaxDiffOutput( content );
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
    public void addClearPart( final String text ) throws Exception {
        diffOutput.addClearPart( text );
    }


    @Override
    public void addRemovedPart( final String text ) throws Exception {
        diffOutput.addRemovedPart( text );
    }


    @Override
    public void addAddedPart( final String text ) throws Exception {
        diffOutput.addAddedPart( text );
    }

    public void markPartRemoved( final String text, final String removedPart ) {
        try {
            final int removeStarts = text.indexOf( removedPart );
            final String clearPartBefore = text.substring( 0, removeStarts );
            final String clearPartAfter = text.substring( removeStarts + removedPart.length(), text.length() );
            addClearPart( clearPartBefore );
            addRemovedPart( removedPart );
            addClearPart( clearPartAfter );
        }
        catch( final Exception e ) {
            System.err.println( "Failed to write removed paragraph: ["+removedPart+"] from [" + text + "], exception occurred: " + e );
            e.printStackTrace();
        }
    }

    public void markPartAdded( final String text, final String addedPart ) {
        try {
            final int removeStarts = text.indexOf( addedPart );
            final String clearPartBefore = text.substring( 0, removeStarts );
            final String clearPartAfter = text.substring( removeStarts + addedPart.length(), text.length() );
            addClearPart( clearPartBefore );
            addAddedPart( addedPart );
            addClearPart( clearPartAfter );
        }
        catch( final Exception e ) {
            System.err.println( "Failed to write added paragraph: ["+addedPart+"] from [" + text + "], exception occurred: " + e );
            e.printStackTrace();
        }
    }


}
