package org.technbolts.eclipse.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Status;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.util.New;

public class LogWriter {
    private static LogWriter logWriter;
    public synchronized static LogWriter getWriter () {
        if(logWriter==null) {
            logWriter = new LogWriter();
            logWriter.start();
        }
        return logWriter;
    }
    
    
    private final LinkedBlockingQueue<LogData> asyncQueue = new LinkedBlockingQueue<LogData>();
    public void publish(LogData data) {
        asyncQueue.add(data);
    }
    
    private void processQueue() {
        List<LogData> buffer = New.arrayList();
        while(true) {
            try {
                takeAndDrainTo(buffer);
                while(!buffer.isEmpty()) {
                    LogData data = buffer.remove(0);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Callback cb = callbackFor(out);
                    cb.op(data);
                    iterateAndRemoveForFile(data.file, buffer, cb);
                    
                    data.file.appendContents(new ByteArrayInputStream(out.toByteArray()), IFile.FORCE, null);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Activator.getDefault().getLog().log(
                        new Status(Status.ERROR, Activator.PLUGIN_ID, "Oups!", e));
            } 
        }
    }
    
    private static final byte[] LF = "\n".getBytes();
    
    private static Callback callbackFor(final ByteArrayOutputStream out) {
        return new Callback() {
            @Override
            public void op(LogData data) throws IOException {
                out.write(data.formattedMessage().getBytes());
                out.write(LF);
            }
        };
    }
    
    private static void iterateAndRemoveForFile(IFile file, List<LogData> buffer, Callback cb) throws IOException {
        Iterator<LogData> it = buffer.iterator();
        while(it.hasNext()) {
            LogData data = it.next();
            if(data.file.equals(file)) {
                cb.op(data);
                it.remove();
            }
        }
    }
    
    public interface Callback {
        void op(LogData data) throws IOException;
    }
    
    /**
     * This is a blocking method that waits until Msg are availables and then
     * drain all it can to the list.
     * @param drained
     * @throws InterruptedException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void takeAndDrainTo(List<LogData> buffer) throws InterruptedException {
        buffer.clear();
        LogData m  = asyncQueue.take();
        buffer.add(m);
        asyncQueue.drainTo((Collection) buffer);
    }
    
    public void start() {
        Executors.newFixedThreadPool(1).submit(new Runnable() {
            @Override
            public void run() {
                processQueue();
            }
        });
    }
}
