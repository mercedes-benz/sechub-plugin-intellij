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
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.*;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.plugin.idea.IntellijComponentBuilder;
import com.mercedesbenz.sechub.plugin.idea.IntellijRenderDataProvider;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import com.mercedesbenz.sechub.plugin.idea.util.ErrorLogger;
import com.mercedesbenz.sechub.plugin.model.FileLocationExplorer;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUIContext;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUISupport;
import com.mercedesbenz.sechub.plugin.ui.SecHubTreeNode;
import com.mercedesbenz.sechub.plugin.util.SimpleStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.DefaultMutableTreeNode;
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

    private ToolWindow toolWindow;
    private JLabel trafficLightIconLabel;
    private JLabel amountOfFindingsLabel;
    private JBTextField scanResultForJobText;
    private JBTable callStepDetailTable;
    private Tree callHierarchyTree;
    private JBTable reportTable;
    private JBLabel cweIdLabel;
    private JBTextArea reportSourceCodeTextArea;
    private JBTextArea findingDescriptionTextArea;

    private JPanel contentPanel;
    private JBLabel findingLabel;
    private JTextArea findingSolutionTextArea;
    private JBTabbedPane southTabPane;
    private JPanel webRequestPanel;
    private JPanel webResponsePanel;
    private JTabbedPane descriptionAndSolutionTabbedPane;
    private JTextArea webRequestTextArea;
    private JTextArea webResponseTextArea;
    private JTextArea attackTextArea;
    private JPanel callHierarchyPanel;
    private JPanel attackPanel;


    public SecHubReportPanel(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;

        createComponents();
        createAndInstallSupport();

        installDragAndDrop();

        customizeCallHierarchyTree();
        customizeCallStepDetailsTable();

        reset();
    }

    @NotNull
    private void createComponents() {

        JPanel contentNorth = createReportTablePanel();
        JPanel contentSouth = createFindingPanel();

        JBSplitter reportAndDetailsSplitterPanel = new OnePixelSplitter(true);
        reportAndDetailsSplitterPanel.setShowDividerControls(true);
        reportAndDetailsSplitterPanel.setShowDividerIcon(true);
        reportAndDetailsSplitterPanel.setFirstComponent(new JBScrollPane(contentNorth));
        reportAndDetailsSplitterPanel.setSecondComponent(contentSouth);
        reportAndDetailsSplitterPanel.setDividerPositionStrategy(Splitter.DividerPositionStrategy.DISTRIBUTE);

        callHierarchyElementIcon = AllIcons.General.ChevronDown;

        contentPanel = new JBPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(reportAndDetailsSplitterPanel, BorderLayout.CENTER);

    }

    private JPanel createReportTablePanel() {

        JBTable reportTable = new JBTable();
        JBScrollPane reportTableScrollPane = new JBScrollPane(reportTable);

        JLabel trafficLightIconLabel = new JLabel();
        JLabel amountOfFindingsLabel = new JLabel();


        JPanel secHubReportTablePanel = new JBPanel();
        secHubReportTablePanel.setLayout(new BorderLayout());

        JBTextField scanResultForJobText = new JBTextField();
        scanResultForJobText.setEditable(false);
        scanResultForJobText.setBorder(null); // avoid jumping field in UI - looks now like a label, but people can select and copy the job uuid if wanted...

        JPanel secHubReportHeaderPanel = new JBPanel();
        secHubReportHeaderPanel.setLayout(new HorizontalLayout(SECHUB_REPORT_DEFAULT_GAP));
        secHubReportHeaderPanel.add(trafficLightIconLabel);
        secHubReportHeaderPanel.add(amountOfFindingsLabel);
        secHubReportHeaderPanel.add(scanResultForJobText);


        JPanel secHubReportContentPanel = new JBPanel();
        secHubReportContentPanel.setLayout(new BorderLayout());
        secHubReportTablePanel.add(secHubReportHeaderPanel, BorderLayout.NORTH);
        secHubReportTablePanel.add(secHubReportContentPanel, BorderLayout.CENTER);
        secHubReportContentPanel.add(reportTableScrollPane, BorderLayout.CENTER);

        /* now setup fields */
        this.scanResultForJobText = scanResultForJobText;
        this.trafficLightIconLabel = trafficLightIconLabel;
        this.amountOfFindingsLabel = amountOfFindingsLabel;

        this.reportTable = reportTable;

        return secHubReportTablePanel;
    }

    private JPanel createFindingPanel() {

        JComponent findingNorthPanel = createFindingNorthComponent();
        JComponent findingSouthPanel = createFindingSouthComponent();

        JBSplitter findingAndFindingStepsSplitter = new OnePixelSplitter(true);
        findingAndFindingStepsSplitter.setFirstComponent(findingNorthPanel);
        findingAndFindingStepsSplitter.setSecondComponent(findingSouthPanel);
        findingAndFindingStepsSplitter.setShowDividerControls(true);
        findingAndFindingStepsSplitter.setShowDividerIcon(true);
        findingAndFindingStepsSplitter.setProportion(0.1f);

        return findingAndFindingStepsSplitter;

    }

    private JComponent createFindingNorthComponent() {
        JBLabel cweIdLabel = new JBLabel();

        JBLabel findingLabel = new JBLabel();


        JPanel cweAndFindingPanel = new JBPanel<>();
        cweAndFindingPanel.setLayout(new HorizontalLayout(SECHUB_REPORT_DEFAULT_GAP));
        cweAndFindingPanel.add(findingLabel);
        cweAndFindingPanel.add(cweIdLabel);

        JBTextArea findingDescriptionTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        JBTextArea findingSolutionTextArea = prepareNonEditLargeTextArea(new JBTextArea());

        descriptionAndSolutionTabbedPane = new JBTabbedPane();
        descriptionAndSolutionTabbedPane.add("Description", new JBScrollPane(findingDescriptionTextArea));
        descriptionAndSolutionTabbedPane.add("Solution", new JBScrollPane(findingSolutionTextArea));

        JPanel northPanel = new JBPanel();
        northPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        northPanel.add(cweAndFindingPanel);
        northPanel.add(descriptionAndSolutionTabbedPane);

        /* setup now as fields */
        this.findingLabel = findingLabel;
        this.cweIdLabel = cweIdLabel;
        this.findingDescriptionTextArea = findingDescriptionTextArea;
        this.findingSolutionTextArea = findingSolutionTextArea;

        return northPanel;
    }

    private JBTextArea prepareNonEditLargeTextArea(JBTextArea textArea){
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        Caret caret = textArea.getCaret();
        if (caret instanceof DefaultCaret){
            DefaultCaret defaultCaret = (DefaultCaret) caret;
            defaultCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        return textArea;
    }

    private JComponent createFindingSouthComponent() {
        southTabPane = new JBTabbedPane();

        createCallHierachyComponents();

        createWebRequestComponents();
        createWebResponseComponents();
        createAttackComponents();

        return southTabPane;
    }

    private void createWebResponseComponents() {
        webResponseTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        webResponsePanel = new JBPanel<>();
        webResponsePanel.setLayout(new BorderLayout());
        webResponsePanel.add(webResponseTextArea, BorderLayout.CENTER);
    }

    private void createWebRequestComponents() {
        webRequestTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        webRequestPanel = new JBPanel<>();
        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.add(new JBScrollPane(webRequestTextArea), BorderLayout.CENTER);
    }
    private void createAttackComponents() {
        attackTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        attackPanel = new JBPanel<>();
        attackPanel.setLayout(new BorderLayout());
        attackPanel.add(new JBScrollPane(attackTextArea), BorderLayout.CENTER);
    }

    private void createCallHierachyComponents() {
        Tree callHierarchyTree = new Tree();

        JBTextArea reportSourceCodeTextArea = prepareNonEditLargeTextArea(new JBTextArea());

        JBLabel reportSourceCodeLabel = new JBLabel("Source:");

        JBPanel reportSourceCodePanel = new JBPanel();
        reportSourceCodePanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        reportSourceCodePanel.add(reportSourceCodeLabel);
        reportSourceCodePanel.add(reportSourceCodeTextArea);


        JBTable callStepDetailTable = new JBTable();
        JBPanel callStepDetailPanel = new JBPanel();
        callStepDetailPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        callStepDetailPanel.add(new JBScrollPane(callStepDetailTable));
        callStepDetailPanel.add(reportSourceCodePanel);

        OnePixelSplitter callHierarchySplitterPanel = new OnePixelSplitter(false);
        callHierarchySplitterPanel.setFirstComponent(new JBScrollPane(callHierarchyTree));
        callHierarchySplitterPanel.setSecondComponent(new JBScrollPane(callStepDetailPanel));
        callHierarchySplitterPanel.setShowDividerControls(true);
        callHierarchySplitterPanel.setShowDividerIcon(true);
        callHierarchySplitterPanel.setDividerPositionStrategy(Splitter.DividerPositionStrategy.DISTRIBUTE);

        /* now set as fields */
        this.callStepDetailTable = callStepDetailTable;
        this.callHierarchyTree = callHierarchyTree;
        this.callHierarchyPanel=callHierarchySplitterPanel;
        this.reportSourceCodeTextArea = reportSourceCodeTextArea;
    }

    private void customizeCallStepDetailsTable() {
        callStepDetailTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    Object component = callHierarchyTree.getLastSelectedPathComponent();
                    if (component instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) component;
                        Object userObject = treeNode.getUserObject();
                        if (userObject instanceof FindingNode) {
                            showInEditor((FindingNode) userObject);
                        }
                    }
                }
            }
        });
    }

    private void createAndInstallSupport() {
        SecHubToolWindowUIContext context = new SecHubToolWindowUIContext();
        context.findingTable = reportTable;

        context.callHierarchyTabComponent = callHierarchyPanel;
        context.callHierarchyTree = callHierarchyTree;
        context.callHierarchyDetailTable = callStepDetailTable;

        context.errorLog = ErrorLogger.getInstance();
        context.cweIdLabel = cweIdLabel;
        context.findingRenderDataProvider = new IntellijRenderDataProvider();
        context.componentBuilder=new IntellijComponentBuilder();
        context.findingTypeDetailsTabbedPane = southTabPane;

        context.descriptionAndSolutionTabbedPane = descriptionAndSolutionTabbedPane;


        context.webResponseTabComponent = webResponsePanel;
        context.webResponseTextArea = webResponseTextArea;

        context.webRequestTabComponent = webRequestPanel;
        context.webRequestTextArea = webRequestTextArea;

        context.attackTextArea = attackTextArea;
        context.attackTabComponent = attackPanel;

        support = new SecHubToolWindowUISupport(context);

        support.addCallStepChangeListener((callStep, showEditor) -> {
            reportSourceCodeTextArea.setText(callStep == null ? "" : SimpleStringUtil.toStringTrimmed(callStep.getSource()) + "\n");
            /* now show in editor as well */
            if (showEditor) {
                showInEditor(callStep);
            }
        });
        support.addReportFindingSelectionChangeListener((finding) -> {
            findingLabel.setText("Finding " + finding.getId() + ":");
            findingDescriptionTextArea.setText(finding.getDescription() == null ? "No description available" : finding.getDescription());
            if (finding.getSolution() == null) {
                if (finding.getCweId() == null) {
                    findingSolutionTextArea.setText("No solution available");
                } else {
                    findingSolutionTextArea.setText("Please follow the CWE link and read the solution text there.");
                }
            } else {
                findingSolutionTextArea.setText(finding.getSolution());
            }
        });
        support.initialize();
    }

    private void installDragAndDrop() {
        SecHubToolWindowTransferSupport transferHandler = new SecHubToolWindowTransferSupport(ErrorLogger.getInstance());
        reportTable.setTransferHandler(transferHandler);
        callHierarchyTree.setTransferHandler(transferHandler);

        contentPanel.setTransferHandler(transferHandler);
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
                if (relevantPart != null) {
                    append(relevantPart, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                    append(" - ");
                }
                String fileName = findingNode.getFileName();
                append(fileName == null ? "Unknown" : fileName, SimpleTextAttributes.GRAY_ATTRIBUTES);

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
        if (!callStep.canBeShownInCallHierarchy()) {
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
                    "No source found for location: " + callStep.getLocation(), null, null, (builder) -> {
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
        return contentPanel;
    }

    public void update(FindingModel model) {
        // reset old field data
        findingLabel.setText("");
        cweIdLabel.setText("");
        findingDescriptionTextArea.setText("");
        findingSolutionTextArea.setText("");
        reportSourceCodeTextArea.setText("");
        descriptionAndSolutionTabbedPane.setVisible(false);
        support.showFindingNode(null, false);

        UUID jobUUID = model.getJobUUID();

        TrafficLight trafficLight = model.getTrafficLight();
        if (trafficLight == null) {
            trafficLight = TrafficLight.OFF;
        }
        if (jobUUID == null) {
            amountOfFindingsLabel.setText("No SecHub report loaded");
            scanResultForJobText.setText("");
        } else {
            amountOfFindingsLabel.setText(model.getFindings().size() + " findings in job:");
            scanResultForJobText.setText(jobUUID.toString());
        }

        trafficLightIconLabel.setIcon(support.getRenderDataProvider().getIconForTrafficLight(trafficLight));
        trafficLightIconLabel.setToolTipText("Traffic light is:" + trafficLight.toString());

        support.setFindingModel(model);
    }

    public void reset() {
        update(new FindingModel());

        support.resetTablePresentation();
        support.resetCallHierarchyStepTable();
        support.resetFindingNodeTabPane();
        support.resetDescriptionAndSolutionTabPane(false);
    }
}