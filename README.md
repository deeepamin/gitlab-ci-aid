# Gitlab CI Aid

*The best Aid you can get in Intellij for working with Gitlab CI YAML! 🚀*

![Build](https://github.com/deeepamin/gitlab-ci-aid/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Why Gitlab CI Aid?

<!-- Plugin description -->
Do you frequently work with Gitlab CI Yaml files in Intellij and find yourself constantly using `Shift Shift` to search 
for stages, script files or included files? Gitlab CI Aid to the rescue. With this plugin, enjoy automatic Gitlab schema 
provision, navigation across elements, stage/job autocompletion, and prominent error notifications when issues are detected 
in configuration. Reduce errors and accelerate your pipeline development.

## Features

* Autocomplete & Syntax Highlighting: Write Gitlab CI YAML files with confidence. Autocompletion suggestions and multi 
  level syntax highlighting will make your code easy to navigate and work with.
* Local Path Resolution: Effortless navigation with one-click access to scripts, included files, job needs and more.
* Errors and Quick fix: IDE integrated warning/error for undefined stages, jobs, scripts and quick fix to create scripts on the fly
* Script Language Injection: Injection of shell language in script blocks for .sh autocompletes, suggestions and shortcuts
* Schema Support: Auto detection of Gitlab CI YAML and auto schema configuration, with descriptions, errors and suggestions for Gitlab known keywords 

## Getting Started

* **Installation**: Download the plugin from JetBrains Marketplace
* **Usage**: Enjoy autocomplete, syntax highlighting, and much more with Gitlab CI YAML files.

<!-- Plugin description end -->

## Limitations / Future Improvements
* Only works with default Gitlab CI file name (.gitlab-ci.yml/yaml), Gitlab supports name change but no API for getting the name
* Only works with local files currently, no components, remote files or templates
* Reference tags are shown as warning with schema (there's a way from fellow plugin dev, but requires a lot of duplication of IntelliJ YAML parser)

## Known Issues
* Newly added job is not highlighted - file has to be reopened for it to work

## Notes
* Some features may not work while IntelliJ is indexing the project
* If there's an issue with some feature, reopening the file should help 


## Template TODOs
- [ ] Get familiar with the [template documentation][template].
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
