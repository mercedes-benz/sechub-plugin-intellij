// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.action;
import com.mercedesbenz.sechub.SecHubReportImporter;
import com.mercedesbenz.sechub.compatiblity.VirtualFileCompatibilityLayer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class SechubImportAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(SechubImportAction.class);

    @Override
    public void update(AnActionEvent event) {
        // Using the event, evaluate the context, and enable or disable the action.
        // Set the availability based on whether a project is open
        Project project = event.getProject();
        event.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project currentProject = event.getProject();
        @Nullable VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor("json"), currentProject, null);
        if (file==null){
            return;
        }
        @NotNull Path p = VirtualFileCompatibilityLayer.toNioPath(file);
        try {
            SecHubReportImporter.getInstance().importAndDisplayReport(p.toFile());
        } catch (IOException e) {
            LOG.error("Failed to import "+p,e);
        }

    }

}
