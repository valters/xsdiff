package ch.vvingolds.xsdiff.format;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.w3c.dom.Document;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import ch.vvingolds.xsdiff.app.NodeChanges;
import ch.vvingolds.xsdiff.app.NodeChangesHolder;
import ch.vvingolds.xsdiff.app.NodeToString;
import ch.vvingolds.xsdiff.app.XmlDomUtils;

public class SemanticDiffFormatter {

    private static final String PREFIX_OP_REMOVED = "removed";
    private static final String PREFIX_OP_ADDED = "added";

    private static final Joiner JOIN_KEY = Joiner.on( '-' );

    private final XmlDomUtils xmlDomUtils = new XmlDomUtils();
    private final NodeToString printNode = new NodeToString();

    private final Map<String, NodeChangesHolder> nodeChanges = Maps.newLinkedHashMap();

    private DiffOutput output;

    public void printDiff( final DiffOutput output ) {
        this.output = output;
        for( final Map.Entry<String, NodeChangesHolder> entry : nodeChanges.entrySet() ) {
            markChanges( entry.getKey(), entry.getValue() );
        }
    }

    public void markPartRemoved( final String text, final List<String> removedParts ) {

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

    public void markPartAdded( final String text, final List<String> addedParts ) {
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


    public void markChanges( final String xpath, final NodeChanges changes ) {

        if( ! changes.getAddedNodes().isEmpty() ) {
            output.newline();
            output.newline();
            output.addedPart( "all adds for node (" + xpath + ")");
            output.newline();
            output.newline();
            markPartAdded( changes.getParentNodeNext(), changes.getAddedNodes() );
        }
        if( ! changes.getRemovedNodes().isEmpty() ) {
            output.newline();
            output.newline();
            output.removedPart( "all removes from node (" + xpath + ")");
            output.newline();
            output.newline();
            markPartRemoved( changes.getParentNodeNext(), changes.getRemovedNodes() );
        }
    }

    /** create holder on the fly for certain add/remove operations
     * @param opType make sure we can have a holder for each add/remove because parent text is different
     */
    private NodeChangesHolder getOrAddHolder( final String parentXpath, final Document parentDoc, final String opType ) {
        final NodeChangesHolder changeHolder = nodeChanges.get( parentXpath );
        if( changeHolder != null ) {
            return changeHolder;
        }

        return addHolder( parentXpath, parentDoc, opType );
    }

    /** check of change holder should be created, if one does not exist. verifies that the node is not located too shallow (i.e., we don't want to track stuff added under doc root) */
    public NodeChangesHolder addHolder( final String parentXpath, final Document parentDoc, final String opType ) {
        final long xpathDepth = XmlDomUtils.xpathDepth( parentXpath );
        final boolean tooShallow = xpathDepth < 2;
        if( tooShallow ) {
            return null;
        }

        // should mark anyway
        return addChangeHolder( opType, parentXpath, printNode.nodeToString( xmlDomUtils.findNode( parentDoc, parentXpath ) ) );
    }

    /** @return false, if change could not be posted (parent holder did not exist). caller should print change explicitly. */
    public boolean markNodeRemoved( final String parentXpath, final String nodeText, final Document parentDoc ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, parentDoc, PREFIX_OP_REMOVED );
        if( changeHolder == null ) {
            return false;
        }

        changeHolder.removedNode( nodeText );
        return true;
    }

    /** @return false, if change could not be posted (parent holder did not exist). caller should print change explicitly. */
    public boolean markNodeAdded( final String parentXpath, final String nodeText, final Document parentDoc ) {
        final NodeChangesHolder changeHolder = getOrAddHolder( parentXpath, parentDoc, PREFIX_OP_ADDED );
        if( changeHolder == null ) {
            return false;
        }

        changeHolder.addedNode( nodeText );
        return true;
    }

    public NodeChangesHolder addChangeHolder( final String opType, final String xpathExpr, final String nodeText ) {
        final String key = makeKey( opType, xpathExpr );

        final NodeChangesHolder holder = nodeChanges.get( key );
        if( holder != null ) {
            return holder;
        }

        final NodeChangesHolder changesHolder = new NodeChangesHolder( nodeText );
        nodeChanges.put( key, changesHolder );
        return changesHolder;
    }

    private String makeKey( final String opType, final String xpathExpr ) {
        if( opType == null ) {
            return xpathExpr;
        }
        return JOIN_KEY.join( opType, xpathExpr );
    }


    public String opAdded() {
        return PREFIX_OP_ADDED;
    }

    public String opRemoved() {
        return PREFIX_OP_REMOVED;
    }

}
