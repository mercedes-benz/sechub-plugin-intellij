// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.*;
import java.util.List;

/**
 * Because its very inconvenient and slow to test and develop the toolwindow
 * this support class was established. There exists a simple
 * SecHubToolWindowUISupportTestMain application class which can be called to
 * tweak ui without need to start intellj all time
 */
public class SecHubToolWindowUISupport {

    private final JTable reportTable;
    private final JTree callHierarchyTree;
    private final JTable callStepDetailTable;
    private final ErrorLog errorLog;
    private final JLabel cweIdLabel;
    private final SecHubToolWindowUIContext context;

    private FindingModel findingModel;
    private Set<CallStepChangeListener> callStepChangeListeners;
    private Set<ReportFindingSelectionChangeListener> reportFindingSelectionChangeListeners;

    public SecHubToolWindowUISupport(SecHubToolWindowUIContext context) {
        this.context = context;
        this.reportTable = context.findingTable;
        this.callHierarchyTree = context.callHierarchyTree;
        this.callStepDetailTable = context.callHierarchyDetailTable;
        this.errorLog = context.errorLog;
        this.cweIdLabel = context.cweIdLabel;
        this.callStepChangeListeners = new LinkedHashSet<>();
        this.reportFindingSelectionChangeListeners = new LinkedHashSet<>();
    }

    public interface CallStepChangeListener {
        public void callStepChanged(FindingNode callStep, boolean doubleClick);
    }

    public interface ReportFindingSelectionChangeListener {
        public void reportFindingSelectionChanged(FindingNode callStep);
    }

    public void addCallStepChangeListener(CallStepChangeListener listener) {
        this.callStepChangeListeners.add(listener);
    }

    public void removeCallStepChangeListener(CallStepChangeListener listener) {
        this.callStepChangeListeners.remove(listener);
    }

    public void addReportFindingSelectionChangeListener(ReportFindingSelectionChangeListener listener) {
        this.reportFindingSelectionChangeListeners.add(listener);
    }

    public void removeReportFindingSelectionChangeListener(ReportFindingSelectionChangeListener listener) {
        this.reportFindingSelectionChangeListeners.remove(listener);
    }

    public void initialize() {
        initCweIdLink();
        initReportTable();
        initCallHierarchyTree();
        initCallStepDetailTable();
    }

    private void initCweIdLink() {
        setCweId(null);
        cweIdLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cweIdLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Integer cweId = context.currentSelectedCweId;
                if (cweId == null) {
                    return;
                }
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop == null) {
                    return;
                }
                String uriAsText = createMitreCweDescriptionLink(cweId);

