package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.intellij.credentialStore.Credentials;
import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.sdk.settings.AppSettings;
import com.mercedesbenz.sechub.sdk.settings.AppSettingsCredentialsSupport;

import java.net.URI;
import java.util.Objects;

public class SecHubAccessFactory {
    public SecHubAccess create() {
        AppSettingsCredentialsSupport appSettingsCredentialsSupport = new AppSettingsCredentialsSupport();
        Credentials credentials = appSettingsCredentialsSupport.retrieveCredentials();
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());

        SecHubAccess secHubAccess = SecHubAccess.getInstance();
        initSecHubClient(state.serverURL, credentials.getUserName(), credentials.getPasswordAsString(), true);
        return secHubAccess;
    }

    private void initSecHubClient(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {

        SecHubClient client = null;

        if (!isInputNotEmpty(secHubServerUrl, userId, apiToken)) {
            SecHubAccess.getInstance().setClient(client);
            return;
        }
        try {
            URI serverUri = URI.create(secHubServerUrl);

            /* @formatter:off */
            client = DefaultSecHubClient.builder()
                    .server(serverUri)
                    .user(userId)
                    .apiToken(apiToken)
                    .trustAll(trustAllCertificates)
                    .build();
            /* @formatter:on */

        } finally {
            SecHubAccess.getInstance().setClient(client);
        }
    }

    private boolean isInputNotEmpty(String secHubServerUrl, String userId, String apiToken) {
        return !secHubServerUrl.isBlank() && !(userId == null) && !(apiToken == null);
    }
}
