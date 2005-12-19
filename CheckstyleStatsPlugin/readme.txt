This is an experimental addition to the eclipse-cs Checkstyle Plugin for Eclipse.
This plugin provides 2 views that display Checkstyle violations within the workspace.

Both views are acessible via Window->Show View->Other...->Checkstyle:

1. Checkstyle violations view

This view acts as a replacement for the Problems view in regard of the 
handling of Checkstyle violations.
Checkstyle markers are grouped together by their violation message, 
allowing for easily cleaning up of certain violations.
By double-clicking a violation group the user can drill down into a details view,
which shows all occurrences of this particular violation.
The violations view uses a filter facility similar to the Problems view.

2. Checkstyle violations chart

This view shows Checkstyle violations in form of a pie chart.
Similar to the first view a filter can be used to determine which violations should be shown.

This plug-in needs the standard eclipse-cs plugin (com.atlassw.tools.eclipse.checkstyle_x.x.x) 
in version 4.0.0 or greater to be installed.

