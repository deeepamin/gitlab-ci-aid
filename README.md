
<div align="center">
    <a href="https://plugins.jetbrains.com/plugin/25859-gitlab-ci-aid">
        <img src="./src/main/resources/META-INF/pluginIcon.svg" width="200" height="200" alt="logo"/>
    </a>
</div>
<h1 align="center">Gitlab CI Aid</h1>
<p align="center">The best aid you can get in Intellij for working with Gitlab CI YAML! 🚀</p>

<p align="center">
<a href="https://actions-badge.atrox.dev/deeepamin/gitlab-ci-aid/goto?ref=main"><img alt="Build Status" src="https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fdeeepamin%2Fgitlab-ci-aid%2Fbadge%3Fref%3Dmain&style=flat" /></a>
<a href="https://plugins.jetbrains.com/plugin/25859-gitlab-ci-aid"><img src="https://img.shields.io/jetbrains/plugin/v/25859-gitlab-ci-aid.svg?style=flat-square" alt="version"></a>
<a href="https://plugins.jetbrains.com/plugin/25859-gitlab-ci-aid"><img src="https://img.shields.io/jetbrains/plugin/d/25859-gitlab-ci-aid.svg?style=flat-square" alt="downloads"></a>
</p>
<br>

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

## Change log

Please see [CHANGELOG](CHANGELOG.md) for more information on what has changed recently.

## Limitations / Future Improvements
* Only works with default Gitlab CI file name (.gitlab-ci.yml/yaml), Gitlab supports name change but no API for getting the name
* Only works with local files currently, no components, remote files or templates

## Known Issues

## Notes
* Some features may not work while IntelliJ is indexing the project
* If there's an issue with some feature, reopening the file should help 

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
