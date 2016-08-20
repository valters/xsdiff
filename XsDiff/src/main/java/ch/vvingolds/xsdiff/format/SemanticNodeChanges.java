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

package ch.vvingolds.xsdiff.format;

import java.util.List;
import java.util.Set;

/** accumulate changes done to xml node, then process them at same time */
public interface SemanticNodeChanges {

    String getControlParentNodeNext();

    String getTestParentNodeNext();

    List<String> getAddedNodes();
    Set<String> getNodesWithAddedAttributes();
    Set<String> getAddedAttributesForNode(String nodeText);

    List<String> getRemovedNodes();
    Set<String> getNodeWithRemovedAttributes();
    Set<String> getRemovedAttributesForNode(String nodeText);

    /** produce git-style diff */
    DiffOutputFormatter getHistogramDiff();

    /** produce word-style diff */
    ContentHandlerFormatter getDaisyDiff();

    /** produce wikipedia style diff */
    ContentHandlerFormatter getWikedDiff();

    boolean isSomethingAdded();

    boolean isSomethingRemoved();
}
