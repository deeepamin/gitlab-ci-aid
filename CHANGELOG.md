<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CI Aid for GitLab Changelog

## [Unreleased]

### Added
- Detailed documentation for all features and configuration options [Documentation](https://deeepamin.github.io/gitlab-ci-aid/)

## [1.12.0] - 2025-07-05

### Added

- Navigation, autocompletion and highlighting support for "dependencies"
- Support for moving allowed elements to default using "Move to Default" refactoring action
- Move job to other files using Move refactoring
- Extract keys to !reference tag using "Extract !reference" refactoring action

### Changed

- Empty API URL is allowed in remote settings

### Fixed

- Remote edit doesn't populate the content in settings page
- Literal block conversion results in error on GitLab
- Smart pointers creation deferred until PSI changes are done
- EmptyProgressIndicator exception while reading schema file
- Include file path separator handling exceptions

## [1.11.0] - 2025-06-09

### Added

- Intent action to convert folded (>) script blocks to literal (|) blocks
- Editor notification to mark files as GitLab CI Yaml files can be disabled

### Changed

- Script injection is not done in folded (>) script blocks

### Fixed

- Needs with job key sometimes doesn't resolve to correct reference
- Needs with optional or other keys except job, show jobs in auto-completion
- Undo/redo, deleting and re-adding elements lose references and highlighting

## [1.10.0] - 2025-06-01

### Added

- Support for components, templates and remote files
- Configurable caching support for remote GitLab CI Yaml files
- Inspections for GitLab CI Yaml files (Editor > Inspections > Gitlab CI inspections)

### Changed

- Multiple inputs in a single line shows documentation for each input
- Errors in yaml files can be configured in inspections
- Undefined stage in job errors will not be reported anymore

### Fixed

- Inputs with arrays show empty value in documentation

## [1.9.1] - 2025-05-29

### Fixed

- Settings page doesn't shrink with resizing
- !reference tag with only one key doesn't resolve to correct reference
- Documentation provider NPEs
- Regex pattern error on WSL file names
- Schema provider Progress Cancelled exception

## [1.9.0] - 2025-05-22

### Added

- Variables navigation and autocompletion
- !reference tag auto-completion in script blocks

### Changed

- Settings page visual improvements

### Fixed

- Auto-completion doesn't show file path for jobs
- Stage/job with quoted strings doesn't resolve to correct reference

## [1.8.0] - 2025-05-17

### Added

- __!reference__ tag navigation, autocompletion, renaming and more
- Inputs highlighting

### Changed

- Uses latest version of compatible bash support pro plugin from marketplace
- Code completions are provided using more robust implementation

### Fixed

- Renaming input or needs/extends job doesn't update the references
- Unknown input renders empty documentation popup
- Comment text in settings has small font

## [1.7.0] - 2025-05-11

### Added

- CI Aid for Gitlab Settings page under Tools for configuring the plugin
- Default Gitlab CI Yaml path can be configured in the settings
- Errors about undefined stage, job can be ignored
- User marked yaml files are configurable and plugin remembers them across restarts

## [1.6.0] - 2025-05-09

### Added

- Support for [CI/CD Inputs](https://docs.gitlab.com/ci/inputs) 
- Inputs are autocompleted and navigable
- Quick documentation for inputs 

### Fixed

- Default keywords e.g., stage, stages in inputs generates unexpected errors

## [1.5.0] - 2025-05-06

### Added

- Support shell script highlighting with BashSupport Pro in addition to the existing support for the JetBrains Shell plugin.
  CI Aid for GitLab can be installed without the JetBrains Shell or BashSupport Pro plugins. Highlighting of shell scripts
  will be available as soon as one of the plugins is enabled.
- Support for script/file reference resolve in YAML multi line block (| and >) for script tags

### Changed

- Script path detection and navigation handles more cases

### Fixed

- Stages to job reference wouldn't resolve due to cached element

## [1.4.0] - 2025-05-03

### Changed

- Under the hood YAML handling 

### Fixed

- Non included and user marked YAML files are not highlighted (Thanks @gtaylor1981 for the contribution)
- Stages to job reference couldn't be resolved sometimes
- ConcurrentModificationException raised when multiple files are processed

## [1.3.0] - 2025-05-02

Name of the plugin is changed to "CI Aid for GitLab" to comply with GitLab official trademark guidelines.
All the good features (and bugs) offered by plugin stay the same as before.

## [1.2.0] - 2025-04-30

### Changed

- Updated GitLab CI schema for inputs and other elements support

### Fixed

- Script element resolves to wrong path in project
- Ignore action doesn't remove the Editor notification panel
- Windows path separator handling and in general Path not found exceptions
- Shell plugin not found exception
- Quoted text in include path prohibits included files to not provide autosuggestions and highlighting

## [1.1.0] - 2025-04-16

### Added

- Support for IntelliJ 2025.1

## [1.0.5] - 2025-01-20

### Added

- Support for navigation, autocompletion and highlighting for "extends" keyword

### Changed

- Hidden jobs (starting with .) are highlighted and can be navigated to, not shown in autocompletion

### Fixed

- Quoted text in file path resolves in not navigating to file

## [1.0.4] - 2024-12-15

### Added

- Script injection for multiline literal (|) and folded (>) blocks
- Multiple script paths detection in script blocks

### Changed

- Improved caching for yaml parsing
- Script and include files don’t require absolute path for navigation
- Updated GitLab CI schema json

### Fixed

- Quoted text in include file name resolves correctly

## [1.0.3] - 2024-11-20

### Added

- .gitlab-ci.yml and .gitlab-ci.yaml both are considered as default files
- Improved job processing

### Fixed

- Non GitLab CI Yaml files are annotated/highlighted
- Some python script paths weren't highlighted correctly

## [1.0.2] - 2024-11-18

### Added

- Jetbrains marketplace exception analyzer

## [1.0.1] - 2024-11-18

### Fixed

- Intellij plugin verification issues fixed

## [1.0.0] - 2024-11-16

### Added

- Auto-completion and syntax highlighting
- Local navigation support for stages, jobs, script files and include files
- Errors and Quick fix: IDE integrated warning/error for undefined stages, jobs, scripts and quick fix to create scripts on the fly
- Script Language Injection: Injection of shell language in script blocks for .sh autocompletes, suggestions and shortcuts
- Schema Support: Auto detection of GitLab CI YAML and auto schema configuration, with descriptions, errors and suggestions for GitLab known keywords

[Unreleased]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.12.0...HEAD
[1.12.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.11.0...v1.12.0
[1.11.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.10.0...v1.11.0
[1.10.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.9.1...v1.10.0
[1.9.1]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.9.0...v1.9.1
[1.9.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.7.0...v1.8.0
[1.7.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.6.0...v1.7.0
[1.6.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.5...v1.1.0
[1.0.5]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/deeepamin/gitlab-ci-aid/commits/v1.0.0
