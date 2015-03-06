package org.technbolts.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.FileLocator;

/**
 * The Class ScannerForKeywordsFiles.
 * Scan defined path for keywords files
 * usually placed in i18n. 
 * 
 * @author johhy
 */

public class ScannerForKeywordsFiles {

	   /**
   	 * Gets the keywords files from path.
   	 *
   	 * @param path to search keywords files
   	 * @return the keywords files from path
   	 * @throws Exception the exception
   	 */
   	private static String[] getKeywordsFilesFromPath(String path) throws Exception {
	    	Enumeration<URL> urls = Thread.currentThread()
	    			.getContextClassLoader().getResources(path);
	    	Set<String> allFiles = new HashSet<String>(); 
	    	while(urls.hasMoreElements()) {
	    		URL a = urls.nextElement();
	    		URL dirURL = FileLocator.resolve(a);
	    		if (dirURL != null && dirURL.getProtocol().equals("file")) {
	    			for(String s:new File(dirURL.toURI()).list()) {
	    				allFiles.add(s);
	    			}
	    		} 

	    		if (dirURL.getProtocol().equals("jar")) {
	    			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); 
	    			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	    			Enumeration<JarEntry> entries = jar.entries(); 
	    			Set<String> result = new HashSet<String>();
	    			while(entries.hasMoreElements()) {
	    				String name = entries.nextElement().getName();
	    				if (name.startsWith(path)) { //filter according to the path
	    					String entry = name.substring(path.length());
	    					if(!entry.equals("")) {
	    						result.add(entry);
	    					}
	    				}
	    			}
	    			jar.close();
	    			allFiles.addAll(result);
	    		} 
	    	}
	    	return allFiles.toArray(new String[allFiles.size()]);
	    }
	   
		/**
		 * Gets the locale names from file names.
		 *
		 * @param fileNames the file names
		 * @return the locale names from file names
		 * @throws Exception the exception
		 */
		private static String[] getLocaleNamesFromFileNames(String[] fileNames) throws Exception {
			String[] availableLocales = new String[fileNames.length];
			for(int i=0;i<fileNames.length;i++) {
				String nameLocale =  fileNames[i]
						.substring(9, fileNames[i].indexOf('.'));
				availableLocales[i] = nameLocale;
			}
			return availableLocales;
		}
		
		/**
		 * Gets the available locale names from path.
		 *
		 * @param path the path
		 * @return the available locale names from path
		 * @throws Exception the exception
		 */
		public static String[] getAvailableLocaleNamesFromPath(String path) throws Exception {
			return getLocaleNamesFromFileNames(getKeywordsFilesFromPath(path));
		}
}
