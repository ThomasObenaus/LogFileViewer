/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.preferences;

import java.util.prefs.Preferences;

/**
 * @author Thomas Obenaus
 * @source ISubPrefs.java
 * @date Oct 31, 2014
 */
public interface ISubPrefs
{
	public void load( Preferences applicationRoot );

	public void save( Preferences applicationRoot );
}
