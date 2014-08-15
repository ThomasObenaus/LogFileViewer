/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.console;

import java.util.regex.Pattern;

/**
 * Interface providing access to the instance/factory that is responsible for creating/registering {@link SubConsole}es.
 * @author Thomas Obenaus
 * @source ISubConsoleFactoryAccess.java
 * @date Jul 27, 2014
 */
public interface ISubConsoleFactoryAccess
{
	/**
	 * Creates a new instance of an {@link SubConsole}.
	 * @param parentConsolePattern - {@link Pattern} of the parent-console (might be null)
	 * @param pattern
	 * @return
	 */
	public SubConsole createNewSubConsole( String parentConsolePattern, String pattern, boolean closeable );

	/**
	 * Registers the given {@link SubConsole}. Additionally the {@link SubConsole}-Thread will be started and (in case
	 * registerVisualComponent is true) its visual component will be registered too (added to the main-frame).
	 * @param subConsole
	 * @param registerVisualComponent - If true, the visual component of the {@link SubConsole} will be added to the main-frame.
	 */
	public void registerSubConsole( SubConsole subConsole, boolean registerVisualComponent );

	public void unRegisterSubConsole( SubConsole subConsole );

	public String createTitle( SubConsole subConsole );

	public String createDescription( SubConsole subConsole );
}
