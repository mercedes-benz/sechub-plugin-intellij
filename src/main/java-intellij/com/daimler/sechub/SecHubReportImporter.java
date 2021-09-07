// SPDX-License-Identifier: MIT
package com.daimler.sechub;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.daimler.sechub.client.java.SecHubClient;
import com.daimler.sechub.client.java.SecHubReport;
import com.daimler.sechub.client.java.SecHubReportException;
import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.model.FindingModel;
import com.daimler.sechub.model.SecHubFindingToFindingModelTransformer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;

public class SecHubReportImporter {
    private static SecHubReportImporter INSTANCE = new SecHubReportImporter();

    public static SecHubReportImporter getInstance() {
        return INSTANCE;
    }

    private SecHubFindingToFindingModelTransformer transformer = new SecHubFindingToFindingModelTransformer();
    private SecHubReportViewUpdater secHubReportViewUpdater = new SecHubReportViewUpdater();

    public void importAndDisplayReport(File reportFile) throws IOException {
        importAndDisplayReport(reportFile, ProgressIndicatorProvider.getGlobalProgressIndicator());
    }

    public void importAndDisplayReport(File reportFile, ProgressIndicator progressIndicator) throws IOException {
        if (reportFile == null) {
            throw new IOException("No report file defined");
        }
        if (!reportFile.isFile()) {
            throw new IOException("Is not a file:" + reportFile);
        }
        if (!reportFile.canRead()) {
            throw new IOException("No permissions to read the report:" + reportFile);
        }
        String absolutePath = reportFile.getAbsolutePath();
        //progressIndicator.setText("Import SecHub report from " + absolutePath);

        try {
            SecHubReport report = SecHubClient.importSecHubJsonReport(reportFile);
            if (report == null) {
                throw new IOException("Report file importer returned null");
            }
            List<SecHubFinding> secHubFindings = report.getResult().getFindings();

            FindingModel model = transformer.transform(secHubFindings);
            model.setJobUUID(report.getJobUUID());
            model.setTrafficLight(report.getTrafficLight());

            secHubReportViewUpdater.updateReportViewInAWTThread(report.getJobUUID(), report.getTrafficLight(), model);

        } catch (SecHubReportException e) {
            throw new IOException("An error occured while reading the report: " + absolutePath
                    + ". Make sure the report is an actual SecHub Report.", e);
        } catch (RuntimeException e) {
            throw new IOException("Unexpected error on import happened", e);
        }
    }


}
