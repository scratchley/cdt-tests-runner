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
package org.eclipse.cdt.testsrunner.internal.boost;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.xml.sax.SAXException;

/**
 * The Tests Runner Plug-in to run tests with Boost.Test framework.
 * 
 * Configures the test module to output in XML format, parses the output and
 * provides the data for the Tests Runner Core.
 * 
 * @note There is a note about Tests Runner Plug-in accuracy. Communication
 * between Boost test module and Boost Tests Runner Plug-in is done through
 * standard output (which is buffered by default). Boost.Test (at least current
 * version - 1.48.0) does not provide a way to flush the data about tests
 * execution when they are available, so there may be a delay between test event
 * happening (e.g. test case is started) and its displaying in the results view.
 * The possible solution is to turn off the standard output buffering like this:
 * <pre>
 *     static struct DisableStdCoutBuffering
 *     {
 *         DisableStdCoutBuffering()
 *         {
 *             std::cout.setf(std::ios_base::unitbuf);
 *         }
 *     } s_disableStdCoutBuffering;
 * </pre>
 * It will make the results view showing progress more accurate.
 */
public class BoostTestsRunner implements ITestsRunner {

	public String[] getAdditionalLaunchParameters(String[][] testPaths) throws TestingException {
		final String[] boostParameters = {
			"--output_format=xml", //$NON-NLS-1$
			"--log_level=all", //$NON-NLS-1$
			"--report_level=no" //$NON-NLS-1$
		};
		String[] result = boostParameters;
		
		// Build tests filter
		if (testPaths != null && testPaths.length >= 1) {
			if (testPaths.length != 1) {
				throw new TestingException("Only on test suite or test case should be specified to rerun");
			}
			StringBuilder sb = new StringBuilder("--run_test="); //$NON-NLS-1$
			String[] testPath = testPaths[0];
			for (int i = 1; i < testPath.length; i++) {
				if (i != 1) {
					sb.append("/"); //$NON-NLS-1$
				}
				sb.append(testPath[i]);
			}
			result = new String[boostParameters.length + 1];
			System.arraycopy(boostParameters, 0, result, 0, boostParameters.length);
			result[boostParameters.length] = sb.toString();
		}
		return result;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(inputStream, new BoostXmlLogHandler(modelUpdater));
		} catch (IOException e) {
			throw new TestingException("I/O Error: "+e.getLocalizedMessage());
			
		} catch (NumberFormatException e) {
			throw new TestingException("XML parse error: Cannot convert integer value.");

		} catch (ParserConfigurationException e) {
			throw new TestingException("XML parse error: "+e.getLocalizedMessage());

		} catch (SAXException e) {
			throw new TestingException("XML parse error: "+e.getLocalizedMessage());
		}
	}

}
