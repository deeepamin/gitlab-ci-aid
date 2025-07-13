# Remotes

## GitLab Project Configuration

In order to use remote includes such as `component`, `template`, and `project`, you need to configure the remotes in the plugin settings. This is necessary if the URL to the remote file requires authentication.

User can access the remotes configuration dialog from `Settings > Tools > CI Aid for Gitlab > Remotes`. Here you can add, edit, or remove remotes. The remotes are used to fetch the remote files and provide the functionality as if they were local files.
User can add a remote with `+` button, edit the remote with `Edit` button, or remove the remote with `-` button. The remotes can be configured with the following fields: 

- **GitLab API URL**: The API URL of the GitLab instance. For example, `https://gitlab.com/api/v4/` for GitLab.com or `https://gitlab.example.com/api/v4/` for a self-hosted GitLab instance.
- **Group/Project**: the project path in GitLab. For example, if your project URL is `https://gitlab.com/group/project`, then specify /group/project.
- **Access Token**: The personal access token with `api` scope to access the GitLab API. This is required if the remote file requires authentication. You can create a personal access token in your GitLab profile settings under "Access Tokens". If you have a group token specifying just the group e.g., group or group/project both will work for all the projects under the group.

![img/remotes-dialog.png](img/remotes-dialog.png)

## Templates
If you are using a self-hosted GitLab instance, you can configure the project and path to the templates repository in the remotes settings. The plugin will fetch the template from the specified URL.
For example, if your templates repository is located at `https://gitlab.example.com/gitlab/-/tree/master/lib/ci/templates`, you can specify the group as `gitlab` and project as `lib/ci/templates` in the remotes settings.

## Caching
The plugin caches the remote files in the IntelliJ cache director. This is done to avoid fetching the remote files every time they are needed. The cache is updated when it expires, the expiration time is set to 24 hours by default. You can change the cache expiration time in the plugin settings.
If you don't want to use the remote includes or in general not use caching, you can disable it in the plugin settings using `Enable components, templates and remote files caching` checkbox. This will disable the caching of remote files totally, it will not remove existing cached files, if previously cached.
To remove the cached files, you can use the `Clear cache` button in the remotes settings dialog. This will remove all the cached files, and they will be fetched again when needed.

![img/remotes-settings.png](img/remotes-settings.png)
