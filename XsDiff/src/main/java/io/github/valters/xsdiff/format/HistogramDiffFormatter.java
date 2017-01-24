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

import java.util.List;

import org.outerj.eclipse.jgit.diff.Edit;
import org.outerj.eclipse.jgit.diff.EditList;
import org.outerj.eclipse.jgit.diff.HistogramDiff;
import org.outerj.eclipse.jgit.diff.RawText;
import org.outerj.eclipse.jgit.diff.RawTextComparator;
import org.outerj.eclipse.jgit.util.RawParseUtils;


public class HistogramDiffFormatter implements DiffOutputFormatter {

    private final String oldText;
    private final String newText;

    public HistogramDiffFormatter( final String oldText, final String newText ) {
        super();
        this.oldText = oldText;
        this.newText = newText;
    }

    private final HistogramDiff diffAlgorithm = new HistogramDiff();
    private final RawTextComparator textComparator = RawTextComparator.WS_IGNORE_ALL;

    private DiffOutput output;
    private EditList editList;

    private RawText a;
    private RawText b;

    @Override
    public void printDiff( final DiffOutput output ) {
        this.output = output;

        a = new RawText( oldText.getBytes(), RawParseUtils.MAP_LINES );
        b = new RawText( newText.getBytes(), RawParseUtils.MAP_LINES );

        editList = diffAlgorithm.diff(textComparator, a, b );

        if (editList.isEmpty()) {
            output.clearPart( newText );
        }
        else {
            format( editList, a, b );
        }
    }

    /** jgit diff output */
    public void format(final EditList edits, final RawText a, final RawText b) {

        for (int curIdx = 0; curIdx < edits.size();) {
            Edit curEdit = edits.get(curIdx);
            final int endIdx = findCombinedEnd(edits, curIdx);
            final Edit endEdit = edits.get(endIdx);

            int aCur = (int) Math.max(0, (long) curEdit.getBeginA() - context);
            int bCur = (int) Math.max(0, (long) curEdit.getBeginB() - context);
            final int aEnd = (int) Math.min(a.size(), (long) endEdit.getEndA() + context);
            final int bEnd = (int) Math.min(b.size(), (long) endEdit.getEndB() + context);

            while (aCur < aEnd || bCur < bEnd) {
                if (aCur < curEdit.getBeginA() || endIdx + 1 < curIdx) {
                    output.clearPart( a.getString( aCur ) );
                    output.newline();
                    aCur++;
                    bCur++;
                } else if (aCur < curEdit.getEndA()) {
                    output.removedPart( a.getString( aCur ) );
                    output.newline();
                    aCur++;
                } else if (bCur < curEdit.getEndB()) {
                    output.addedPart( b.getString( bCur ) );
                    output.newline();
                    bCur++;
                }

                if (end(curEdit, aCur, bCur) && ++curIdx < edits.size()) {
                    curEdit = edits.get(curIdx);
                }
            }
        }
    }

    private int findCombinedEnd(final List<Edit> edits, final int i) {
        int end = i + 1;
        while (end < edits.size()
                && (combineA(edits, end) || combineB(edits, end))) {
            end++;
        }
        return end - 1;
    }

    private final int context = 300;

    private boolean combineA(final List<Edit> e, final int i) {
        return e.get(i).getBeginA() - e.get(i - 1).getEndA() <= 2 * context;
    }

    private boolean combineB(final List<Edit> e, final int i) {
        return e.get(i).getBeginB() - e.get(i - 1).getEndB() <= 2 * context;
    }

    private static boolean end(final Edit edit, final int a, final int b) {
        return edit.getEndA() <= a && edit.getEndB() <= b;
    }

}
