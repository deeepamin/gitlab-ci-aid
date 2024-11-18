<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# gitlab-ci-aid Changelog

## [Unreleased]
## [1.0.1]
### Fixed
- Intellij plugin verification issues fixed


## [1.0.0] - 16.11.2024
### Added
First stable release with following features:

- Auto-completion and syntax highlighting
- Local navigation support for stages, jobs, script files and include files
- Errors and Quick fix: IDE integrated warning/error for undefined stages, jobs, scripts and quick fix to create scripts on the fly
- Script Language Injection: Injection of shell language in script blocks for .sh autocompletes, suggestions and shortcuts
- Schema Support: Auto detection of Gitlab CI YAML and auto schema configuration, with descriptions, errors and suggestions for Gitlab known keywords
