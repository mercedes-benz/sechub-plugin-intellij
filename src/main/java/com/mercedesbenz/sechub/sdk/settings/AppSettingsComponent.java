package com.mercedesbenz.sechub.sdk.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AppSettingsComponent {

    private final JPanel mainPanel;
    private final JBTextField userNameText = new JBTextField();
    private final JBTextField serverUrlText = new JBTextField();
    private final JBPasswordField apiTokenPassword = new JBPasswordField();

    public AppSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Server URL:"), serverUrlText, 1, false)
                .addLabeledComponent(new JBLabel("User name:"), userNameText, 1, false)
                .addLabeledComponent(new JBLabel("API token:"), apiTokenPassword, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return userNameText;
    }

    @NotNull
    public String getUserNameText() {
        return userNameText.getText();
    }

    @NotNull
    public String getServerUrlText() {
        return serverUrlText.getText();
    }

    @NotNull
    public String getApiTokenPassword() {
        return String.valueOf(apiTokenPassword.getPassword());
    }

    public void setUserNameText(@NotNull String newText) {
        userNameText.setText(newText);
    }

    public void setServerUrlText(@NotNull String newText) {
        serverUrlText.setText(newText);
    }

    public void setApiTokenPassword(@NotNull String newText) {
        apiTokenPassword.setText(newText);
    }

}
