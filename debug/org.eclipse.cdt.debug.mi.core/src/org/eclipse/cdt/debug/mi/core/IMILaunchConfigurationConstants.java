package org.eclipse.cdt.debug.mi.core;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */


public interface IMILaunchConfigurationConstants {
	/**
	 * Launch configuration attribute key. The value is the name of
	 * the Debuger associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_DEBUG_NAME = MIPlugin.getUniqueIdentifier() + ".DEBUG_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the gdb command file
	 * Debuger/gdb/MI property.
	 */
	public static final String ATTR_GDB_INIT = MIPlugin.getUniqueIdentifier() + ".GDB_INIT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'automatically load shared library symbols' flag of the debugger.
	 */
	public static final String ATTR_DEBUGGER_AUTO_SOLIB = MIPlugin.getUniqueIdentifier() + ".AUTO_SOLIB"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'stop on shared library events' flag of the debugger.
	 */
	public static final String ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS = MIPlugin.getUniqueIdentifier() + ".STOP_ON_SOLIB_EVENTS"; //$NON-NLS-1$
}
