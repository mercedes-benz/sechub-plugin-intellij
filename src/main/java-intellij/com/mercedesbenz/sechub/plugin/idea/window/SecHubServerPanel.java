package com.mercedesbenz.sechub.plugin.idea.window;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.mercedesbenz.sechub.sdk.settings.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class SecHubServerPanel implements SecHubPanel {

    private static final Logger LOG = Logger.getInstance(SecHubServerPanel.class);
    private static SecHubServerPanel INSTANCE;
    private JBTextField serverUrlText;
    private JPanel contentPanel;

    public SecHubServerPanel() {
        createComponents();
    }

    public static void registerInstance(SecHubServerPanel secHubToolWindow) {
        LOG.info("register tool windows instance:" + secHubToolWindow);
        INSTANCE = secHubToolWindow;
    }

    public static SecHubServerPanel getInstance() {
        return INSTANCE;
    }

    @Override
    public JPanel getContent() {
        return contentPanel;
    }

    public void update(String serverURL) {
        serverUrlText.setText(serverURL);
    }

    private void createComponents() {
        contentPanel = new JBPanel<>();
        contentPanel.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        String serverURL = Objects.requireNonNull(AppSettings.getInstance().getState()).serverURL;
        serverUrlText = new JBTextField();
        serverUrlText.setText(serverURL);
        serverUrlText.setEditable(false);

        top.add(new JLabel("Server URL: "), BorderLayout.WEST);
        top.add(serverUrlText);
        contentPanel.add(top, BorderLayout.NORTH);
    }
}
