package ch.vvingolds.xsdiff.format;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.w3c.dom.Document;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import ch.vvingolds.xsdiff.app.HtmlContentOutput;
import ch.vvingolds.xsdiff.app.NodeToString;
import ch.vvingolds.xsdiff.app.XmlDomUtils;

public class SemanticDiffFormatter {

    private final XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private final NodeToString printNode = new NodeToString();

    private final Map<String, NodeChangesHolder> nodeChanges = Maps.newLinkedHashMap();

    private HtmlContentOutput output;

    public void printDiff( final HtmlContentOutput output ) {
        this.output = output;
        printAllChanges();
    }

    private void printAllChanges() {
        for( final Map.Entry<String, NodeChangesHolder> entry : nodeChanges.entrySet() ) {
            printChanges( entry.getKey(), entry.getValue() );
        }
    }

    /** produce actual output */
    public void printChanges( final String xpath, final SemanticNodeChanges changes ) {

        output.writeTab( semanticOutput -> printDiff( xpath, changes, semanticOutput ),
            histogramOutput -> changes.getHistogramDiff().printDiff( histogramOutput ),
            daisyOutput -> changes.getDaisyDiff().printDiff( daisyOutput ),
            wikedOutput -> changes.getWikedDiff().printDiff( wikedOutput )
        );

      }

    public void printPartRemoved( final String text, final List<String> removedParts, final DiffOutput output ) {

        final TrieBuilder trie = Trie.builder().removeOverlaps();
        for( final String part : removedParts ) {
            trie.addKeyword(part);
        }
        final Collection<Emit> emits = trie.build().parseText( text );

        int prevFragment = 0;
        for( final Emit emit : emits ) {
            final String clearPartBefore = text.substring( prevFragment, emit.getStart() );
            output.clearPart( clearPartBefore );
            output.removedPart( emit.getKeyword() );

            prevFragment = emit.getEnd()+1;
        }

        final String clearPartAfter = text.substring( prevFragment, text.length() );
        output.clearPart( clearPartAfter );
    }

    public void printPartAdded( final String text, final List<String> addedParts, final DiffOutput output ) {
        final TrieBuilder trie = Trie.builder().removeOverlaps();
        for( final String part : addedParts ) {
            trie.addKeyword(part);
        }
        final Collection<Emit> emits = trie.build().parseText( text );

        int prevFragment = 0;
        for( final Emit emit : emits ) {
            final String clearPartBefore = text.substring( prevFragment, emit.getStart() );
            output.clearPart( clearPartBefore );
            output.addedPart( emit.getKeyword() );

            prevFragment = emit.getEnd()+1;
        }

        final String clearPartAfter = text.substring( prevFragment, text.length() );
        output.clearPart( clearPartAfter );
    }

    private void printDiff( final String xpath, final SemanticNodeChanges changes, final DiffOutput output ) {
        if( ! changes.getAddedNodes().isEmpty() ) {
            output.newline();
            output.newline();
            output.addedPart( "all adds for node (" + xpath + ")");
            output.newline();
            output.newline();
            final String nodeText = changes.getTestParentNodeNext();
            if( Strings.isNullOrEmpty( nodeText ) ) {
                output.clearPart( "NULL TEXT" );
            } else {
                printPartAdded( nodeText, changes.getAddedNodes(), output );
            }
        }
        if( ! changes.getRemovedNodes().isEmpty() ) {
            output.newline();
            output.newline();
            output.removedPart( "all removes from node (" + xpath + ")");
            output.newline();
            output.newline();
            final String nodeText = changes.getControlParentNodeNext();
            if( Strings.isNullOrEmpty( nodeText ) ) {
                output.clearPart( "NULL TEXT" );
            } else {
                printPartRemoved( nodeText, changes.getRemovedNodes(), output );
            }
        }
    }

    /** create holder on the fly for certain add/remove operations
     * @param opType make sure we can have a holder for each add/remove because parent text is different
     */
    private NodeChangesHolder getOrAddHolder( final String parentXpath, final Document parentDoc, final NodeChangesHolder.OpType opType ) {
        final NodeChangesHolder changeHolder = nodeChanges.get( parentXpath );
        if( changeHolder == null ) {
            return addHolder( parentXpath, parentDoc, opType );
        }

        updateHolder( changeHolder, opType, getNodeText( XmlDomUtils.wideContext( parentXpath ), parentDoc ) );
        return changeHolder;
    }

    /** check of change holder should be created, if one does not exist. verifies that the node is not located too shallow (i.e., we don't want to track stuff added under doc root) */
    public NodeChangesHolder addHolder( final String parentXpath, final Document parentDoc, final NodeChangesHolder.OpType opType ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( parentXpath );
        final boolean tooShallow = xpathDepth < 2;
        if( tooShallow ) {
            return null;
        }

        // should mark anyway
        return updateHolder( addChangeHolder( parentXpath ), opType, getNodeText( parentXpath, parentDoc ) );
    }

    private String getNodeText( final String parentXpath, final Document parentDoc ) {
        if( parentDoc == null ) {
            return null;
        }
        return printNode.nodeToString( xmlDomUtils.findNode( parentDoc, parentXpath ) );
    }

    /** @return false, if change could not be posted (parent holder did not exist). caller should print change explicitly. */
    public boolean markNodeRemoved( final String parentXpath, final String nodeText, final Document parentDoc ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, parentDoc, NodeChangesHolder.OpType.REMOVED );
        if( changeHolder == null ) {
            return false;
        }

        changeHolder.removedNode( nodeText );
        return true;
    }

    /** @return false, if change could not be posted (parent holder did not exist). caller should print change explicitly. */
    public boolean markNodeAdded( final String parentXpath, final String nodeText, final Document parentDoc ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, parentDoc, NodeChangesHolder.OpType.ADDED );
        if( changeHolder == null ) {
            return false;
        }

        changeHolder.addedNode( nodeText );
        return true;
    }

    public NodeChangesHolder addChangeHolder( final String key ) {
        NodeChangesHolder holder = nodeChanges.get( key );
        if( holder == null ) {
            holder = new NodeChangesHolder( key );
            nodeChanges.put( key, holder );
        }
        return holder;
    }

    public NodeChangesHolder updateHolder( final NodeChangesHolder holder, final NodeChangesHolder.OpType opType, final String nodeText ) {
        holder.addParentNodeText( opType, nodeText );
        return holder;
    }


    public void attachDaisyDiff( final String parentXpath, final DaisyDiffFormatter daisyDiff ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, null, null );
        if( changeHolder == null ) {
            return;
        }

        changeHolder.setDaisyDiff( daisyDiff );
    }

    public void attachHistogramDiff( final String parentXpath, final HistogramDiffFormatter histogramDiff ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, null, null );
        if( changeHolder == null ) {
            return;
        }

        changeHolder.setHistogramDiff( histogramDiff );
    }

    public void attachWikedDiff( final String parentXpath, final WikedDiffFormatter wikedDiff ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, null, null );
        if( changeHolder == null ) {
            return;
        }

        changeHolder.setWikedDiff( wikedDiff );
    }

}
