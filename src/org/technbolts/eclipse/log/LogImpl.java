package org.technbolts.eclipse.log;

import org.eclipse.core.resources.IFile;

public class LogImpl implements Log {

    private IFile file;
    public LogImpl(IFile file) {
        this.file = file;
    }

    public void logInfo(String message) {
        LogWriter.getWriter().publish(new LogData(file, message));
    }
}
