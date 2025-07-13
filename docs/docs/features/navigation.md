# Navigation

Using reference resolver, plugin can navigate to the definition of various elements in the GitLab CI YAML files.
User can use `(Ctrl + Click)` or `(Cmd + Click)` on the element to navigate to its definition in the same file or in another file.

The navigation is available for the following elements:

# include
User can navigate to the included file from `include:` keyword, the plugin will open the file in the editor. If remote includes are used, configure them for the using with plugin. ([Remote Includes](./remote-includes#remote-includes)).

# local files

User can navigate to the local file defined in `script`, `before_script`, `after_script` sections, the plugin will open the file in the editor.

# dependencies
User can navigate to the dependent job from `dependencies:` keyword.

# extends
User can navigate to the job from `extends:` keyword.

# needs
User can navigate to the needing job from `needs:` keyword.

# inputs
User can navigate to the input declaration (spec:input -> variable) from `$[[ inputs.variable ]]` keyword.

# stage
User can navigate to the `stages` declaration from `stage:` keyword.

# stages
User can navigate to the `stage` declaration of job from `stages:` keyword. If multiple jobs have the same stage, the plugin will show a pop-up with all the jobs with their file names.

# variables
User can navigate to the variable declaration from `$` or `${` in the `script`, `before_script` and `after_script` sections. If same name variable is declared in multiple files, the plugin will show a pop-up with "Declared in multiple files".

# !reference
User can navigate to the `!reference` declaration from `!reference` keyword.
