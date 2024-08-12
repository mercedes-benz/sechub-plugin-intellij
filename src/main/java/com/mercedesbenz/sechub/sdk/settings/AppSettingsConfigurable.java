package com.mercedesbenz.sechub.sdk.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.options.Configurable;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubServerPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/*
 * Provides controller functionality for application settings.
 */
final class AppSettingsConfigurable implements Configurable {

    private final String sechubCredentialsKey = "SECHUB_CREDENTIALS";
    private AppSettingsComponent appSettingsComponent;

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
        return appSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        String currentPassword = "";
        String userName = "";

        Credentials credentials = retrieveCredentials();
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
        // Updating the server URL in the SecHubServerPanel
        SecHubServerPanel secHubServerPanel = SecHubServerPanel.getInstance();
        secHubServerPanel.update(state.serverURL);

        CredentialAttributes attributes = createCredentialAttributes();
        Credentials credentials = new Credentials(appSettingsComponent.getUserNameText(), appSettingsComponent.getApiTokenPassword());

        PasswordSafe.getInstance().set(attributes, credentials);
    }

    @Override
    public void reset() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        appSettingsComponent.setServerUrlText(state.serverURL);

        Credentials credentials = retrieveCredentials();

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

    private Credentials retrieveCredentials() {
        CredentialAttributes attributes = createCredentialAttributes();
        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        return passwordSafe.get(attributes);
    }

    private CredentialAttributes createCredentialAttributes() {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("SecHub", sechubCredentialsKey));
    }
}