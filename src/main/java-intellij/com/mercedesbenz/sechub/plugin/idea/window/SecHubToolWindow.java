// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.plugin.model.FileLocationExplorer;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUIContext;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUISupport;
import com.mercedesbenz.sechub.plugin.ui.SecHubTreeNode;
import com.mercedesbenz.sechub.plugin.idea.util.ErrorLogger;
import com.mercedesbenz.sechub.plugin.util.SimpleStringUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class SecHubToolWindow {
    private static final Logger LOG = Logger.getInstance(SecHubToolWindow.class);

    private final SecHubToolWindowUISupport support;
    private Icon iconCallHierarchyElement;

    private static SecHubToolWindow INSTANCE;

    private JPanel sechubToolWindowContent;
    private JPanel secHubReportPanel;
    private JPanel sechubCallHierarchy;
    private JPanel secHubReportHeaderPanel;
    private JLabel scanResultForJobLabel;
    private JLabel trafficLightLabel;
    private JLabel findingsLabel;
    private JPanel secHubReportContentPanel;
    private JTable reportTable;
    private JTable callStepDetailTable;
    private JPanel detailPanel;
    private JTextField trafficLightText;
    private JTextField scanResultForJobText;
    private JTextField findingsText;
    private JTree callHierarchyTree;
    private JPanel callStepDetailPanel;
    private JTextArea reportSourceCodeTextArea;
    private JLabel findingHeaderLabel;
    private JSplitPane mainSplitPane;
    private JLabel reportSourceCodeLabel;
    private JSplitPane callHierarchySplitPane;
    private JLabel cweIdLabel;
    private FindingModel model;


    public SecHubToolWindow(ToolWindow toolWindow) {
        //mainSplitPane.setDividerLocation(0.5);
        iconCallHierarchyElement= IconLoader.findIcon("/icons/activity.png");
        callHierarchySplitPane.setDividerLocation(0.5);
        SecHubToolWindowUIContext context = new SecHubToolWindowUIContext();
        context.findingTable=reportTable;
        context.callHierarchyTree=callHierarchyTree;
        context.callHierarchyDetailTable=callStepDetailTable;
        context.errorLog=ErrorLogger.getInstance();
        context.cweIdLabel =cweIdLabel;

        support = new SecHubToolWindowUISupport(context);

        support.addCallStepChangeListener((callStep)->{
            reportSourceCodeTextArea.setText(callStep == null ? "" : SimpleStringUtil.toStringTrimmed(callStep.getSource()));
            /* now show in editor as well */
            showInEditor(callStep);
        });
        support.addReportFindingSelectionChangeListener((finding)->{
            findingHeaderLabel.setText("Finding "+finding.getId()+" - " +finding.getDescription());
        });
        support.initialize();

        installDragAndDrop();
        customizeCallHierarchyTree();

    }
    private void installDragAndDrop() {
        SecHubToolWindowTransferSupport transferHandler = new SecHubToolWindowTransferSupport(ErrorLogger.getInstance());
        reportTable.setTransferHandler(transferHandler);
        callHierarchyTree.setTransferHandler(transferHandler);

        sechubToolWindowContent.setTransferHandler(transferHandler);
        reportSourceCodeTextArea.setTransferHandler(transferHandler);
        callStepDetailTable.setTransferHandler(transferHandler);
    }

    private void customizeCallHierarchyTree() {
        callHierarchyTree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (!(value instanceof DefaultMutableTreeNode)) {
                    return;
                }
                SecHubTreeNode treeNode = (SecHubTreeNode) value;
                FindingNode findingNode = treeNode.getFindingNode();
                if (findingNode == null) {
                    return;
                }
                String relevantPart = findingNode.getRelevantPart();
                append(relevantPart ==null ? "unknown" : relevantPart, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                append(" - ");
                String fileName = findingNode.getFileName();
                append(fileName == null ? "unknown": fileName, SimpleTextAttributes.GRAY_ATTRIBUTES);

                setIcon(iconCallHierarchyElement);

            }
        });

    }

    public static void registerInstance(SecHubToolWindow secHubToolWindow) {
        LOG.info("register tool windows instance:"+secHubToolWindow);
        INSTANCE = secHubToolWindow;
    }

    public static SecHubToolWindow getInstance() {
        return INSTANCE;
    }

    private void showInEditor(FindingNode callStep) {
        if (callStep == null) {
            return;
        }
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        if (activeProject == null) {
            LOG.error("No active project found, so cannot show current call step in editor!");
            return;
        }
        FileLocationExplorer explorer = new FileLocationExplorer();
        VirtualFile projectDir = ProjectUtil.guessProjectDir(activeProject);
        if (projectDir==null){
            return;
        }

        explorer.getSearchFolders().add(VirtualFileCompatibilityLayer.toNioPath(projectDir));

        List<Path> pathes = null;
        try {
            pathes = explorer.searchFor(callStep.getLocation());
        } catch (IOException e) {
            LOG.error("Lookup for sources failed", e);
            return;
        }
        if (pathes.isEmpty()) {
            LOG.error("No source found!");
            return;
        }
        if (pathes.size() > 1) {
            LOG.warn("Multiple pathes found useing only first one");
        }
        Path first = pathes.get(0);
        @Nullable VirtualFile firstAsVirtualFile = VirtualFileManager.getInstance().findFileByUrl(first.toUri().toString());
        if (firstAsVirtualFile == null) {
            LOG.error("Found in normal filesystem but not in virutal one:" + first);
            return;
        }
        int line = callStep.getLine();
        int column = callStep.getColumn();

        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(activeProject, firstAsVirtualFile, line-1, column);
        fileDescriptor.navigateInEditor(activeProject, true);
    }


    public JPanel getContent() {
        return sechubToolWindowContent;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void update(FindingModel model) {
        UUID jobUUID = model.getJobUUID();
        TrafficLight trafficLight = model.getTrafficLight();

        scanResultForJobText.setText(jobUUID == null ? "" : jobUUID.toString());
        trafficLightText.setText(trafficLight == null ? "" : trafficLight.toString());
        findingsText.setText("" + model.getFindings().size());
        this.model = model;
        support.setFindingModel(model);
    }
}