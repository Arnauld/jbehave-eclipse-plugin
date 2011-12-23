package org.technbolts.jbehave.eclipse.editors;

import java.util.ResourceBundle;

public class EditorMessages {
    private static final String BUNDLE_FOR_CONSTRUCTED_KEYS = "org.technbolts.jbehave.eclipse.editors.ConstructedEditorMessages";//$NON-NLS-1$

    private static final ResourceBundle fgBundleForConstructedKeys = ResourceBundle
            .getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

    /**
     * Returns the message bundle which contains constructed keys.
     * 
     * @since 3.1
     * @return the message bundle
     */
    public static ResourceBundle getBundleForConstructedKeys() {
        return fgBundleForConstructedKeys;
    }
}