                try {
                    URI uri = new URI(uriAsText);
                    desktop.browse(uri);
                } catch (Exception exception) {
                    context.errorLog.error("Was not able to open URI:" + uriAsText, exception);
                }
            }
        });
    }

    @NotNull
    private String createMitreCweDescriptionLink(Integer cweId) {
        return "https://cwe.mitre.org/data/definitions/" + cweId + ".html";
    }

    private void initCallStepDetailTable() {
        callStepDetailTable.setModel(new SecHubTableModel("Step", "Line", "Column", "Location"));
        resetDetailsTablePresentation();
    }

    public void resetDetailsTablePresentation() {
        /* resize headers */
        TableColumnModel columnModel = callStepDetailTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(400);
    }

    private void initCallHierarchyTree() {
        callHierarchyTree.setRootVisible(false);
        callHierarchyTree.setModel(new SechubTreeModel());
        callHierarchyTree.addTreeSelectionListener((event) -> {

            SecHubTreeNode selected = (SecHubTreeNode) callHierarchyTree.getLastSelectedPathComponent();
            if (selected == null) {
                return;
            }
            FindingNode callStep = selected.getFindingNode();

            showCallStep(callStep, false);
        });

        callHierarchyTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SecHubTreeNode selected = (SecHubTreeNode) callHierarchyTree.getLastSelectedPathComponent();
                if (selected == null) {
                    return;
                }
                FindingNode callStep = selected.getFindingNode();
                if (e.getClickCount() > 1) {
                    showCallStep(callStep, true);
                }
            }
        });

        callHierarchyTree.setMinimumSize(new Dimension(300, 200));

    }

    private void initReportTable() {
        findingModel = new FindingModel();
        initTableWithModel(List.of());

        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                    handleReportTableSelection(true);
                }
            }

        });
        reportTable.getSelectionModel().addListSelectionListener((event) -> {
            handleReportTableSelection(false);
        });
        reportTable.setMinimumSize(new Dimension(600, 300));
    }

    private void initTableWithModel(List<Object[]> elements) {
        SecHubTableModel tableModel = new SecHubTableModel("Id", "Severity", "Type", "Name", "Location");
        for (Object[] element : elements) {
            tableModel.addRow(element);
        }
        TableRowSorter<SecHubTableModel> rowSorter = new TableRowSorter<>(tableModel);
        rowSorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        reportTable.setModel(tableModel);
        reportTable.setRowSorter(rowSorter);
        rowSorter.toggleSortOrder(0); // initial sort on first column

        /* resize headers */
        resetTablePresentation();
    }

    public void resetTablePresentation() {
        TableColumnModel columnModel = reportTable.getColumnModel();
        int index = 0;
        columnModel.getColumn(index++).setPreferredWidth(50);
        columnModel.getColumn(index++).setPreferredWidth(80);
        columnModel.getColumn(index++).setPreferredWidth(80);
        columnModel.getColumn(index++).setPreferredWidth(100);
        columnModel.getColumn(index++).setPreferredWidth(400);

        reportTable.doLayout();
    }

    private void handleReportTableSelection(boolean doubleClick) {
        if (this.findingModel == null) {
            errorLog.error("No model available");
            return;
        }
        int rowViewIndex = reportTable.getSelectedRow();
        int row = reportTable.convertRowIndexToModel(rowViewIndex);
        Object obj = reportTable.getModel().getValueAt(row, 0);
        if (obj == null) {
            errorLog.error("entry row in table is null!");
            return;
        }
        Integer integer = Integer.valueOf(obj.toString());
        int id = integer.intValue();

        for (FindingNode finding : findingModel.getFindings()) {
            if (finding.getId() == id) {
                showFindingNode(finding, doubleClick);
                break;
            }
        }
    }

    private void setCweId(Integer cweId) {
        context.currentSelectedCweId = cweId;
        if (cweId == null) {
            cweIdLabel.setVisible(false);
            return;
        }
        cweIdLabel.setText("<html><a href=\"" + createMitreCweDescriptionLink(cweId) + "\">CWE-ID " + cweId + "</a></html>");
        cweIdLabel.setToolTipText("Open " + createMitreCweDescriptionLink(cweId) + " in external browser");
        cweIdLabel.setVisible(true);
    }

    public void showFindingNode(FindingNode findingNode, boolean showEditor) {
        SechubTreeModel hierarchyTreeModel = (SechubTreeModel) callHierarchyTree.getModel();
        SecHubRootTeeNode newRootNode = new SecHubRootTeeNode();

        if (findingNode == null) {
            hierarchyTreeModel.setRoot(newRootNode);
            setCweId(null);
            showCallStep(null, false);
            return;
        }
        setCweId(findingNode.getCweId());

        buildCallhierarchyTreeNodes(newRootNode, findingNode);
        hierarchyTreeModel.setRoot(newRootNode);

        /* inform listeners */
        for (ReportFindingSelectionChangeListener listener : reportFindingSelectionChangeListeners) {
            listener.reportFindingSelectionChanged(findingNode);
        }

        showCallStep(findingNode, showEditor);
        callHierarchyTree.addSelectionInterval(0, 0);
    }

    private void buildCallhierarchyTreeNodes(SecHubTreeNode parent, FindingNode findingNode) {
        SecHubTreeNode treeNode = new SecHubTreeNode(findingNode);
        parent.add(treeNode);

        for (FindingNode child : findingNode.getChildren()) {
            buildCallhierarchyTreeNodes(parent, child);
        }
    }

    private void showCallStep(FindingNode callStep, boolean showEditor) {
        /* show in detail table */
        SecHubTableModel callStepTableModel = (SecHubTableModel) callStepDetailTable.getModel();
        callStepTableModel.removeAllRows();

        if (callStep != null) {
            Object[] rowData = new Object[]{callStep.getCallStackStep(), callStep.getLine(), callStep.getColumn(), callStep.getLocation()};
            callStepTableModel.addRow(rowData);
        }
        callStepTableModel.fireTableDataChanged();

        /* inform listeners */
        for (CallStepChangeListener listener : callStepChangeListeners) {
            listener.callStepChanged(callStep, showEditor);
        }
    }


    public void setFindingModel(FindingModel findingModel) {
        List<Object[]> elements = new ArrayList<>();
        /* fill with new rows */
        List<FindingNode> findings = findingModel.getFindings();
        for (FindingNode finding : findings) {
            if (finding == null) {
                continue;
            }
            Object[] rowData = new Object[]{finding.getId(), finding.getSeverity(), finding.getScanType(), finding.getName(),
                    finding.getFileName()};
            elements.add(rowData);
        }

        this.findingModel = findingModel;
        initTableWithModel(elements);
    }

}
