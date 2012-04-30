package org.technbolts.jbehave.eclipse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;
import org.technbolts.jbehave.eclipse.console.JBehaveConsoleAppender;
import org.technbolts.jbehave.eclipse.editors.story.completion.StoryContextType;
import org.technbolts.util.ProcessGroup;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "technbolts-jbehave-eclipse-plugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

    private ContributionContextTypeRegistry fContextTypeRegistry;

    private ContributionTemplateStore fStore;

    private String version;

    private boolean replaceFileBasedAppenders = false;
    
    /** Key to store custom templates. */
    private static final String CUSTOM_TEMPLATES_KEY = "org.technbolts.jbehave.customtemplates"; //$NON-NLS-1$
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Bundle bundle = context.getBundle();
		version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		plugin = this;
		initLogger();
	}
	
    private void initLogger () {
	    String logFile = getStateLocation().append("plugin.log").toOSString();
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();

	    RollingFileAppender<ILoggingEvent> rfAppender = rollingFileLog(logFile, loggerContext, encoder);
	    
	    // ~~ console
	    JBehaveConsoleAppender clAppender = new JBehaveConsoleAppender();
	    clAppender.setEncoder(encoder);
	    clAppender.start();

	    // attach the rolling file appender to the logger of your choice
	    Logger logbackLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
	    if(replaceFileBasedAppenders)
	        logbackLogger.detachAndStopAllAppenders();
	    logbackLogger.addAppender(rfAppender);
	    logbackLogger.addAppender(clAppender);
        
	    
	    logInfo("Log file at " + logFile);
	}

    protected RollingFileAppender<ILoggingEvent> rollingFileLog(String logFile, LoggerContext loggerContext,
            PatternLayoutEncoder encoder) {
        RollingFileAppender<ILoggingEvent> rfAppender = new RollingFileAppender<ILoggingEvent>();
	    rfAppender.setContext(loggerContext);
        rfAppender.setFile(logFile);
	    FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
	    rollingPolicy.setContext(loggerContext);
	    // rolling policies need to know their parent
	    // it's one of the rare cases, where a sub-component knows about its parent
	    rollingPolicy.setParent(rfAppender);
	    rollingPolicy.setFileNamePattern("plugin.%i.log.zip");
	    rollingPolicy.start();

	    SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
	    triggeringPolicy.setMaxFileSize("5MB");
	    triggeringPolicy.start();

	    rfAppender.setEncoder(encoder);
	    rfAppender.setRollingPolicy(rollingPolicy);
	    rfAppender.setTriggeringPolicy(triggeringPolicy);

	    rfAppender.start();
        return rfAppender;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
	    super.initializeImageRegistry(registry);
	    registry.put(ImageIds.STEP_GIVEN, getImageDescriptor("icons/bdd-g-blue.png"));
        registry.put(ImageIds.STEP_WHEN,  getImageDescriptor("icons/bdd-w-orange.png"));
        registry.put(ImageIds.STEP_THEN,  getImageDescriptor("icons/bdd-t-green.png"));
        //
        registry.put(ImageIds.NARRATIVE,  getImageDescriptor("icons/bdd-n-darkred.png"));
        registry.put(ImageIds.SCENARIO,  getImageDescriptor("icons/bdd-s-darkpink.png"));
        registry.put(ImageIds.EXAMPLE_TABLE,  getImageDescriptor("icons/bdd-e-turquoise.png"));
        //
        registry.put(ImageIds.FORBIDDEN_OVERLAY, getImageDescriptor("icons/error_ovr16.gif"));
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
	    return super.getPreferenceStore();
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public TemplateStore getTemplateStore() {
        // this is to avoid recursive call when fContextTypeRegistry is null
        getContextTypeRegistry();
        if (fStore == null) {
            fStore = new ContributionTemplateStore(
                    getContextTypeRegistry(), getPreferenceStore(),
                    CUSTOM_TEMPLATES_KEY);
            try {
                fStore.load();
            } catch (final IOException e) {
                getLog().log(
                        new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            }
        }
        return fStore;
    }
	
	public ContextTypeRegistry getContextTypeRegistry() {
        if (fContextTypeRegistry == null) {
            // create an configure the contexts available in the template editor
            fContextTypeRegistry = new ContributionContextTypeRegistry();
            fContextTypeRegistry.addContextType(StoryContextType.STORY_CONTEXT_TYPE_ID);
        }
        return fContextTypeRegistry;
    }
	
	private ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
	    private ThreadGroup group = new ThreadGroup("AsyncExecutor") {
	        @Override
	        public void uncaughtException(Thread t, Throwable e) {
	            logError("Uncaught exception in asynchronous executor", e);
	            super.uncaughtException(t, e);
	        }
	    };
	    @Override
	    public Thread newThread(Runnable r) {
	        Thread thr = new Thread (group, r);
	        return thr;
	    }
	});
    public <T> ProcessGroup<T> newProcessGroup () {
        return new ProcessGroup<T>(executor);
    }
	
    public static void logInfo(String message) {
        getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
    }

    public static void logError(String message, Throwable e) {
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, message, e));        
    }

    public String getVersion() {
        return version;
    }
}
