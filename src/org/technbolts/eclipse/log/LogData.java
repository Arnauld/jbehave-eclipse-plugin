package org.technbolts.eclipse.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;

public class LogData {
    private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss-SSS");
    
    public final IFile file;
    public final String message;
    public final long when;
    
    public LogData(IFile file, String message) {
        this.file = file;
        this.message = message;
        this.when = System.currentTimeMillis();
    }
    
    public String formattedMessage() {
        return df.format(new Date(when)) + " " + message;
    }
}
