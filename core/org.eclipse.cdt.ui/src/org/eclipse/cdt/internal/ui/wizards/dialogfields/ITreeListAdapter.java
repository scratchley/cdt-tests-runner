/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

import org.eclipse.swt.events.KeyEvent;

/**
 * Change listener used by <code>TreeListDialogField</code>
 */
public interface ITreeListAdapter<T> {
	
	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(TreeListDialogField<T> field, int index);
	
	/**
	 * The selection of the list has changed.
	 */	
	void selectionChanged(TreeListDialogField<T> field);

	/**
	 * The list has been double clicked
	 */
	void doubleClicked(TreeListDialogField<T> field);

	/**
	 * A key has been pressed
	 */
	void keyPressed(TreeListDialogField<T> field, KeyEvent event);

	Object[] getChildren(TreeListDialogField<T> field, Object element);

	Object getParent(TreeListDialogField<T> field, Object element);

	boolean hasChildren(TreeListDialogField<T> field, Object element);

}
