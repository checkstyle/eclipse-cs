## What's this?

The .target file describes the minimum eclipse environment that eclipse-cs runs in.

## How to use the target platform

In the Maven build it is used automatically for compilation
(see target-platform-configuration section in the parent POM).

For development in Eclipse you have to load it manually, but only one time:
Open the .target file and use the "Set As Active Target" hyperlink. This may take
a while the first time, since plugins are loaded from the Internet.

If you don't do that, the compilation happens against the eclipse plugins of 
your IDE and you may call methods that don't exist in earlier versions, which
will lead to runtime errors for end users.

## How to update the target platform

Target files are hard to maintain manually. Therefore we use a custom DSL to
maintain the target platform, and to derive the .target file.

Install https://github.com/eclipse-cbi/targetplatform-dsl to edit the .tpd
file. After saving use the context menu of the .tpd file to create a new
version of the .target file.

Both the .target and .tpd files must be checked in after changes.