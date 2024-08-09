package org.intellij.sdk.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(
        name = "org.intellij.sdk.settings.AppSettings",
        storages = @Storage("SdkSettingsPlugin.xml")
)
public final class AppSettings
        implements PersistentStateComponent<AppSettings.State> {

    public static class State {

        @NonNls
        public String serverURL = "";
        @NonNls
        public String userName = "";
        @NonNls
        public String apiToken = "";

    }

    private State state = new State();

    public static AppSettings getInstance() {
        return ApplicationManager.getApplication()
                .getService(AppSettings.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

}