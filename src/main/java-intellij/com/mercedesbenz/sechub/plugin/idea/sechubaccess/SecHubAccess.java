package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class SecHubAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubAccess.class);
    private static SecHubAccess INSTANCE;
    private String secHubServerUrl;
    private boolean trustAllCertificates;
    private String userId;
    private String apiToken;
    private SecHubClient client;

    private SecHubAccess() {
    }

    public static SecHubAccess getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SecHubAccess();
        }

        return INSTANCE;
    }

    public void setSecHubServerUrl(String secHubServerUrl) {
        this.secHubServerUrl = secHubServerUrl;
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public void initSecHubClient() {

        URI serverUri = URI.create(secHubServerUrl);

        this.client = DefaultSecHubClient.builder()
                .server(serverUri)
                .user(userId)
                .apiToken(apiToken)
                .trustAll(trustAllCertificates)
                .build();
    }

    public boolean isSecHubServerAlive() {
        if (client == null) {
            LOG.error("SecHub client is not initialized");
            return false;
        }
        try {
            return client.isServerAlive();
        } catch (SecHubClientException e) {
            return false;
        }
    }
}
