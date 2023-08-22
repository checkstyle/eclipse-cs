# eclipse-cs
The Eclipse Checkstyle plug-in integrates the Checkstyle Java code auditor into the Eclipse IDE.
The plug-in provides real-time feedback to the user about violations of rules that
check for coding style and possible error prone code constructs.

[Checkstyle](https://github.com/checkstyle/checkstyle) is an Open Source development tool to help you ensure that your Java code adheres to a set of coding standards. Checkstyle does this by inspecting your Java source code and pointing out items that deviate from a defined set of coding rules.

### Install
You can install the latest version of the plugin from the Eclipse Marketplace.
- Go to Help->Eclipse Marketplace...
- Search for "Checkstyle" or "Checkstyle Plug-in". Look for the eclipse-cs plugin.
- Click "Install".

If you need to install an older version:
- Go to Help->Install New Software...
- If Eclipse does not yet know about the Checkstyle repository, you need to add it. Click "Add..." to add a new repository.
  - Name: Checkstyle
  - Location: https://checkstyle.org/eclipse-cs-update-site/
- Make sure that the Checkstyle repository has been selected from the dropdown.
- Make sure that "Show only the latest versions of available software" is unchecked.
- You should now see the available versions of the Checkstyle plugin from which you can select and install.

### What does it do?
With the Checkstyle Eclipse Plugin your code is constantly inspected for coding standard deviations. Within the Eclipse workbench you are immediately notified of problems via the Eclipse Problems View and source code annotations similar to compiler errors or warnings.
This ensures an extremely short feedback loop right at the developers fingertips.

### Why would I use it?
If your development team consists of more than one person, then obviously a common ground for coding standards (formatting rules, line lengths etc.) must be agreed upon - even if it is just for practical reasons to avoid superficial, format related merge conflicts.
Checkstyle (and the Eclipse Checkstyle Plugin for that matter) helps you define and easily apply those common rules.

### Build it yourself
Clone the git repository (or create a fork) and import all projects into your Eclipse workspace.

Open a command line in project root directory and run `./mvnw clean package`.

Find the installable bundle update site archive `net.sf.eclipsecs-updatesite/target/net.sf.eclipsecs-updatesite_X.X.X.YYYYmmDDHHMM.zip`.
