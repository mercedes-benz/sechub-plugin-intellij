// SPDX-License-Identifier: MIT
package com.daimler.sechub;

import com.intellij.openapi.diagnostic.Logger;
import java.util.UUID;

import com.daimler.sechub.window.SecHubToolWindow;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.model.FindingModel;

public class SecHubReportViewUpdater {

	private static final Logger LOG = Logger.getInstance(SecHubReportViewUpdater.class);

	public void updateReportViewInSWTThread(UUID jobUUID, TrafficLight trafficLight, FindingModel model) {

		ProgressManager.getInstance().executeProcessUnderProgress(()-> internalUpdateReportView(jobUUID, trafficLight, model), ProgressIndicatorProvider.getGlobalProgressIndicator());
	}

	private void internalUpdateReportView(UUID jobUUID, TrafficLight trafficLight, FindingModel model) {
		SecHubToolWindow sechubToolWindow = SecHubToolWindow.getInstance();
		if (sechubToolWindow==null){
			LOG.error("Did not found sechub tool window!");
			return;
		}
		sechubToolWindow.update(model);
	}
}
