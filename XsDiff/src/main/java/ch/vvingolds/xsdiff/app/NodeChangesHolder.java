package ch.vvingolds.xsdiff.app;

import java.util.List;

import com.google.common.collect.Lists;

public class NodeChangesHolder implements NodeChanges {
    private final String parentNodeNext;

    private final List<String> addedNodeText = Lists.newArrayList();

    private final List<String> removedNodeText = Lists.newArrayList();

    public NodeChangesHolder( final String parentNodeNext ) {
        super();
        this.parentNodeNext = parentNodeNext;
    }

    public void addedNode( final String nodeText ) {
        addedNodeText.add( nodeText );
    }

    public void removedNode( final String nodeText ) {
        removedNodeText.add( nodeText );
    }

    @Override
    public String getParentNodeNext() {
        return parentNodeNext;
    }

    @Override
    public List<String> getAddedNodes() {
        return addedNodeText;
    }

    @Override
    public List<String> getRemovedNodes() {
        return removedNodeText;
    }
}
