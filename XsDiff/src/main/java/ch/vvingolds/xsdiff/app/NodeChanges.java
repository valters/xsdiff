package ch.vvingolds.xsdiff.app;

import java.util.List;

/** accumulate changes done to xml node, then process them at same time */
interface NodeChanges {

    String getParentNodeNext();

    List<String> getAddedNodes();

    List<String> getRemovedNodes();
}
