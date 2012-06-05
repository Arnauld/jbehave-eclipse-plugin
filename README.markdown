JBehave Eclipse plugin
=======================

**In progress** see [presentation](http://arnauld.github.com/jbehave-eclipse-plugin/)

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
* Localized Keyword support
* Console (and logger level) settings

Download
========================

* [1.0.6](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.6.jar)
* [1.0.5](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.5.jar)
* [1.0.4](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.4.jar)

Previous (for posterity)

* [1.0.3.SNAPSHOT](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.3.SNAPSHOT.jar)
* [1.0.0.SNAPSHOT](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.0.SNAPSHOT.jar)

Installation
------------

Simply drop the jar in your eclipse `plugins/` directory and restart your ide.


RELEASE NOTES
=============

[Neighbours' Day](http://fr.wikipedia.org/wiki/F%C3%AAte_des_voisins) - Release notes (1.0.7)
-----------------------------------------------------------------------------------------------------------

* Thanks to [dschneller](https://github.com/dschneller) for initiating the development of the localized keywords support ([Issue 3](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/3)) 
 * A per project preference is available to set the locale that should be used.
* Add JBehave Console to trace what happens under the hood ([Issue 45](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/45)). A new preference page allows to define level per logger (similar to logback and log4j settings)
* Support shortcut for (un-)commenting selection ([Issue 44](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/44))
* Various fixes:
  * Plugin fails to disambiguate steps matching different kind of steps [Issue 41](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/41)
  * See here for more []()

Valentine's Day - Release notes (1.0.6)
------------------------------------------------

Download here [1.0.6](https://github.com/downloads/Arnauld/jbehave-eclipse-plugin/technbolts-jbehave-eclipse-plugin_1.0.6.jar)

* Thanks to [dschneller](https://github.com/dschneller) priority is now taken into account to disambiguate steps during validation phase ([Issue 27](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/27)) and on aliases too ([Issue 33](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/33)).
* Outline view is available and story is displayed as tree ([Issue 22](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/22) and [Issue 8](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/8)), double click allows a quick jump into the corresponding part in the story.
* Syntax highlighting has been enhanced to support **meta** properties. **Comments** should now be highlighted within an `ExampleTable`. ([Issue 21](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/21))
* Validation of the story's structure has been relaxed to allow meta definition before Narratives ([Issue 20](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/20))
* Several enhancements and fixes see [here](https://github.com/Arnauld/jbehave-eclipse-plugin/issues?milestone=3&sort=created&direction=desc&state=closed) for a more complete list.
* Error markers enhanced to display tooltip, Ambiguous cases also list the all matching steps


Thanks to [dschneller](https://github.com/dschneller) for creating issues, providing fixes and contributing to the project!!


Daughter 1st Birthday - Release notes (1.0.5)
---------------------------------------------

* Fix and unify code for **jump to declaration** behavior (mouse and keybord shortcut use the same code, supports multiline steps and should not be anymore sensible to trailing newlines) [Brathax's followup on Issue 6](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/6#issuecomment-3395767).
* Complete refactoring of the Java Scanner [Issue 15](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/15)
  * **All the classpath is now scanned**: source folders and libraries
  * Classes and packages scanned can be **filtered** to reduce overhead: a **new preference page** has been added to configure the filters. Filters can be setup globally through *preferences*, or by project through the *project properties*.
  * Step cache is now fully operational and not anymore recalculated each time: rebuild is triggered on JDT change, and the cache sub-hierarchy is only recalculated if required (see implementation notes [here](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/15#issuecomment-3478376))
* Plugin has now a dedicated log file [Issue 16](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/16) available at `<workspace>/.metadata/.plugins/technbolts-jbehave-eclipse-plugin/plugin.log`. `logback.xml` in plugin jar file is used to configure logger (whereas appenders are removed and configured programatically).
* Integrate step variants feature by [dschneller](https://github.com/dschneller) [Issue 18](https://github.com/Arnauld/jbehave-eclipse-plugin/pull/18). **Your project requires to use a suitable version of jbehave in order for the steps to be recognize at runtime too**. `@When("$A {+|plus|is added to} $B")` would then fits
  * `When 3 + 4`
  * `When 3 plus 4`
  * `When 3 is added to 4`

Thanks to [brathax](https://github.com/brathax) for his help in solving [Issue 6](https://github.com/Arnauld/jbehave-eclipse-plugin/issues/6#issuecomment-3395767).

Thanks to [dschneller](https://github.com/dschneller) for his integration of the [step variants](http://jira.codehaus.org/browse/JBEHAVE-702?focusedCommentId=288852&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-288852) in both [jbehave-core](https://github.com/jbehave/jbehave-core) and this project.


Note: the embedded `jbehave-core` library is build from the [jbehave-core](https://github.com/jbehave/jbehave-core) master branch (commit 3bf29212b6). Since this library is used only by the plugin itself, and not the project that uses the plugin, this should have no impact on projects.


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


Inspiration
========================

* [JBEHAVE-233](http://jira.codehaus.org/browse/JBEHAVE-233)
* [GivWenZen](https://bitbucket.org/szczepiq/givwenzenclipse/wiki/Home)
* [Building an Eclipse Text Editor with JFace Text](http://www.realsolve.co.uk/site/tech/jface-text.php)
* [Eclipse Plug-ins, Third Edition](http://www.amazon.com/Eclipse-Plug-ins-3rd-Eric-Clayberg/dp/0321553462/ref=sr_1_1?ie=UTF8&s=books&qid=1300059405&sr=8-1)

Erlide for plugin usage and template proposal behavior

* [Erlang IDE ](https://github.com/erlide/erlide)