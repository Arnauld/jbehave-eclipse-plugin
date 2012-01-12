JBehave Eclipse plugin
=======================

**In progree** see [presentation](http://arnauld.github.com/jbehave-eclipse-plugin/)

Misc:

* Custom and internal buildin Story parser

Story Editor:

* Story syntax highlighting
* Step hyperlink detector and implementation jump
* Basic step auto-completion
* Story's steps validation:
  * Detects unimplemented steps, ie invalid step syntax
  * Detects ambiguous steps, ie entry that is match by several implementation

Preference page:

* Story syntax coloring settings

Daughter 1st Birthday - Release notes 1.0.5.SNAPSHOT
----------------------------------------------------

* Fix and unify code for **jump to declaration** behavior (mouse and keybord shortcut use the same code, supports multiline steps and should not be anymore sensible to trailing newlines) [Brathax's followup on Issue 6](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/6#issuecomment-3395767).
* Complete refactoring of the Java Scanner [Issue 15](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/15)
  * **All the classpath is now scanned**: source folders and libraries
  * Classes and packages scanned can be **filtered** to reduce overhead: a **new preference page** has been added to configure the filters. Filters can be setup globally through *preferences*, or by project through the *project properties*.
  * Step cache is now fully operational and not anymore recalculated each time: rebuild is triggered on JDT change, and the cache sub-hierarchy is only recalculated if required.
* Plugin has now a dedicated log file [Issue 16](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/16) available at `<workspace>/.metadata/.plugins/technbolts-jbehave-eclipse-plugin/plugin.log`. `logback.xml` in plugin jar file is used to configure logger (whereas appenders are removed and configured programatically).



Thanks to [brathax](https://github.com/brathax) for his help in solving [Issue 6](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/6#issuecomment-3395767).

Christmas 2011 & Happy new year 2012 - Release notes (1.0.4)
------------------------------------------------------------

* Fix and **improve completion**:
  * Fix several issue on completion
  * Completion supports step with parameters, even if parameter values are already written ([Issue 4](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/4))
  * Add **template support**: is the inserted step contains variables, the eclipse template behavior is triggered, allowing to efficiently replace variable declaration with their value, and switching to the next using tab key.
  * Completion now supports the `And` step keyword too.
* Smarter step editor: should be able to **detect parameter values and parameter variables** within a step and apply corresponding syntax highlighting ([Issue 5](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/5)).
* **Quick search** (Ctrl+J) for an quick popup display ([Issue 7](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/7)) that allows to searched for steps. Step's type (Given, When, Then) is indicated with a corresponding icon. Focus is made on the filter where `*` can be used as special character for search. Step selected in then inserted as a new line at the carret position.
* Jump to declaration (ctrl+mouse click) is also bind on keyboard using (Ctrl+G). Furthermore it now detect multiline steps ([Issue 1](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/1))
* *Quick outline* (Ctrl+O) can be used to quickly navigate within big stories with several scenario ([Issue 8](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/8))
* Add validation for narrative keywords: check for uniqueness and presence of all keywords.
* Add preferences page to modify the editor color settings ([Issue 12](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/12))


Download
========================

* [1.0.0.SNAPSHOT](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.0.SNAPSHOT.jar)
* [1.0.3.SNAPSHOT](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.3.SNAPSHOT.jar)
* [1.0.4](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.4.jar)

Installation
------------

Simply drop the jar in your eclipse `plugins/` directory and restart your ide.

Inspiration
========================

* [JBEHAVE-233](http://jira.codehaus.org/browse/JBEHAVE-233)
* [GivWenZen](https://bitbucket.org/szczepiq/givwenzenclipse/wiki/Home)
* [Building an Eclipse Text Editor with JFace Text](http://www.realsolve.co.uk/site/tech/jface-text.php)
* [Eclipse Plug-ins, Third Edition](http://www.amazon.com/Eclipse-Plug-ins-3rd-Eric-Clayberg/dp/0321553462/ref=sr_1_1?ie=UTF8&s=books&qid=1300059405&sr=8-1)

Erlide for plugin usage and template proposal behavior

* [Erlang IDE ](https://github.com/erlide/erlide)