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
    private SecHubClient client;

    private SecHubAccess() {
    }

    public static SecHubAccess getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SecHubAccess();
        }

        return INSTANCE;
    }

    public void setClient(SecHubClient client) {
        this.client = client;
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
}
