/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.playground.classLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Thomas Obenaus
 * @source PluginClassLoader.java
 * @date Apr 9, 2015
 */
public abstract class PluginClassLoader extends ClassLoader
{
	/**
	 * Map of class-name to the corresponding {@link InputStream}.
	 */
	private Map<String, InputStream>	entries;

	/**
	 * Map of resource-name to the corresponding {@link URL} within the given jar-file
	 */
	private Map<String, URL>			resources;

	/**
	 * The internal {@link Logger}.
	 */
	private Logger						log;

	/**
	 * True if debugging is enabled, false otherwise.
	 */
	private boolean						debugLoggingEnabled;

	public PluginClassLoader( ClassLoader parent, ZipFile jarFile ) throws ZipException, IOException, URISyntaxException
	{
		this( parent, jarFile, null );
	}

	private static void listFiles( final File file, Set<File> files, final boolean recurse )
	{
		File[] list = file.listFiles( );

		// no files found --> return
		if ( list == null )
			return;

		for ( File subFile : list )
		{
			files.add( subFile );
			if ( recurse && ( subFile.isDirectory( ) ) )
			{
				// recurse into directory
				listFiles( subFile, files, recurse );
			}
		}
	}

	public PluginClassLoader( ClassLoader parent, ZipFile jarFile, Logger log ) throws ZipException, IOException, URISyntaxException
	{
		super( parent );
		this.log = log;
		this.debugLoggingEnabled = ( ( this.log != null ) && ( this.log.isLoggable( Level.FINEST ) ) );
		this.entries = new HashMap<String, InputStream>( );
		this.resources = new HashMap<String, URL>( );

		// create the url-prefix for this jar-file 
		String urlPrefix = "jar:file:" + jarFile.getName( ) + "!/";

		// now iterate over all entries and obtain resources and input-streams for loading classes
		Enumeration<? extends ZipEntry> zipEntries = jarFile.entries( );
		while ( zipEntries.hasMoreElements( ) )
		{
			ZipEntry entry = zipEntries.nextElement( );
			if ( entry != null )
			{
				String nameOfEntry = entry.getName( );
				URL uriOfResource = new URI( urlPrefix + entry.getName( ) ).toURL( );

				// store the resource
				this.resources.put( nameOfEntry, uriOfResource );

				// logging
				if ( this.debugLoggingEnabled )
				{
					this.log.finest( "Res: '" + uriOfResource + "' available under '" + nameOfEntry + "'" );
				}

				// convert the given resource-name into a class-name if possible 
				// non class-entries will be ignored
				String name = resourceNameToClassName( entry.getName( ) );
				if ( name != null )
				{
					this.entries.put( name, jarFile.getInputStream( entry ) );
					// logging
					if ( this.debugLoggingEnabled )
					{
						this.log.finest( "Class: '" + name + ".class'" );
					}
				}// if ( name != null )

			}// if ( entry != null )
		}// while ( zipEntries.hasMoreElements( ) )
	}

	public PluginClassLoader( ClassLoader parent, List<File> directories ) throws ZipException, IOException, URISyntaxException
	{
		this( parent, directories, null );
	}

	public PluginClassLoader( ClassLoader parent, List<File> directories, Logger log ) throws ZipException, IOException, URISyntaxException
	{
		super( parent );
		this.debugLoggingEnabled = ( ( this.log != null ) && ( this.log.isLoggable( Level.FINEST ) ) );
		this.entries = new HashMap<String, InputStream>( );
		this.resources = new HashMap<String, URL>( );

		for ( File directory : directories )
		{
			if ( directory == null )
			{
				throw new IOException( "The given directory is null." );
			}
			if ( !directory.isDirectory( ) )
			{
				throw new IOException( "The given file is no directory." );
			}

			// create the url-prefix for this directory		
			String urlPrefixToSubtract = directory.getAbsolutePath( );
			if ( !urlPrefixToSubtract.endsWith( File.separator ) )
			{
				urlPrefixToSubtract += File.separator;
			}
			String urlPrefix = "file:" + urlPrefixToSubtract;

			Set<File> files = new HashSet<File>( );
			listFiles( directory, files, true );

			// now iterate over all entries and obtain resources and input-streams for loading classes
			for ( File entry : files )
			{
				if ( entry != null )
				{
					String nameOfEntry = entry.getAbsolutePath( ).replaceAll( urlPrefixToSubtract, "" );
					String resourceName = nameOfEntry + File.separator;
					URL uriOfResource = new URI( urlPrefix + resourceName ).toURL( );

					// store the resource
					this.resources.put( resourceName, uriOfResource );

					// logging
					if ( this.debugLoggingEnabled )
					{
						this.log.finest( "Res: '" + uriOfResource + "' available under '" + nameOfEntry + "'" );
					}

					// convert the given resource-name into a class-name if possible 
					// non class-entries will be ignored
					String name = resourceNameToClassName( nameOfEntry );
					if ( name != null )
					{
						this.entries.put( name, new FileInputStream( entry ) );
						// logging
						if ( this.debugLoggingEnabled )
						{
							this.log.finest( "Class: '" + name + ".class'" );
						}
					}// if ( name != null )

				}// if ( entry != null )
			}// for ( File entry : files )
		}
	}

	/**
	 * Implement this method to decide which of the classes (denoted by the given name) should be loaded by the parent {@link ClassLoader}.
	 * This method should return true if it should load the given class using the parent {@link ClassLoader}. This method should return
	 * false if the given class should be loaded from the given container (jar-, zip-file or folder).
	 * @param name - name of the class to be load
	 * @return
	 */
	protected abstract boolean useParentClassLoader( String name );

	@Override
	protected URL findResource( String name )
	{
		// try to find the requested resource in the stored resources
		URL result = this.resources.get( name );
		if ( result == null )
		{
			return super.findResource( name );
		}

		return result;
	}

	public Class<?> loadClass( String name ) throws ClassNotFoundException
	{
		InputStream input = this.entries.get( name );

		// use the parent classloader for unknown classes and for those which should not be loaded using this PluginClassLoder
		if ( ( input == null ) || useParentClassLoader( name ) )
			return super.loadClass( name );

		try
		{
			// read the class-file
			ByteArrayOutputStream buffer = new ByteArrayOutputStream( );
			int data = input.read( );

			while ( data != -1 )
			{
				buffer.write( data );
				data = input.read( );
			}

			input.close( );

			// now define the class, will never be loaded again
			byte[] classData = buffer.toByteArray( );
			return defineClass( name, classData, 0, classData.length );
		}
		catch ( IOException e )
		{
			if ( debugLoggingEnabled )
			{
				this.log.throwing( PluginClassLoader.class.getName( ), "loadClass", e );
			}
		}

		return null;
	}

	/**
	 * Converts the given resource-/ file-name into a class-name if possible. If this operation fails null will be returned.
	 * A resource e.g. 'package/test/Test.class' will be converted to 'package.test.Test' ('/' converted to '.' and '.class' removed).
	 * @param ressourceName
	 * @return - returns null on fail, otherwise the class-name.
	 */
	private static String resourceNameToClassName( String ressourceName )
	{
		String result = null;
		if ( ( ressourceName != null ) && ( ressourceName.endsWith( ".class" ) ) )
		{
			// replace all / with .
			result = ressourceName.replaceAll( "/", "." );

			// crop the string '.class'
			result = result.replaceAll( "\\.class", "" );

		}// if ( ( ressourceName != null )  && ( ressourceName.endsWith( ".class" ) ) )
		return result;
	}

}
