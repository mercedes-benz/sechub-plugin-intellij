package org.intellij.sdk.settings;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * Provides controller functionality for application settings.
 */
final class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent appSettingsComponent;

    // A default constructor with no arguments is required because
    // this implementation is registered as an applicationConfigurable

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SecHub Configuration";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return appSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        appSettingsComponent = new AppSettingsComponent();
        return appSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettings.State state =
                Objects.requireNonNull(AppSettings.getInstance().getState());
        String currentPassword = "";
        try {
            currentPassword = PasswordSafe.getInstance().getPassword(null, AppSettings.class, "PASSWORD_KEY");
        } catch (PasswordSafeException e) {
            e.printStackTrace();
        }
        return !appSettingsComponent.getUserNameText().equals(state.userName) ||
                appSettingsComponent.getApiTokenText().equals(StringUtil.notNullize(currentPassword)) ||
                appSettingsComponent.getServerUrlText().equals(state.serverURL);
    }

    @Override
    public void apply() {
        AppSettings.State state =
                Objects.requireNonNull(AppSettings.getInstance().getState());
        state.userName = appSettingsComponent.getUserNameText();
        state.serverURL = appSettingsComponent.getServerUrlText();
        try {
            PasswordSafe.getInstance().storePassword(null, AppSettings.class, "PASSWORD_KEY", appSettingsComponent.getApiTokenText());
        } catch (PasswordSafeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        AppSettings.State state =
                Objects.requireNonNull(AppSettings.getInstance().getState());
        appSettingsComponent.setUserNameText(state.userName);
        appSettingsComponent.setServerUrlText(state.serverURL);
        String currentPassword = "";
        try {
            currentPassword = PasswordSafe.getInstance().getPassword(null, AppSettings.class, "PASSWORD_KEY");
        } catch (PasswordSafeException e) {
            e.printStackTrace();
        }
        appSettingsComponent.setApiTokenText(StringUtil.notNullize(currentPassword));

    }

    @Override
    public void disposeUIResources() {
        appSettingsComponent = null;
    }

}