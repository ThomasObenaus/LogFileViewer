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

/**
 * @author Thomas Obenaus
 * @source ILogLineFactoryAccess.java
 * @date 23.09.2014
 */
public interface ILogLineFactoryAccess
{
	/**
	 * The maximum size (number of entries) of the cache.
	 * @return
	 */
	public int getMaxCacheSize( );

	/**
	 * Current size (number of entries) of the cache.
	 * @return
	 */
	public int getCacheSize( );

	/**
	 * Returns Cache-Statistics: hits
	 * @return
	 */
	public long getCacheHits( );

	/**
	 * Returns Cache-Statistics: misses
	 * @return
	 */
	public long getCacheMisses( );

	/**
	 * Returns Cache-Statistics: cache-ratio
	 * @return
	 */
	public float getCacheRatio( );

	/**
	 * Returns the memory consumed by the cache in bytes.
	 * @return
	 */
	public long getCacheMemory( );

	/**
	 * Frees the memory used in internal cache.
	 */
	public void clearCache( );

}
