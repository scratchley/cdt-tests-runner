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
package org.eclipse.cdt.testsrunner.internal.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnerInfo;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Manages all the testing sessions (creates, activates, stores history).
 */
public class TestingSessionsManager {
	
	/** Tests Runners Plug-ins Manager. */
	private TestsRunnersManager testsRunnersManager;

	/** Testing sessions history list (the first is the newest). */
	private LinkedList<TestingSession> sessions = new LinkedList<TestingSession>();

	/** Currently active testing session. */
	private TestingSession activeSession;
	
	/** Listeners collection. */
	private List<ITestingSessionsManagerListener> listeners = new ArrayList<ITestingSessionsManagerListener>();
	
	/** The size limit of the testing sessions history. */
	private int historySizeLimit = 10;

	public TestingSessionsManager(TestsRunnersManager testsRunnersManager) {
		this.testsRunnersManager = testsRunnersManager;
	}
	
	/**
	 * Tries to find the last testing session for the specified launch
	 * configuration and Tests Runner Plug-in.
	 * 
	 * <p>
	 * Usually testing frameworks do not provide the information about tests
	 * hierarchy and total tests count before the testing is finished. So we try
	 * to reuse them from one the previous testing sessions that meets the
	 * requirements:
	 * <ul>
	 * <li>it should be for the same launch configuration;
	 * <li>it should be completed (finished and not stopped);
	 * <li>it should has the same tests runner;
	 * </ul>
	 * This function tries to find a such session.
	 * </p>
	 * 
	 * @param launchConfiguration required launch configuration
	 * @param testsRunnerInfo required Tests Runner Plug-in
	 * @return testing session or null if not found
	 */
	private TestingSession findActualPreviousSession(ILaunchConfiguration launchConfiguration, TestsRunnerInfo testsRunnerInfo) {
		String testsRunnerName = testsRunnerInfo.getName();
		ListIterator<TestingSession> sessionsIt = sessions.listIterator(sessions.size());
		while(sessionsIt.hasPrevious()) {
			TestingSession session = sessionsIt.previous();
			// Find the latest testing session that matches the next requirements:
			//   - it should be for the same launch configuration (should have the same parameters)
			//   - should be already terminated (to have complete tests hierarchy structure)
			//   - should not be stopped by user (the same as terminated)
			//   - should have the same tests runner
			if (session != null) {
				if (launchConfiguration.equals(session.getLaunch().getLaunchConfiguration())
					&& session.isFinished()
					&& !session.wasStopped()
					&& session.getTestsRunnerInfo().getName().equals(testsRunnerName)) {
					return session;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new testing session for the specified launch.
	 * 
	 * @param launch launch
	 * @return new testing session
	 */
	public TestingSession newSession(ILaunch launch) throws CoreException {
		TestsRunnerInfo testsRunnerInfo = testsRunnersManager.getTestsRunner(launch.getLaunchConfiguration());
		TestingSession previousSession = findActualPreviousSession(launch.getLaunchConfiguration(), testsRunnerInfo);
		TestingSession newTestingSession = new TestingSession(launch, testsRunnerInfo, previousSession);
		sessions.addFirst(newTestingSession);
		setActiveSession(newTestingSession);
		truncateHistory();
		return newTestingSession;
	}
	
	/**
	 * Returns the testing sessions history (the first is the newest).
	 * 
	 * @return testing sessions list
	 */
	public List<? extends ITestingSession> getSessions() {
		return sessions;
	}
	
	/**
	 * Rewrites the testing sessions history with the specified list. Truncates
	 * the history if necessary.
	 * 
	 * @return testing sessions list
	 */
	public void setSessions(List<ITestingSession> newSessions) {
		sessions.clear();
		for (ITestingSession newSession : newSessions) {
			sessions.add((TestingSession) newSession);
		}
		truncateHistory();
	}
	
	/**
	 * Returns the testing sessions history size.
	 * 
	 * @return history size
	 */
	public int getSessionsCount() {
		return sessions.size();
	}
	
	/**
	 * Accesses the currently active testing session.
	 * 
	 * @return testing session
	 */
	public ITestingSession getActiveSession() {
		return activeSession;
	}
	
	/**
	 * Sets the new active testing session.
	 * 
	 * @param newActiveSession testing session
	 */
	public void setActiveSession(ITestingSession newActiveSession) {
		if (activeSession != newActiveSession) {
			activeSession = (TestingSession) newActiveSession;
			// Notify listeners
			for (ITestingSessionsManagerListener listener : listeners) {
				listener.sessionActivated(activeSession);
			}
		}
	}
	
	/**
	 * Adds the given listener to this registered listeners collection.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addListener(ITestingSessionsManagerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the given listener from registered listeners collection.
	 * Has no effect if the listener is not already registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeListener(ITestingSessionsManagerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns the size limit of the testing sessions history.
	 * 
	 * @return history size limit
	 */
	public int getHistorySizeLimit() {
		return historySizeLimit;
	}

	/**
	 * Sets the size limit of the testing sessions history.
	 * 
	 * @param historySizeLimit new history size limit
	 */
	public void setHistorySizeLimit(int historySizeLimit) {
		this.historySizeLimit = historySizeLimit;
		truncateHistory();
	}
	
	/**
	 * Truncates the history list if it is longer than size limit.
	 */
	private void truncateHistory() {
		// The most frequently this method will be used to remove one element, so removeAll() is unnecessary here
		while (sessions.size() > historySizeLimit) {
			sessions.removeLast();
		}
		if (!sessions.contains(activeSession)) {
			ITestingSession newActiveSession = sessions.isEmpty() ? null : sessions.getFirst();
			setActiveSession(newActiveSession);
		}
	}
	
}