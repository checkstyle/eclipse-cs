[![][travis img]][travis]

# eclipse-cs
The Eclipse Checkstyle plug-in integrates the Checkstyle Java code auditor into the Eclipse IDE. 
The plug-in provides real-time feedback to the user about violations of rules that 
check for coding style and possible error prone code constructs. 

[Checkstyle](https://github.com/checkstyle/checkstyle) is an Open Source development tool to help you ensure that your Java code adheres to a set of coding standards. Checkstyle does this by inspecting your Java source code and pointing out items that deviate from a defined set of coding rules.

### Install and download
You can install the plugin by dragging and dropping the following link into your eclipse:
[Install ](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=150)

If you want to install manually, you can find the binaries here:
https://bintray.com/eclipse-cs/eclipse-cs/update-site-archive

### What does it do?
With the Checkstyle Eclipse Plugin your code is constantly inspected for coding standard deviations. Within the Eclipse workbench you are immediately notified of problems via the Eclipse Problems View and source code annotations similar to compiler errors or warnings. 
This ensures an extremely short feedback loop right at the developers fingertips.

### Why would I use it?
If your development team consists of more than one person, then obviously a common ground for coding standards (formatting rules, line lengths etc.) must be agreed upon - even if it is just for practical reasons to avoid superficial, format related merge conflicts. 
Checkstyle (and the Eclipse Checkstyle Plugin for that matter) helps you define and easily apply those common rules.

### Build it yourself
Clone the git repository (or create a fork) and import all projects into your Eclipse workspace.

Open a command line in project root directory and run `mvn clean package`. 

Find the installable bundle in net.sf.eclipsecs-updatesite/target.

[travis]:https://travis-ci.org/checkstyle/eclipse-cs
[travis img]:https://travis-ci.org/checkstyle/eclipse-cs.svg?branch=master
