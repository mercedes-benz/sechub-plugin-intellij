// SPDX-License-Identifier: MIT
package com.daimler.sechub.util;

import javax.swing.JTree;

public class JTreeUtil {
	
	public static void expandAllNodes(JTree tree) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

}
