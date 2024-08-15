package com.mercedesbenz.sechub.sdk.settings;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.options.Configurable;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessSupport;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubServerPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/*
 * Provides controller functionality for application settings.
 */
final class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent appSettingsComponent;
    private AppSettingsCredentialsSupport appSettingsCredentialsSupport;

    // A default constructor with no arguments is required because
    // this implementation is registered as an applicationConfigurable

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SecHub";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return appSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        appSettingsComponent = new AppSettingsComponent();
        appSettingsCredentialsSupport = new AppSettingsCredentialsSupport();
        return appSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        String currentPassword = "";
        String userName = "";

        Credentials credentials = appSettingsCredentialsSupport.retrieveCredentials();
        if (credentials != null) {
            currentPassword = credentials.getPasswordAsString();
            userName = credentials.getUserName();
        }

        return !appSettingsComponent.getUserNameText().equals(userName) ||
                appSettingsComponent.getApiTokenPassword().equals(currentPassword) ||
                appSettingsComponent.getServerUrlText().equals(state.serverURL);
    }

    @Override
    public void apply() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        state.serverURL = appSettingsComponent.getServerUrlText();

        Credentials credentials = new Credentials(appSettingsComponent.getUserNameText(), appSettingsComponent.getApiTokenPassword());
        appSettingsCredentialsSupport.storeCredentials(credentials);

        updateComponents(state);
    }



    @Override
    public void reset() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        appSettingsComponent.setServerUrlText(state.serverURL);

        Credentials credentials = appSettingsCredentialsSupport.retrieveCredentials();

        if (credentials != null) {
            String password = credentials.getPasswordAsString();
            String userName = credentials.getUserName();

            assert userName != null;
            appSettingsComponent.setUserNameText(userName);
            assert password != null;
            appSettingsComponent.setApiTokenPassword(password);
        }
    }

    @Override
    public void disposeUIResources() {
        appSettingsComponent = null;
    }

    private static void updateComponents(AppSettings.State state) {
        // Updating the SecHubAccess object with the new credentials and server URL
        SecHubAccessSupport secHubAccessSupport = new SecHubAccessSupport();
        secHubAccessSupport.setSecHubAccessComponents();

        // Updating the server URL in the SecHubServerPanel and check alive status
        SecHubServerPanel secHubServerPanel = SecHubServerPanel.getInstance();
        SecHubAccess secHubAccess = SecHubAccess.getInstance();
        secHubServerPanel.update(state.serverURL, secHubAccess.isSecHubServerAlive());
    }
}