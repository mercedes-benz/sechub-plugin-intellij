// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;

/**
 * This is only a simple test application - so we do not need to start IntelliJ all time here and are able to test
 * UI interaction provided by SecHubToolWindowUISupport class
 */
public class SecHubToolWindowUISupportTestMain {

	public static void main(String[] args) {
		new InternalUITest().start();
	}

	private static class InternalUITest {

		private static int findingCounter = 1;
		private SecHubToolWindowUISupport supportToTest;

		private void start() {
			JFrame frame = new JFrame("test");
			JMenu menu = new JMenu("Action");
			menu.add(new SetNewModelAction());
			JMenuBar menuBar = new JMenuBar();

			menuBar.add(menu);
			frame.setJMenuBar(menuBar);

			JTree callHierarchyTree = new JTree();
			FindingModel model = createTestModel();

			JTable reportTable = new JTable();
			JTable callStepDetailTable = new JTable();
			JSplitPane pane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(callHierarchyTree),
					new JScrollPane(callStepDetailTable));
			JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(reportTable), pane1);
			frame.add(pane2);

			JPanel southPanel = new JPanel();
			JLabel reportFindingLabel = new JLabel("No finding selected");
			JLabel callStepLabel = new JLabel("No call step selected");
			southPanel.add(reportFindingLabel);
			southPanel.add(new JLabel("---"));
			southPanel.add(callStepLabel);
			frame.add(southPanel, BorderLayout.SOUTH);

			callHierarchyTree.setPreferredSize(new Dimension(800, 600));

			SecHubToolWindowUIContext context = new SecHubToolWindowUIContext();
			context.findingTable=reportTable;
			context.callHierarchyDetailTable=callStepDetailTable;
			context.callHierarchyTree=callHierarchyTree;
			context.errorLog=new ErrorLog() {
			};
			context.cweIdLabel = new JLabel("cwe");
			supportToTest = new SecHubToolWindowUISupport(context);
			supportToTest.initialize();
			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			supportToTest.setModel(model);
			supportToTest.addCallStepChangeListener((callstep) -> {
				callStepLabel.setText(callstep.getRelevantPart());
			});

			supportToTest.addReportFindingSelectionChangeListener((finding) -> {
				reportFindingLabel.setText(finding.getDescription());
			});
		}

		private FindingModel createTestModel() {
			FindingModel model = new FindingModel();
			append(model, "alpha", Severity.CRITICAL);
			append(model, "beta", Severity.HIGH);
			append(model, "gamma", Severity.MEDIUM);
			append(model, "delta", Severity.LOW);
			append(model, "epsilon", Severity.INFO);
			return model;
		}

		private void append(FindingModel model, String prefix, Severity severity) {
			int step = 1;
			FindingNode node1 = FindingNode.builder().setId(findingCounter++).setDescription(generateDescription(prefix))
					.setColumn(12).setLine(1).setLocation("/some/where/found/Xyz.java").setSeverity(severity)
					.setCallStackStep(step++).setRelevantPart("i am relevant1")
					.setSource("I am source... and i am relevant").build();

			FindingNode node2 = FindingNode.builder().setId(findingCounter++).setDescription(generateDescription(prefix))
					.setColumn(13).setLine(2).setLocation("/some/where/found/Xyz.java").setSeverity(severity)
					.setCallStackStep(step++).setRelevantPart("i am relevant2")
					.setSource("I am source... and i am relevant").build();

			FindingNode node3 = FindingNode.builder().setId(findingCounter++).setDescription(generateDescription(prefix))
					.setColumn(14).setLine(3).setLocation("/some/where/found/Xyz.java").setSeverity(severity)
					.setCallStackStep(step++).setRelevantPart("i am relevant3")
					.setSource("I am source... and i am relevant").build();

			node1.getChildren().add(node2);
			node2.getChildren().add(node3);

			model.getFindings().add(node1);
		}

		private String generateDescription(String prefix) {
			return "describe-" + prefix + "_" + findingCounter;
		}

		private class SetNewModelAction extends AbstractAction {
			private static final long serialVersionUID = 1L;

			private SetNewModelAction() {
				putValue(Action.NAME, "set new model");
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				InternalUITest uiTest = InternalUITest.this;
				uiTest.supportToTest.setModel(uiTest.createTestModel());
			}

		}
	}
}
