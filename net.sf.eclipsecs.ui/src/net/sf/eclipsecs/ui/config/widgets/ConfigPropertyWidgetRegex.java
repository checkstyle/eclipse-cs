//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.ui.config.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.regex.RegexCompletionProposalFactory;

/**
 * A string property configuration widget.
 */
public final class ConfigPropertyWidgetRegex extends AbstractConfigPropertyWidget {

    /** The red color. */
    private static final Color RED = new Color(255, 225, 225);
    /** The green color. */
    private static final Color GREEN = new Color(219, 235, 204);

    /** The default test message. */
    private final String mDefaultMessage = Messages.ConfigPropertyWidgetRegex_msgRegexTestString;

    /** The contents composite. */
    private Composite mContents;

    /** The text widget. */
    private Text mTextWidget;

    /** The regex test text widget. */
    private Text mRegexTestWidget;

    /** The text background color. */
    private Color mTextBgColor;

    /**
     * Creates the widget.
     *
     * @param parent
     *            the parent composite
     * @param prop
     *            the property
     */
    private ConfigPropertyWidgetRegex(Composite parent, ConfigProperty prop) {
        super(parent, prop);
    }

    public static ConfigPropertyWidgetRegex create(Composite parent, ConfigProperty prop) {
        return new ConfigPropertyWidgetRegex(parent, prop);
    }

    @Override
    protected Control getValueWidget(Composite parent) {

        if (mContents == null) {

            mContents = new Composite(parent, SWT.NULL);
            mContents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            final GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            mContents.setLayout(layout);

            //
            // Create a text entry field.
            //
            mTextWidget = new Text(mContents, SWT.SINGLE | SWT.BORDER);
            mTextWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mTextWidget.addKeyListener(new RegexTestListener());
            mTextBgColor = mTextWidget.getBackground();

            // content assist
            RegexCompletionProposalFactory.createForText(mTextWidget);

            final String initValue = getInitValue();
            if (initValue != null) {
                mTextWidget.setText(initValue);
            }

            mRegexTestWidget = new Text(mContents, SWT.SINGLE | SWT.BORDER);
            mRegexTestWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mRegexTestWidget.setMessage(mDefaultMessage);
            mRegexTestWidget.addKeyListener(new RegexTestListener());

        }

        return mTextWidget;
    }

    @Override
    public String getValue() {
        String result = mTextWidget.getText();
        if (result == null) {
            result = "";
        }
        return result;
    }

    @Override
    public void restorePropertyDefault() {
        final ConfigPropertyMetadata metadata = getConfigProperty().getMetaData();
        final String defaultValue =
            metadata.getOverrideDefault() != null ? metadata.getOverrideDefault()
                : metadata.getDefaultValue();
        mTextWidget.setText(defaultValue != null ? defaultValue : "");
    }

    @Override
    public void validate() throws CheckstylePluginException {
        try {
            //
            // Compile the text to a regex pattern
            //
            Pattern.compile(mTextWidget.getText());
        } catch (PatternSyntaxException ex) {
            CheckstylePluginException.rethrow(ex, ex.getLocalizedMessage());
        }
    }

    private void testRegex() {
        try {
            final Pattern pattern = Pattern.compile(mTextWidget.getText());
            final Matcher matcher = pattern.matcher(mRegexTestWidget.getText());
            if (matcher.find()) {
                mRegexTestWidget.setBackground(GREEN);
            } else {
                mRegexTestWidget.setBackground(RED);
            }

            mTextWidget.setBackground(mTextBgColor);
        } catch (PatternSyntaxException ex) {
            mTextWidget.setBackground(RED);
        }
    }

    /**
     * Simple key listener to test the regular expression.
     *
     */
    private final class RegexTestListener implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            // NOOP
        }

        @Override
        public void keyReleased(KeyEvent e) {
            testRegex();
        }
    }
}
