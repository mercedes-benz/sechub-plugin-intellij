// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.ui;

import com.mercedesbenz.sechub.model.FindingNode;

import javax.swing.tree.DefaultMutableTreeNode;

public class SecHubTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	public SecHubTreeNode(FindingNode findingNode){
        super(findingNode);
    }

    public FindingNode getFindingNode(){
        return (FindingNode) getUserObject();
    }
}
