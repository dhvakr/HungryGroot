# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.5.0] - 2023-08-09

### Added:
- Added some bugs and done several improvements to the application
- Open Sourced the programme for demonstration purposes
- Version was skipped directly to 4.5.0 - on personal choice for demo-purpose

## [2.0.1] - 2023-05-21

### Added:
- Setup scheduling to clean-up past 4 days count data
- Added Enter key shortcut on signUp page

### Changes
- Changed count visibility automatically in grid by meal preference checkbox

### Fixed
- Fixed Recurring checkbox is always visible irrespective of configured time
- Fixed count not resetting when date filter is changed

## [2.0.0] - 2023-05-17

### Added:
- Added Multi support for food count (i,e) Dinner
- Added Login Layout with forgot password view with model layout
- Added Postgres Database as Mandatory, Deprecated H2

### Changed:
- Complete code-base structure revamped

### Fixed
- Fixed base support to filter everything by date picker value
- Fixed getCount by filtered date instead of current Date

> Known Issues: 
  * ForgetPassword Overlay not displaying properly with login layout overlay

## [1.0.0] - 2023-05-15

- Initial project setup of food-app that takes count based on checkbox
> Known Issues: 
  * Lacks of Account Sign-Up and ForgetPassword, Filter by date picker value in data page

