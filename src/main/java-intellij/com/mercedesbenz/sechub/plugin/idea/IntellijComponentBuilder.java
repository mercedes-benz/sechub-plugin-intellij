package com.mercedesbenz.sechub.plugin.idea;

import com.intellij.ui.components.JBScrollPane;
import com.mercedesbenz.sechub.plugin.ui.ComponentBuilder;

import javax.swing.*;

public class IntellijComponentBuilder implements ComponentBuilder {
    @Override
    public JScrollPane createScrollPane(JComponent component) {
        return new JBScrollPane(component);
    }
}
