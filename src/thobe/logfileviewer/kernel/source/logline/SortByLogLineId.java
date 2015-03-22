/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logline;

import java.util.Comparator;

import thobe.logfileviewer.plugin.source.logline.ILogLine;

/**
 * @author Thomas Obenaus
 * @source SortByLogLineId.java
 * @date Oct 12, 2014
 */
public class SortByLogLineId implements Comparator<ILogLine>
{

	@Override
	public int compare( ILogLine o1, ILogLine o2 )
	{
		if ( o1.getId( ) > o2.getId( ) )
			return 1;
		if ( o1.getId( ) < o2.getId( ) )
			return -1;
		return 0;
	}

}
