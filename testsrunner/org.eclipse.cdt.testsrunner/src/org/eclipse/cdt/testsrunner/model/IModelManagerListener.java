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
package org.eclipse.cdt.testsrunner.model;

/**
 * TODO: Add descriptions
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IModelManagerListener {

	void enterTestSuite(ITestSuite testSuite);
	
	void exitTestSuite(ITestSuite testSuite);
	
	void enterTestCase(ITestCase testCase);
	
	void exitTestCase(ITestCase testCase);

	void addTestSuite(ITestSuite parent, ITestSuite child);
	
	void addTestCase(ITestSuite parent, ITestCase child);
	
	void testingStarted(boolean restartPrevious);

	void testingFinished();
	
}