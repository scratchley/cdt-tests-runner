/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;


import java.util.Iterator;

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchCommandConstants;

/**
 * TODO: add description
 */
public class CopySelectedTestsAction extends Action {

	private TreeViewer treeViewer;
	private Clipboard clipboard;

	public CopySelectedTestsAction(TreeViewer treeViewer, Clipboard clipboard) {
		super("Copy");
		setToolTipText("Copy The Selected Tests To Clipboard");
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		this.treeViewer = treeViewer;
		this.clipboard = clipboard;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (!selection.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			boolean needEOL = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				if (needEOL) {
					sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
				} else {
					needEOL = true;
				}
				sb.append(TestPathUtils.getTestItemPath((ITestItem)it.next()));
			}
			clipboard.setContents(
					new String[]{ sb.toString() },
					new Transfer[]{ TextTransfer.getInstance() });
		}
	}
	
}

