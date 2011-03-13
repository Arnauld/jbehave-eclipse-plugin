package org.technbolts.eclipse.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class UIUtils {
    public static void show(String title, String message) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageDialog.openInformation(shell, title, message);
    }
    public static void warn(String title, String message) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageDialog.openWarning(shell, title, message);
    }
}
