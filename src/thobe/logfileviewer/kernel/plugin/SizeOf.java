/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin;

/**
 * @author Thomas Obenaus
 * @source SizeOf.java
 * @date Jun 15, 2014
 */
public class SizeOf
{
	/**
	 * #bytes of a byte (8 Bit signed)
	 */
	public static final long	BYTE	= 1;

	/**
	 * #bytes of a boolean (not really defined)
	 */
	public static final long	BOOLEAN	= 1;

	/**
	 * #bytes of a char (16 Bit unicode)
	 */
	public static final long	CHAR	= 2;

	/**
	 * #bytes of a short (16 Bit signed)
	 */
	public static final long	SHORT	= 2;

	/**
	 * #bytes of a integer (32 Bit signed)
	 */
	public static final long	INT		= 4;

	/**
	 * #bytes of a long (64 Bit signed)
	 */
	public static final long	LONG	= 8;

	/**
	 * #bytes of a float (32 Bit)
	 */
	public static final long	FLOAT	= 4;

	/**
	 * #bytes of a double (64 Bit)
	 */
	public static final long	DOUBLE	= 8;

	/**
	 * Size of a {@link String}
	 * @param str
	 * @return
	 */
	public static long STRING( String str )
	{
		return str.length( ) * CHAR;
	}

}
