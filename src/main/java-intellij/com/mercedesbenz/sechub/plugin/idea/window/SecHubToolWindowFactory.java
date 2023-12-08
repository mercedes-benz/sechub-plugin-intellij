// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class SecHubToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();

        SecHubToolWindow secHubToolWindow = new SecHubToolWindow(toolWindow);
        SecHubToolWindow.registerInstance(secHubToolWindow);
        Content content = contentFactory.createContent(secHubToolWindow.getContent(), "SecHub", false);

        toolWindow.getContentManager().addContent(content);
    }
}
