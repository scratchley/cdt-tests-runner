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


import java.net.URI;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.model.ITestLocation;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles tests tree hierarchy auto-scroll
 */
public class OpenInEditorAction extends Action {

	private TableViewer messagesViewer;

	public OpenInEditorAction(TableViewer tableViewer) {
		super("Go to");
		this.messagesViewer = tableViewer;
		setToolTipText("Go to file pointed by message"); // TODO: Add detailed tooltip
		// TODO: Add image
//		setDisabledImageDescriptor(Activator.getImageDescriptor("dlcl16/scroll_lock.gif")); //$NON-NLS-1$
//		setHoverImageDescriptor(Activator.getImageDescriptor("elcl16/scroll_lock.gif")); //$NON-NLS-1$
//		setImageDescriptor(Activator.getImageDescriptor("elcl16/scroll_lock.gif")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		//modelSyncronizer.setAutoScroll(!isChecked());
		Object selectedObject = ((IStructuredSelection)messagesViewer.getSelection()).getFirstElement();
		if (selectedObject != null && selectedObject instanceof ITestMessage) {
			ITestLocation messageLocation = ((ITestMessage)selectedObject).getLocation();
			if (messageLocation != null) {
				// Get source locator
				ILaunch launch = Activator.getDefault().getTestsRunnersManager().getCurrentLaunch();
				ISourceLocator sourceLocator = launch.getSourceLocator();
				ISourceLookupResult result = DebugUITools.lookupSource(messageLocation.getFile(), sourceLocator);
				try {
					openEditorAndSelect(result, messageLocation.getLine());
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void openEditorAndSelect(ISourceLookupResult result, int line) throws PartInitException, BadLocationException {
		IEditorInput input = result.getEditorInput();
		String editorID = result.getEditorId();
		
		if (input == null || editorID == null) {
			// Consult the CDT DebugModelPresentation
			Object sourceElement = result.getSourceElement();
			if (sourceElement != null) {
				// Resolve IResource in case we get a LocalFileStorage object
				if (sourceElement instanceof LocalFileStorage) {
					IPath filePath = ((LocalFileStorage) sourceElement).getFullPath();
					URI fileURI = URIUtil.toURI(filePath);
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IFile[] files = root.findFilesForLocationURI(fileURI);
					if (files.length > 0) {
						// Take the first match
						sourceElement = files[0];
					}
				}
				
				IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation(CDebugCorePlugin.getUniqueIdentifier());
				input = pres.getEditorInput(sourceElement);
				editorID = pres.getEditorId(input, sourceElement);
				pres.dispose();
			}
		}
		if (input != null && editorID != null) {
			// Open the editor
			IWorkbenchPage activePage = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();;

			IEditorPart editor = IDE.openEditor(activePage, input, editorID);
			// Select the line
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;

				if (line > 0) {
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());

					IRegion lineRegion = document.getLineInformation(line - 1); //zero-indexed
					textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
				}
			}
		}
	}
	
}
