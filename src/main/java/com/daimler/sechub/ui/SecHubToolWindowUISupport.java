// SPDX-License-Identifier: MIT
package com.daimler.sechub.ui;

import com.daimler.sechub.model.FindingModel;
import com.daimler.sechub.model.FindingNode;
import com.daimler.sechub.util.ErrorLog;
import com.daimler.sechub.util.JTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

	private FindingModel model;
	private Set<CallStepChangeListener> callStepChangeListeners;
	private Set<ReportFindingSelectionChangeListener> reportFindingSelectionChangeListeners;

	public SecHubToolWindowUISupport(SecHubToolWindowUIContext context) {
		this.context=context;
		this.reportTable = context.findingTable;
		this.callHierarchyTree = context.callHierarchyTree;
		this.callStepDetailTable = context.callHierarchyDetailTable;
		this.errorLog = context.errorLog;
		this.cweIdLabel=context.cweIdLabel;
		this.callStepChangeListeners = new LinkedHashSet<>();
		this.reportFindingSelectionChangeListeners = new LinkedHashSet<>();
	}

	public interface CallStepChangeListener {
		public void callStepChanged(FindingNode callStep);
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
				if (cweId ==null){
					return;
				}
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (desktop==null){
					return;
				}
				String uriAsText = createMitreCweDescriptionLink(cweId);

				try{
					URI uri = new URI(uriAsText);
					desktop.browse(uri);
				} catch (Exception exception) {
					context.errorLog.error("Was not able to open URI:"+ uriAsText,exception);
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

			showCallStep(callStep);
		});
		callHierarchyTree.setMinimumSize(new Dimension(300, 200));

	}

	private void initReportTable() {
		model = new FindingModel();
		SecHubTableModel model = new SecHubTableModel("Id", "Severity", "Name", "Location");
		reportTable.setModel(model);

		reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reportTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
					handleDoubleClickOnFinding();
				}
			}

		});
		reportTable.setMinimumSize(new Dimension(400, 400));
		/* resize headers */
		TableColumnModel columnModel = reportTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(50);
		columnModel.getColumn(1).setPreferredWidth(80);
		columnModel.getColumn(2).setPreferredWidth(100);
		columnModel.getColumn(3).setPreferredWidth(400);
	}

	private void handleDoubleClickOnFinding() {
		if (this.model == null) {
			errorLog.error("No model available");
			return;
		}
		int row = reportTable.getSelectedRow();
		Object obj = reportTable.getModel().getValueAt(row, 0);
		if (obj == null) {
			errorLog.error("entry row in table is null!");
			return;
		}
		Integer integer = Integer.valueOf(obj.toString());
		int id = integer.intValue();

		for (FindingNode finding : model.getFindings()) {
			if (finding.getId() == id) {
				showFinding(finding);
				break;
			}
		}
	}

	private void setCweId(Integer cweId){
		context.currentSelectedCweId =cweId;
		if (cweId==null){
			cweIdLabel.setVisible(false);
			return;
		}
		cweIdLabel.setText("<html><a href=\""+ createMitreCweDescriptionLink(cweId)+"\">CWE-ID "+cweId+"</a></html>");
		cweIdLabel.setToolTipText("Open "+ createMitreCweDescriptionLink(cweId)+ " in external browser");
		cweIdLabel.setVisible(true);
	}

	private void showFinding(FindingNode finding) {
		SechubTreeModel treeModel = (SechubTreeModel) callHierarchyTree.getModel();
		SecHubRootTeeNode newRootNode = new SecHubRootTeeNode();

		if (finding == null) {
			treeModel.setRoot(newRootNode);
			setCweId(null);
			showCallStep(null);
			return;
		}
		setCweId(finding.getCweId());

		buildTreeNodes(newRootNode, finding);
		treeModel.setRoot(newRootNode);
		
		/* inform listeners */
		for (ReportFindingSelectionChangeListener listener : reportFindingSelectionChangeListeners) {
			listener.reportFindingSelectionChanged(finding);
		}

		showCallStep(finding);

		JTreeUtil.expandAllNodes(callHierarchyTree);
	}

	private void buildTreeNodes(SecHubTreeNode parent, FindingNode findingNode) {
		SecHubTreeNode treeNode = new SecHubTreeNode(findingNode);
		parent.add(treeNode);

		for (FindingNode child : findingNode.getChildren()) {
			buildTreeNodes(treeNode, child);
		}
	}

	private void showCallStep(FindingNode callStep) {
		/* show in detail table */
		SecHubTableModel callStepTableModel = (SecHubTableModel) callStepDetailTable.getModel();
		callStepTableModel.removeAllRows();
		
		Object[] rowData = new Object[] { callStep.getCallStackStep(), callStep.getLine(),callStep.getColumn(),callStep.getLocation()};
		callStepTableModel.addRow(rowData);
		callStepTableModel.fireTableDataChanged();

		/* inform listeners */
		for (CallStepChangeListener listener : callStepChangeListeners) {
			listener.callStepChanged(callStep);
		}
	}
	
	

	public void setModel(FindingModel model) {
		this.model = model;

		SecHubTableModel reportTableModel = (SecHubTableModel) reportTable.getModel();
		reportTableModel.removeAllRows();
		
		/* fill with new rows */
		List<FindingNode> findings = model.getFindings();
		for (FindingNode finding : findings) {
			if (finding == null) {
				continue;
			}
			Object[] rowData = new Object[] { finding.getId(), finding.getSeverity(), finding.getDescription(),
					finding.getFileName() };
			reportTableModel.addRow(rowData);
		}

	}

}
