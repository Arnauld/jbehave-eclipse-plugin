package org.technbolts.jbehave.eclipse.console;

import ch.qos.logback.classic.spi.ILoggingEvent;


public class JBehaveConsoleAppender extends ch.qos.logback.core.OutputStreamAppender<ILoggingEvent> {
    
    @Override
    public void start() {
        JBehaveConsole console = JBehaveConsole.findConsole();
        setOutputStream(console.getOutMessageStream());
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
    }
    
}
