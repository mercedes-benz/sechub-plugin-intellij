// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import com.mercedesbenz.sechub.plugin.util.ErrorLog;

import javax.swing.*;

public class SecHubToolWindowUIContext {
    public JTable findingTable;
    public JTree callHierarchyTree;
    public JTable callHierarchyDetailTable;
    public ErrorLog errorLog;
    public JLabel cweIdLabel;
    public Integer currentSelectedCweId;
}
