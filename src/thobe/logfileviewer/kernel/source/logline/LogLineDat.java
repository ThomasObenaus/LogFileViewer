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

import thobe.logfileviewer.plugin.util.SizeOf;

/**
 * @author Thomas Obenaus
 * @source LogLineDat.java
 * @date 11.09.2014
 */
public final class LogLineDat
{
	private final String	data;
	private static long		instances	= 0;

	public LogLineDat( String data )
	{
		this.data = data;
		instances++;
	}

	public static long getNumberOfInstances( )
	{
		return instances;
	}

	public long getMemory( )
	{
		return SizeOf.STRING( this.data );
	}

	@Override
	protected void finalize( ) throws Throwable
	{
		instances--;
		super.finalize( );
	}

	public String getData( )
	{
		return data;
	}

	@Override
	public int hashCode( )
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( data == null ) ? 0 : data.hashCode( ) );
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass( ) != obj.getClass( ) )
			return false;
		LogLineDat other = ( LogLineDat ) obj;
		if ( data == null )
		{
			if ( other.data != null )
				return false;
		}
		else if ( !data.equals( other.data ) )
			return false;
		return true;
	}
}
