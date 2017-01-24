/*
 * Copyright 2009 Guy Van den Broeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.valters.xsdiff.report;

import java.util.ArrayList;
import java.util.List;

import org.outerj.daisy.diff.output.TextDiffOutput;

import io.github.valters.xsdiff.format.DiffOutput;

/**
 * Minimal test case for diffs mode.
 *
 * @author kapelonk
 * @author vvingolds
 *
 */
public class DiffTestFixture {

    public static DummyOutput output() {
        return new DummyOutput();
    }

    /** Type of changes as produced by the diff process */
    public enum OperationType {
        NO_CHANGE, ADD_TEXT, REMOVE_TEXT
    }

    /**
     * Simple operation for test cases only.
     *
     * @author kapelonk
     *
     */
    protected static class TextOperation {
        @Override
        public String toString() {
            return "[" + type + ": '" + getText() + "']";
        }

        private String text = null;

        private OperationType type = null;

        /** consolidates with following operations */
        private StringBuilder buffer;

        /**
         * @param text the text to set
         */
        public void setText( final String text ) {
            this.text = text;
        }

        /**
         * @param type the type to set
         */
        public void setType( final OperationType type ) {
            this.type = type;
        }

        /**
         * @return the text
         */
        public String getText() {
            if( text == null ) {
                if( buffer != null ) {
                    text = buffer.toString();
                    buffer = null;
                }
            }
            return text;
        }

        /**
         * @return the type
         */
        public OperationType getType() {
            return type;
        }

        public void consolidate( final String text ) {
            if( buffer == null ) {
                buffer = new StringBuilder( this.text );
                this.text = null;
            }

            buffer.append( text );
        }

    }

    /**
     * Dummy output that holds all results in a linear list.
     *
     * @author kapelonk
     *
     */
    public static class DummyOutput implements TextDiffOutput, DiffOutput {
        /** A list of text operations produced by the diff process */
        private final List<TextOperation> results = new ArrayList<>();

        private TextOperation currOperation = new TextOperation();

        /**
         * Retuns a list of basic operations.
         * @return the results
         */
        public List<TextOperation> getResults() {
            return results;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addAddedPart( final String text ) throws Exception {
            currOperation = new TextOperation();
            currOperation.setText( text );
            currOperation.setType( OperationType.ADD_TEXT );
            results.add( currOperation );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addClearPart( final String text ) throws Exception {
            currOperation = new TextOperation();
            currOperation.setText( text );
            currOperation.setType( OperationType.NO_CHANGE );
            results.add( currOperation );

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addRemovedPart( final String text ) throws Exception {
            currOperation = new TextOperation();
            currOperation.setText( text );
            currOperation.setType( OperationType.REMOVE_TEXT );
            results.add( currOperation );

        }

        @Override
        public void newline() {
            currOperation.consolidate( "\n" );
        }

        @Override
        public void clearPart( final String text ) {
            try {
                addClearPart( text );
            }
            catch( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        @Override
        public void removedPart( final String text ) {
            try {
                addRemovedPart( text );
            }
            catch( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        @Override
        public void addedPart( final String text ) {
            try {
                addAddedPart( text );
            }
            catch( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

    }

}
