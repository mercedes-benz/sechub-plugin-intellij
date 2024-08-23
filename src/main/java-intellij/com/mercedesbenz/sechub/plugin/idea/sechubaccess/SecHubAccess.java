// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class SecHubAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubAccess.class);
    private SecHubClient client;
    public SecHubAccess(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {
        initSecHubClient(secHubServerUrl, userId, apiToken, trustAllCertificates);
    }

    public boolean isSecHubServerAlive() {
        if (client == null) {
            LOG.debug("SecHub client is not initialized");
            return false;
        }
        try {
            return client.isServerAlive();
        } catch (SecHubClientException e) {
            return false;
        }
    }

    private void initSecHubClient(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {

        SecHubClient client = null;

        if (isInputMissingOrEmpty(secHubServerUrl, userId, apiToken)) {
            this.client = client;
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
            this.client = client;
        }
    }

    private boolean isInputMissingOrEmpty(String secHubServerUrl, String userId, String apiToken) {
        return secHubServerUrl.isBlank() || userId == null || apiToken == null;
    }
}
