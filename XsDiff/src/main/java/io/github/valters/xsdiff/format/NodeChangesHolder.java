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
import java.util.Set;

import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class NodeChangesHolder implements SemanticNodeChanges {

    /** what kind of text operation happened */
    public static enum OpType { ADDED, REMOVED };

    protected final String nodeXpath;

    private String controlParentNodeNext;

    private String testParentNodeNext;

    private final List<String> addedNodeText = Lists.newArrayList();

    private final List<String> removedNodeText = Lists.newArrayList();

    private final SetValuedMap<String, String> addedAttrText = new HashSetValuedHashMap<>();

    private final SetValuedMap<String, String> removedAttrText = new HashSetValuedHashMap<>();

    private DaisyDiffFormatter daisyDiff;

    private HistogramDiffFormatter histogramDiff;

    private WikedDiffFormatter wikedDiff;

    public NodeChangesHolder( final String nodeXpath ) {
        super();
        this.nodeXpath = nodeXpath;
    }

    public void addedNode( final String nodeText ) {
        addedNodeText.add( nodeText );
    }

    public void removedNode( final String nodeText ) {
        removedNodeText.add( nodeText );
    }


    @Override
    public List<String> getAddedNodes() {
        return addedNodeText;
    }

    @Override
    public List<String> getRemovedNodes() {
        return removedNodeText;
    }

    @Override
    public ContentHandlerFormatter getDaisyDiff() {
        if( daisyDiff == null ) {
            return EmptyDiff.INSTANCE;
        }
        return daisyDiff;
    }

    public void setDaisyDiff( final DaisyDiffFormatter daisyDiff ) {
        this.daisyDiff = daisyDiff;
    }

    @Override
    public DiffOutputFormatter getHistogramDiff() {
        if( histogramDiff == null ) {
            return EmptyDiff.INSTANCE;
        }
        return histogramDiff;
    }

    public void setHistogramDiff( final HistogramDiffFormatter histogramDiff ) {
        this.histogramDiff = histogramDiff;
    }

    @Override
    public String getControlParentNodeNext() {
        return controlParentNodeNext;
    }

    @Override
    public String getTestParentNodeNext() {
        return testParentNodeNext;
    }

    public void addParentNodeText( final OpType op, final String nodeText ) {
        if( op == OpType.ADDED ) {
            if( Strings.isNullOrEmpty( this.testParentNodeNext ) ) {
                this.testParentNodeNext = nodeText;
            }
        } else if( op == OpType.REMOVED ) {
            if( Strings.isNullOrEmpty( this.controlParentNodeNext ) ) {
                this.controlParentNodeNext = nodeText;
            }
        }
    }

    @Override
    public ContentHandlerFormatter getWikedDiff() {
        if( wikedDiff == null ) {
            return EmptyDiff.INSTANCE;
        }
        return wikedDiff;
    }

    public void setWikedDiff( final WikedDiffFormatter wikedDiff ) {
        this.wikedDiff = wikedDiff;
    }

    public void addedAttribute( final String nodeText, final String attributeText ) {
        addedAttrText.put( nodeText, attributeText );
    }

    public void removedAttribute( final String nodeText, final String attributeText ) {
        removedAttrText.put( nodeText, attributeText );
    }

    @Override
    public Set<String> getNodesWithAddedAttributes() {
        return addedAttrText.keySet();
    }

    @Override
    public Set<String> getAddedAttributesForNode( final String nodeText ) {
        return addedAttrText.get( nodeText );
    }

    @Override
    public Set<String> getNodeWithRemovedAttributes() {
        return removedAttrText.keySet();
    }

    @Override
    public Set<String> getRemovedAttributesForNode( final String nodeText ) {
        return removedAttrText.get( nodeText );
    }

    @Override
    public boolean isSomethingAdded() {
        return !addedNodeText.isEmpty() || !addedAttrText.isEmpty();
    }

    @Override
    public boolean isSomethingRemoved() {
        return !removedNodeText.isEmpty() || !removedAttrText.isEmpty();
    }

    public void attachAutoDiffs() {
        this.wikedDiff = new WikedDiffFormatter( controlParentNodeNext, testParentNodeNext );

        this.daisyDiff = new DaisyDiffFormatter( controlParentNodeNext, testParentNodeNext );

        this.histogramDiff = new HistogramDiffFormatter( controlParentNodeNext, testParentNodeNext );
    }
}
