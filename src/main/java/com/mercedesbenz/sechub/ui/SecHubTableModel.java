// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.ui;

import javax.swing.table.DefaultTableModel;

public class SecHubTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	private boolean globalCellEditable = false;

	public SecHubTableModel(String... columnNames) {
		super(columnNames, 0);
	}

	public boolean isCellEditable(int row, int column) {
		return globalCellEditable;
	}
	
	public void removeAllRows() {
		for (int i = 0; i < getRowCount(); i++) {
			removeRow(i);
		}
		dataVector.removeAllElements();
	}
}
