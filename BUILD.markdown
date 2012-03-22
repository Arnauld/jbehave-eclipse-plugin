The build process is not standard, thus this will probably change in the future if it becomes a real bottleneck.
Plugin does not (yet) provide command line to be build due to the required eclipse environement.

In short plugin dependencies can be split into three types:

* other plugin dependencies (rely on eclipse)
* external libraries scope runtime (rely on maven)
* external libraries scope test (rely on maven)

This file explains the current way to setup the project and build the plugin:

    git clone git@github.com:Arnauld/jbehave-eclipse-plugin.git

In order to simplify dependency management, the plugin use maven in a unusual way: retreive the required dependencies and copy jar into a dedicated folder `/lib`. Maven is not used to build and generate the plugin: only to retrieve the external library dependencies.

   mvn dependency:copy-dependencies

Project classpath rely on the **plugin nature** of the project: 

* Eclipse plugin dependencies managed through the plugin nature and declared in `META-INF/MANIFEST.MF`
    
    Require-Bundle: org.eclipse.ui,
     org.eclipse.core.runtime,
     org.eclipse.jface.text,
     org.eclipse.ui.editors,
     org.eclipse.jdt.core;bundle-version="3.3.1",
     ...

* External libraries dependency (retrieved through maven) from the plugin point of view are declared in: `build.properties` and `META-INF/MANIFEST.MF`. Thus both files must be updated according to modified or added dependencies. Test depdencies belongs to the project lib directory, but are not part of the `build.properties` and `META-INF/MANIFEST.MF` thus not exported within the plugin.

    Bundle-ClassPath: .,
	 lib/commons-collections-3.2.1.jar,
	 lib/commons-io-2.1.jar,
	 lib/commons-lang-2.5.jar,
	 ...


Libraries that are not part of the plugin executable (e.g. test libraries) must be manually added in the project library dependencies:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6"/>
	<classpathentry kind="con" path="org.eclipse.pde.core.requiredPlugins"/>
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="src" path="test"/>
	<classpathentry kind="src" path="samples/usecase/src/main/story"/>
	<classpathentry kind="src" path="samples/usecase/src/main/java"/>
	<classpathentry kind="lib" path="lib/hamcrest-core-1.1.jar"/>
	<classpathentry kind="lib" path="lib/testng-6.3.1.jar"/>
	<classpathentry kind="lib" path="lib/hamcrest-integration-1.1.jar"/>
	<classpathentry kind="lib" path="lib/hamcrest-library-1.1.jar"/>
	<classpathentry kind="lib" path="lib/mockito-all-1.8.4.jar"/>
	<classpathentry kind="output" path="bin"/>
</classpath>
```

# To generate the plugin

* Right click on the plugin
* Export...
* *Deployable plug-ins and fragments*
* "Next"
* Select the corresponding plugin 
* Define the wanted directory
* Click "Finish"

# To launch the plugin in an runtime workspace

* Run as... Eclipse application, configure and select required plugins
* or right click on `Eclipse Application (1).launch` Run as...
