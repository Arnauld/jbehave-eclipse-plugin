package org.technbolts.jbehave.eclipse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.technbolts.util.ProcessGroup;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "technbolts-jbehave-eclipse-plugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
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
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
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
}
