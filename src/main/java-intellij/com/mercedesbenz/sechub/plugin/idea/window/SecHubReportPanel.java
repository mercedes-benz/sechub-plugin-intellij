package com.mercedesbenz.sechub.plugin.idea.window;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.*;
import com.intellij.ui.components.*;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import com.mercedesbenz.sechub.plugin.idea.util.ErrorLogger;
import com.mercedesbenz.sechub.plugin.model.FileLocationExplorer;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUIContext;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUISupport;
import com.mercedesbenz.sechub.plugin.ui.SecHubTreeNode;
import com.mercedesbenz.sechub.plugin.util.JTreeUtil;
import com.mercedesbenz.sechub.plugin.util.SimpleStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class SecHubReportPanel {
    private static final Logger LOG = Logger.getInstance(SecHubReportPanel.class);
    private static final int SECHUB_REPORT_DEFAULT_GAP = 5;
    private static SecHubReportPanel INSTANCE;

    private SecHubToolWindowUISupport support;
    private Icon callHierarchyElementIcon;
    private JPanel sechubToolWindowContent;
    private JPanel verticalSecHubReportPanel;
    private JPanel sechubCallHierarchy;
    private JPanel secHubReportHeaderPanel;
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
    private JBTextArea findingHeaderLabel;
    private JSplitPane mainSplitPane;
    private JLabel reportSourceCodeLabel;
    private JSplitPane callHierarchySplitPane;
    private JLabel cweIdLabel;
    private FindingModel model;

    private ToolWindow toolWindow;


    public SecHubReportPanel(ToolWindow toolWindow) {
        this.toolWindow=toolWindow;
        createComponentsAndSupport();
        installDragAndDrop();
        customizeCallHierarchyTree();
        reset();
    }

    @NotNull
    private void createComponentsAndSupport() {
        sechubToolWindowContent = new JBPanel();
        sechubToolWindowContent.setLayout(new BorderLayout());

        verticalSecHubReportPanel = new JBPanel();
        verticalSecHubReportPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));

        sechubCallHierarchy = new JBPanel();
        sechubCallHierarchy.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        secHubReportHeaderPanel = new JBPanel();
        secHubReportHeaderPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));


        secHubReportContentPanel = new JBPanel();
        secHubReportContentPanel.setLayout(new BorderLayout());

        reportTable = new JBTable();
        callStepDetailTable = new JBTable();
        detailPanel = new JPanel();
        detailPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        trafficLightText = new JBTextField();
        trafficLightText.setEditable(false);

        scanResultForJobText = new JBTextField();
        scanResultForJobText.setEditable(false);

        findingsText = new JBTextField();
        findingsText.setEditable(false);

        callHierarchyTree = new Tree();
        callStepDetailPanel = new JBPanel();
        callStepDetailPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));

        reportSourceCodeTextArea = new JBTextArea();
        findingHeaderLabel = new JBTextArea();
        findingHeaderLabel.setAutoscrolls(true);
        findingHeaderLabel.setEditable(false);

        reportSourceCodeLabel = new JBLabel();
        cweIdLabel = new JBLabel();

        verticalSecHubReportPanel.add(secHubReportHeaderPanel);
        verticalSecHubReportPanel.add(secHubReportContentPanel, VerticalLayout.FILL);

        secHubReportHeaderPanel.add(scanResultForJobText);
        secHubReportHeaderPanel.add(trafficLightText);
        secHubReportHeaderPanel.add(scanResultForJobText);

        JBScrollPane reportTableScrollPane = new JBScrollPane(reportTable);
        reportTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        reportTableScrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.BOTTOM + SideBorder.LEFT + SideBorder.TOP));

        secHubReportContentPanel.add(reportTableScrollPane, BorderLayout.CENTER);

        sechubCallHierarchy.add(detailPanel);
        findingHeaderLabel.setLineWrap(true);
        findingHeaderLabel.setWrapStyleWord(true);
        JBScrollPane findingHeaderScrollpane = new JBScrollPane(findingHeaderLabel);
        findingHeaderScrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        findingHeaderScrollpane.setBorder(IdeBorderFactory.createBorder(SideBorder.BOTTOM + SideBorder.LEFT + SideBorder.TOP));
        detailPanel.add(findingHeaderScrollpane);
        detailPanel.add(cweIdLabel);

        JBScrollPane callHierarchyTreeScrollPane = new JBScrollPane(callHierarchyTree);
        callHierarchyTreeScrollPane.setMinimumSize(new Dimension(400,-1));
        callHierarchyTreeScrollPane.setMinimumSize(new Dimension(600,-1));

        JBSplitter hierarchyAndDetailSplitter = new OnePixelSplitter(false);
        hierarchyAndDetailSplitter.setFirstComponent(callHierarchyTreeScrollPane);
        hierarchyAndDetailSplitter.setSecondComponent(callStepDetailPanel);
        hierarchyAndDetailSplitter.setShowDividerControls(true);
        hierarchyAndDetailSplitter.setShowDividerIcon(true);
        hierarchyAndDetailSplitter.setDividerPositionStrategy(Splitter.DividerPositionStrategy.DISTRIBUTE);
        sechubCallHierarchy.add(hierarchyAndDetailSplitter);

        JBSplitter reportAndDetailsSplitter = new OnePixelSplitter(true);
        reportAndDetailsSplitter.setShowDividerControls(true);
        reportAndDetailsSplitter.setShowDividerIcon(true);
        reportAndDetailsSplitter.setFirstComponent(verticalSecHubReportPanel);
        reportAndDetailsSplitter.setSecondComponent(sechubCallHierarchy);
        reportAndDetailsSplitter.setDividerPositionStrategy(Splitter.DividerPositionStrategy.DISTRIBUTE);

        sechubToolWindowContent.add(reportAndDetailsSplitter, BorderLayout.CENTER);

        JBPanel reportSourceCodePanel = new JBPanel();
        reportSourceCodePanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        reportSourceCodePanel.add(reportSourceCodeLabel);
        reportSourceCodePanel.add(new JBScrollPane(reportSourceCodeTextArea));


        callStepDetailTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()>1){
                    Object component = callHierarchyTree.getLastSelectedPathComponent();
                    if (component instanceof DefaultMutableTreeNode){
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) component;
                        Object userObject = treeNode.getUserObject();
                        if (userObject instanceof FindingNode){
                            showInEditor((FindingNode) userObject);
                        }
                    }
                }
            }
        });

        callStepDetailPanel.add(new JBScrollPane(callStepDetailTable));
        callStepDetailPanel.add(new JBScrollPane(reportSourceCodePanel));

        callHierarchyElementIcon = AllIcons.General.ChevronDown;
        SecHubToolWindowUIContext context = new SecHubToolWindowUIContext();
        context.findingTable = reportTable;
        context.callHierarchyTree = callHierarchyTree;
        context.callHierarchyDetailTable = callStepDetailTable;
        context.errorLog = ErrorLogger.getInstance();
        context.cweIdLabel = cweIdLabel;

        support = new SecHubToolWindowUISupport(context);

        support.addCallStepChangeListener((callStep, showEditor) -> {
            reportSourceCodeTextArea.setText(callStep == null ? "" : SimpleStringUtil.toStringTrimmed(callStep.getSource()));
            /* now show in editor as well */
            if (showEditor){
                showInEditor(callStep);
            }
        });
        support.addReportFindingSelectionChangeListener((finding) -> {
            findingHeaderLabel.setText("Finding " + finding.getId() + ":\n"+(finding.getDescription()== null ? "No description available" : finding.getDescription()));
        });
        support.initialize();
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
                append(relevantPart == null ? "unknown" : relevantPart, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                append(" - ");
                String fileName = findingNode.getFileName();
                append(fileName == null ? "unknown" : fileName, SimpleTextAttributes.GRAY_ATTRIBUTES);

                setIcon(callHierarchyElementIcon);

            }
        });
    }

    public static void registerInstance(SecHubReportPanel secHubToolWindow) {
        LOG.info("register tool windows instance:" + secHubToolWindow);
        INSTANCE = secHubToolWindow;
    }

    public static SecHubReportPanel getInstance() {
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
        if (projectDir == null) {
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
            ToolWindowBalloonShowOptions options = new ToolWindowBalloonShowOptions(toolWindow.getId(), MessageType.WARNING,
                    "No source found for location: "+callStep.getLocation(),null,null, (builder) -> {
                builder.setFadeoutTime(1500);
            });

            ToolWindowManager.getInstance(activeProject).notifyByBalloon(options);
            return;
        }
        if (pathes.size() > 1) {
            LOG.warn("Multiple paths found using only first one");
        }
        Path first = pathes.get(0);
        @Nullable VirtualFile firstAsVirtualFile = VirtualFileManager.getInstance().findFileByUrl(first.toUri().toString());
        if (firstAsVirtualFile == null) {
            LOG.error("Found in normal filesystem but not in virtual one:" + first);
            return;
        }
        int line = callStep.getLine();
        int column = callStep.getColumn();

        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(activeProject, firstAsVirtualFile, line - 1, column);
        fileDescriptor.navigateInEditor(activeProject, true);
    }


    public JPanel getContent() {
        return sechubToolWindowContent;
    }

    public void update(FindingModel model) {
        // reset old field data
        findingHeaderLabel.setText("");
        cweIdLabel.setText("");
        reportSourceCodeLabel.setText("");
        reportSourceCodeTextArea.setText("");
        support.showFindingNode(null, false);

        UUID jobUUID = model.getJobUUID();

        TrafficLight trafficLight = model.getTrafficLight();
        if (trafficLight == null) {
            trafficLight = TrafficLight.OFF;
        }


        scanResultForJobText.setText("SecHub Job UUID:"+ (jobUUID == null ? "" : jobUUID.toString()));

        trafficLightText.setText(trafficLight==null ? "" : trafficLight.toString());

        if (trafficLight!=null){
            trafficLightText.setText(trafficLightText.getText()+", "+model.getFindings().size()+" findings");
        }
        this.model = model;
        support.setFindingModel(model);
    }

    public void reset() {
        update(new FindingModel());
        support.resetTablePresentation();
        support.resetDetailsTablePresentation();
    }
}