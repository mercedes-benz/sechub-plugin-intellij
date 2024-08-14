package com.mercedesbenz.sechub.plugin.idea.window;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.sdk.settings.AppSettings;
import com.mercedesbenz.sechub.sdk.settings.AppSettingsCredentialsSupport;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class SecHubServerPanel implements SecHubPanel {

    private static final Logger LOG = Logger.getInstance(SecHubServerPanel.class);
    private static SecHubServerPanel INSTANCE;
    private JPanel contentPanel;
    private JBTextField serverUrlText;
    private JBTextField serverActiveText;

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

    public void update(String serverURL, boolean isActive) {
        if (serverURL.isBlank()) {
            serverURL = "Server URL not configured";
        }
        serverUrlText.setText(serverURL);

        if (isActive) {
            serverActiveText.setText("Connection alive");
        } else {
            serverActiveText.setText("Connection failed");
        }
    }

    private void createComponents() {
        contentPanel = new JBPanel<>();
        contentPanel.setLayout(new BorderLayout());

        final JPanel content = createPanel();
        contentPanel.add(content, BorderLayout.NORTH);
    }

    @NotNull
    private JPanel createPanel() {
        JPanel content = new JPanel(new BorderLayout());
        serverUrlText = new JBTextField();
        serverActiveText = new JBTextField();

        serverUrlText.setEditable(false);
        String serverURL = Objects.requireNonNull(AppSettings.getInstance().getState()).serverURL;
        if (serverURL.isBlank()) {
            serverURL = "Server URL not configured";
        } else {
            initSecHubAccess();
        }
        serverUrlText.setText(serverURL);

        JPanel serverStatePanel = new JPanel();
        serverStatePanel.setLayout(new BorderLayout());

        JButton serverActiveButton = new JButton("connect");
        serverActiveButton.addActionListener(e -> {
            SecHubAccess secHubAccess = SecHubAccess.getInstance();
            update(Objects.requireNonNull(AppSettings.getInstance().getState()).serverURL, secHubAccess.isSecHubServerAlive());
        });

        serverActiveText.setEditable(false);
        serverActiveText.setText("Server connection not checked");
        serverStatePanel.add(serverActiveText, BorderLayout.WEST);
        serverStatePanel.add(serverActiveButton, BorderLayout.EAST);

        JPanel labelPane = new JPanel();
        labelPane.setLayout(new GridLayout(0, 1));
        labelPane.add(new JBLabel("Server URL: "));
        labelPane.add(new JBLabel("Server connection: "));

        JPanel fieldPane = new JPanel();
        fieldPane.setLayout(new GridLayout(0, 1));
        fieldPane.add(serverUrlText);
        fieldPane.add(serverStatePanel);

        content.add(labelPane, BorderLayout.WEST);
        content.add(fieldPane, BorderLayout.CENTER);
        return content;
    }

    private void initSecHubAccess() {
        AppSettingsCredentialsSupport appSettingsCredentialsSupport = new AppSettingsCredentialsSupport();
        Credentials credentials = appSettingsCredentialsSupport.retrieveCredentials();
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        SecHubAccess secHubAccess = SecHubAccess.getInstance();
        secHubAccess.setSecHubServerUrl(state.serverURL);
        secHubAccess.setUserId(credentials.getUserName());
        secHubAccess.setApiToken(credentials.getPasswordAsString());
        secHubAccess.setTrustAllCertificates(true);

        secHubAccess.initSecHubClient();
    }
}
