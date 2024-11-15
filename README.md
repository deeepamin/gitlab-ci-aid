# Gitlab CI Aid

*The best Aid you can get in Intellij for working with Gitlab CI YAML! 🚀*

![Build](https://github.com/deeepamin/gitlab-ci-aid/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Why Gitlab CI Aid?

<!-- Plugin description -->
Do you frequently work with Gitlab CI Yaml files in Intellij and find yourself constantly using `Shift Shift` to search 
for stages, script files or included files? Simplify this repetitive process and enhance your workflow with seamless 
navigation and autocompletion—just like your regular IntelliJ experience.  With this plugin, enjoy automatic schema detection
for known GitLab keywords, reliable stage/job autocompletion, and prominent error notifications when issues are detected in scripts, 
stages, or jobs. Reduce errors and accelerate your CI/CD pipeline development.

## Key Features

* 🌈 Autocomplete & Syntax Highlighting: Write Gitlab CI YAML files with confidence. Autocompletion suggestions and multi 
  level syntax highlighting will make your code easy to navigate and work with.
* 🗺️ Local Path Resolution: Effortless navigation with one-click access to scripts, included files, job needs and more.
* 🔧 Errors and Quick fix: IDE integrated warning/error for undefined stages, jobs, scripts and quick fix to create scripts on the fly
* 📝 Script Language Injection: Injection of shell language in script blocks for .sh autocompletes, suggestions and shortcuts
* 🧩 Schema Support: Auto schema detection for Gitlab CI YAML, with descriptions, errors and suggestions on Gitlab known keywords 

## Getting Started

* **Installation**: Download the plugin
  from JetBrains Marketplace
* **Usage**: Enjoy autocomplete, syntax highlighting, and much more with Gitlab CI YAML files.

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "gitlab-ci-aid"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/deeepamin/gitlab-ci-aid/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>



## Template TODOs
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/java).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [x] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
