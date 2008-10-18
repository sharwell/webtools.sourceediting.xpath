/*******************************************************************************
 * Copyright (c) 2008 Chase Technology Ltd - http://www.chasetechnology.co.uk
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Satchwell (Chase Technology Ltd) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xml.xpath.ui.internal.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.wst.xml.xpath.messages.Messages;
import org.eclipse.wst.xml.xpath.ui.internal.XPathUIPlugin;

class XPathViewActions
{
	private ImageDescriptor COLLAPSE_D = XPathUIPlugin.imageDescriptorFromPlugin(XPathUIPlugin.PLUGIN_ID, "icons/full/dlcl16/collapseall.gif");
	private ImageDescriptor COLLAPSE_E = XPathUIPlugin.imageDescriptorFromPlugin(XPathUIPlugin.PLUGIN_ID, "icons/full/elcl16/collapseall.gif");
	private ImageDescriptor SYNCED_D = XPathUIPlugin.imageDescriptorFromPlugin(XPathUIPlugin.PLUGIN_ID, "icons/full/dlcl16/synced.gif");
	private ImageDescriptor SYNCED_E = XPathUIPlugin.imageDescriptorFromPlugin(XPathUIPlugin.PLUGIN_ID, "icons/full/elcl16/synced.gif");

	boolean linkWithEditor = false;
	private CollapseTreeAction collapseAction;
	private ToggleLinkAction toggleAction;

	protected IAction[] createMenuContributions(TreeViewer viewer)
	{
		return new IAction[]{  };
	}

	protected IAction[] createToolbarContributions(TreeViewer viewer)
	{
		this.collapseAction = new CollapseTreeAction(viewer);
		this.toggleAction = new ToggleLinkAction();
		toggleAction.setChecked(linkWithEditor);
		return new IAction[]{ collapseAction,toggleAction };
	}

	public ISelection getSelection(TreeViewer viewer, ISelection selection)
	{
		return selection;
	}

	public boolean isLinkedWithEditor(TreeViewer treeViewer)
	{
		return linkWithEditor;
	}

	void setLinkWithEditor(boolean isLinkWithEditor)
	{
		linkWithEditor = isLinkWithEditor;
	}
	
	void fillContextMenu(IMenuManager manager)
	{
        manager.add(collapseAction);
        manager.add(toggleAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

//	private static ImageDescriptor getImageDescriptor(String relativePath)
//	{
//		String iconPath = "icons/";
//		return XMLUIPlugin.imageDescriptorFromPlugin(XMLUIPlugin.PLUGIN_ID, iconPath);
//	}

	private class CollapseTreeAction extends Action
	{
		private TreeViewer fTreeViewer = null;

		public CollapseTreeAction(TreeViewer viewer)
		{
			super(Messages.XPathViewActions_0, AS_PUSH_BUTTON);
			setImageDescriptor(COLLAPSE_E);
			setDisabledImageDescriptor(COLLAPSE_D);
			setToolTipText(getText());
			fTreeViewer = viewer;
		}

		public void run()
		{
			fTreeViewer.collapseAll();
		}
	}

	private class ToggleLinkAction extends Action
	{
		public ToggleLinkAction()
		{
			super(Messages.XPathViewActions_1, AS_CHECK_BOX);
			setToolTipText(getText());
			setDisabledImageDescriptor(SYNCED_D);
			setImageDescriptor(SYNCED_E);
		}

		public void run()
		{
			setLinkWithEditor(isChecked());
		}
	}
}