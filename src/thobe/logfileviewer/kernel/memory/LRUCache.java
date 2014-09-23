/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.memory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple threadsafe LRU-Cache.
 * @author Thomas Obenaus
 * @source LRUCache.java
 * @date Sep 21, 2014
 */
public class LRUCache<K, V>
{
	public static final int	DEFAULT_MAX_SIZE	= 1000;

	private final Map<K, V>	dataMap;
	private final int		maxCacheSize;

	public LRUCache( )
	{
		this( DEFAULT_MAX_SIZE );
	}

	@SuppressWarnings ( "serial")
	public LRUCache( final int maxSize )
	{
		this.maxCacheSize = maxSize;
		this.dataMap = ( Map<K, V> ) Collections.synchronizedMap( new LinkedHashMap<K, V>( maxSize + 1, 0.75f, true )
		{
			@Override
			protected boolean removeEldestEntry( Map.Entry<K, V> eldest )
			{
				return size( ) > maxSize;
			}
		} );
	}

	/**
	 * Add a new value into the cache.
	 * @param key - The key.
	 * @param value - The value to be associated with the key.
	 * @return - The old value associated with the given key or null.
	 */
	public V put( K key, V value )
	{
		return dataMap.put( key, value );
	}

	/**
	 * Get the value associated with the given key or null if there is no value in the map.
	 * @param key
	 * @return
	 */
	public V get( K key )
	{
		return dataMap.get( key );
	}

	/**
	 * Returns the current size of the cache.
	 * @return
	 */
	public int size( )
	{
		return this.dataMap.size( );
	}

	/**
	 * Clears the cache.
	 */
	public void clear( )
	{
		this.dataMap.clear( );
	}

	/**
	 * Returns the maximum size of the cache. Until reaching this limit the least recently used entries will be removed.
	 * @return
	 */
	public int getMaxCacheSize( )
	{
		return maxCacheSize;
	}
}
