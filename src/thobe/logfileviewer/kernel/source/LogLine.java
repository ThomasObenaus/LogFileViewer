/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source;

/**
 * @author Thomas Obenaus
 * @source LogLine.java
 * @date Jun 1, 2014
 */
public class LogLine
{
	private String	data;
	private long	id;

	public LogLine( long id, String data )
	{
		this.id = id;
		this.data = data;
	}

	public long getId( )
	{
		return id;
	}

	public String getData( )
	{
		return data;
	}

	@Override
	public String toString( )
	{
		return "{" + this.id + "}" + this.data;
	}
}
