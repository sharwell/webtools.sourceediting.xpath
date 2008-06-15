package org.eclipse.wst.xml.xpath.ui.internal.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.sse.ui.internal.preferences.ui.AbstractPreferencePage;
import org.eclipse.wst.xml.xpath.ui.internal.XPathUIMessages;

@SuppressWarnings("restriction")
public class XPathPrefencePage extends AbstractPreferencePage {
	
	protected Control createContents(Composite parent) {
		Composite composite = createScrolledComposite(parent);

		String description = XPathUIMessages.XPathPrefencePage_0; 
		Text text = new Text(composite, SWT.READ_ONLY);
		// some themes on GTK have different background colors for Text and Labels
		text.setBackground(composite.getBackground());
		text.setText(description);

		setSize(composite);
		return composite;
	}
	

}