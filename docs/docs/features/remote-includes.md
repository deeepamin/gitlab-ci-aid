# Remote Includes

Remote included such as `component`, `template`, `remote` and `project` are supported by the plugin.
If the URL to the remote file needs authentication, it needs to be configured in the `Settings > Tools > CI Aid for Gitlab > Remotes` section. ()
If authentication is not required, the plugin will automatically fetch the remote file in to cache directory and provide the functionality as if it was a local file. ([Configure Remotes](../configuration/remotes#gitlab-project-configuration)).

## Component
Plugin follows official GitLab documentation for [component](https://docs.gitlab.com/ee/ci/yaml/#component) includes. 
Which means that the pre-releases are not fetched for a component, and versioning is used to fetch the specific version of the component as specified in the GitLab official documentation.

## Template
Plugin follows official GitLab documentation for [template](https://docs.gitlab.com/ee/ci/yaml/#template) includes.
For gitlab.com, the plugin will fetch the template from the GitLab templates repository [templates](https://gitlab.com/gitlab-org/gitlab/-/tree/master/lib/gitlab/ci/templates).

If you are using a self-hosted GitLab instance, you can configure the URL to the templates repository in the `Settings > Tools > CI Aid for Gitlab > Remotes`.

## Remote
GitLab doesn't support authentication for remote includes, so includes with `remote` keyword will not work if the remote file requires authentication.
