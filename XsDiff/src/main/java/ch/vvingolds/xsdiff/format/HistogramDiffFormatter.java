package ch.vvingolds.xsdiff.format;

import java.util.List;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

public class HistogramDiffFormatter implements DiffOutputFormatter {

    private final DiffAlgorithm diffAlgorithm = new HistogramDiff();
    private final RawTextComparator textComparator = RawTextComparator.WS_IGNORE_ALL;

    private DiffOutput output;
    private EditList editList;

    private RawText a;
    private RawText b;

    private String newText;

    public void createDiff( final String oldText, final String newText  ) {
        a = new RawText( oldText.getBytes() );
        b = new RawText( newText.getBytes() );

        editList = diffAlgorithm.diff(textComparator, a, b );
    }

    @Override
    public void printDiff( final DiffOutput output ) {
        this.output = output;

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
