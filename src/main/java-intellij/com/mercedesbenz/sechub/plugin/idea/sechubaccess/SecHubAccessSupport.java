package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.intellij.credentialStore.Credentials;
import com.mercedesbenz.sechub.sdk.settings.AppSettings;
import com.mercedesbenz.sechub.sdk.settings.AppSettingsCredentialsSupport;

import java.util.Objects;

public class SecHubAccessSupport {
    public void setSecHubAccessComponents() {
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
