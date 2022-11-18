## What's this?

The `.target` file describes the minimum Eclipse environment that eclipse-cs runs in.
Right now that is 2020-09 (since that is the first version officially running on Java 11).


## Preconditions for development

To use the target platform, these plugins must be installed in your Eclipse IDE:
* [Eclipse M2E](https://marketplace.eclipse.org/content/eclipse-m2e-maven-support-eclipse-ide)
* [Target Platform DSL](https://github.com/eclipse-cbi/targetplatform-dsl)


## How to use the target platform

In the Maven build it is used automatically for compilation
(see target-platform-configuration section in the parent POM).

For development in Eclipse you have to load it manually, but only after it has been modified.
Open the `net.sf.eclipsecs.target.target` file and use the "Set As Active Target" hyperlink.
This may take a while the first time, since plugins are loaded from the Internet.

If you don't do that, the compilation happens against the Eclipse plugins of your IDE
and you may call methods that don't exist in earlier versions,
which will lead to runtime errors for end users that are still on an old Eclipse version.


## How to update the target platform

Target files are hard to maintain manually.
Therefore we use a custom DSL to maintain most of the target platform, and to derive the `.target` file.
However, that DSL based generation is not possible for the part of the target platform that shall include Maven coordinates.
That's why the target platform is split over 2 files.
The root target platform contains only the Maven coordinates and can be edited manually.
It also includes a nested target file, which is generated from the target platform DSL.
While that structure may look confusing at first glance, it simplifies the actual editing a lot,
because you either only edit Maven coordinates or entries in the `.tpd` file.

### Eclipse plugins

Edit the `net.sf.eclipsecs.partial.tpd` file (note the _partial_ in the name).
After saving use the context menu of the .tpd file to create a new version of the `net.sf.eclipsecs.partial.target` file.

### Maven libraries

Meanwhile Eclipse PDE and Tycho can also use Maven libraries directly in the target platform
(by automatically wrapping them into a generated OSGi bundle).
Those libraries are contained in `net.sf.eclipsecs.target.target`.
You can open that with a text editor and edit the Maven coordinates like in a `pom.xml`.


## Committing changes

Both the `.target` and `.tpd` files must be checked in after changes.