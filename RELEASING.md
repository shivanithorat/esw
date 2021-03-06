# Releasing

## Prerequisites (This is already configured in `release.yml`)

* Git authentication works by running cmd: `ssh -vT git@github.com`
* Node is installed
* npm module `junit-merge` is installed (for merging multiple xml test reports into one)
* npm module `junit-viewer` is installed (for generating html test report from merged xml)

## Steps to release

### Release Dependent Repos

Refer RELEASING.md in `msocket`
Refer RELEASING.md in `embedded-keycloak`
Refer RELEASING.md in `sbt-docs`
Refer RELEASING.md in `kotlin-plugin`
Refer RELEASING.md in `csw`

### esw

1. Create a branch named `branch-<major>.<minor>.x` if not already exists from `master`. Example branch name `branch-3.0.x`.
   All subsequent release for this release cycle should be done on this branch. All further steps are to be done on this branch.
1. Update release notes (`notes/<version>.markdown`)
    **Note** - The version in `notes` should be of format `v1.0.0`
1. Update top-level `CHANGELOG.md`
1. Update top-level `README.md`
1. Update CSW_VERSION in `release.yml` 
1. Add changes mention in `CHANGELOG.md` of `esw-contract` in top-level `CHANGELOG.md`
1. Add changes mention in `CHANGELOG.md` of `esw-contract` in the change section of `README.md` of `esw-contract`
1. Add changes mention in `CHANGELOG.md` of `esw-contract` in top-level `README.md`
1. Exclude projects from `build.sbt` which you do not want to release
1. Update dependent repo version in `Libs.scala`
1. Commit and push the changes to `branch-<major>.<minor>.x` branch.
1. Run `release.sh $VERSION$` script by providing version number argument (This triggers release workflow)

    **Note:** `PROD=true` environment variable needs to be set before running `release.sh`
