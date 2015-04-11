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
	 * Map of class-name to resource-name
	 */
	private Map<String, String>		classNameToResourceNameMap;

	/**
	 * Map of resource-name to the corresponding {@link URL} within the given jar-file
	 */
	private Map<String, URL>		resources;

	/**
	 * The internal {@link Logger}.
	 */
	private Logger					log;

	/**
	 * True if debugging is enabled, false otherwise.
	 */
	private boolean					debugLoggingEnabled;

	/**
	 * The zipfile in case we load the classes from a zip/jar.
	 */
	private ZipFile					zip;

	/**
	 * Map of all classes that where already defined.
	 */
	private Map<String, Class<?>>	alreadyDefined;

	/**
	 * Ctor - Loads classes and resources from a given jar/zip-File
	 * @param parent - the parent ClassLoader. This one can typically be obtained via<br>
	 *            <code>ClassLoader parent = SomeClass.class.getClassLoader();</code>
	 * @param jarFile - the jar/zip-file e.g. <br>
	 *            <code>ZipFile jarFile = new ZipFile("mylib.jar");</code>
	 * @throws ZipException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public PluginClassLoader( ClassLoader parent, ZipFile jarFile ) throws ZipException, IOException, URISyntaxException
	{
		this( parent, jarFile, null );
	}

	/**
	 * Ctor - Loads classes and resources from a given jar/zip-File
	 * @param parent - the parent ClassLoader. This one can typically be obtained via<br>
	 *            <code>ClassLoader parent = SomeClass.class.getClassLoader();</code>
	 * @param jarFile - the jar/zip-file e.g. <br>
	 *            <code>ZipFile jarFile = new ZipFile("mylib.jar");</code>
	 * @param log - the logger if u want to know which resources, classes where loaded. Typically the
	 *            {@link PluginClassLoader#PluginClassLoader(ClassLoader, ZipFile)} is used instead.
	 * @throws ZipException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public PluginClassLoader( ClassLoader parent, ZipFile jarFile, Logger log ) throws ZipException, IOException, URISyntaxException
	{
		super( parent );
		this.zip = jarFile;
		this.log = log;
		this.debugLoggingEnabled = ( ( this.log != null ) && ( this.log.isLoggable( Level.FINEST ) ) );
		this.classNameToResourceNameMap = new HashMap<String, String>( );
		this.resources = new HashMap<String, URL>( );
		this.alreadyDefined = new HashMap<>( );

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
				String name = resourceNameToClassName( nameOfEntry );
				if ( name != null )
				{
					this.classNameToResourceNameMap.put( name, nameOfEntry );
					// logging
					if ( this.debugLoggingEnabled )
					{
						this.log.finest( "Class: '" + name + ".class'" );
					}
				}// if ( name != null )

			}// if ( entry != null )
		}// while ( zipEntries.hasMoreElements( ) )
	}

	/**
	 * Ctor - Loads classes and resources from a given list of directories
	 * @param parent - the parent ClassLoader. This one can typically be obtained via<br>
	 *            <code>ClassLoader parent = SomeClass.class.getClassLoader();</code>
	 * @param directories - a set of directories
	 * @throws ZipException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public PluginClassLoader( ClassLoader parent, List<File> directories ) throws ZipException, IOException, URISyntaxException
	{
		this( parent, directories, null );
	}

	/**
	 * Ctor - Loads classes and resources from a given list of directories
	 * @param parent - the parent ClassLoader. This one can typically be obtained via<br>
	 *            <code>ClassLoader parent = SomeClass.class.getClassLoader();</code>
	 * @param directories - a set of directories
	 * @param log - the logger if u want to know which resources, classes where loaded. Typically the
	 *            {@link PluginClassLoader#PluginClassLoader(ClassLoader, List, Logger)} is used instead.
	 * @throws ZipException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public PluginClassLoader( ClassLoader parent, List<File> directories, Logger log ) throws ZipException, IOException, URISyntaxException
	{
		super( parent );
		this.debugLoggingEnabled = ( ( this.log != null ) && ( this.log.isLoggable( Level.FINEST ) ) );
		this.classNameToResourceNameMap = new HashMap<String, String>( );
		this.resources = new HashMap<String, URL>( );
		this.zip = null;

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
					String resourceName = nameOfEntry;
					if ( entry.isDirectory( ) )
					{
						resourceName += File.separator;
					}
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
						this.classNameToResourceNameMap.put( name, nameOfEntry );
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
		String nameOfResource = this.classNameToResourceNameMap.get( name );

		// use the parent classloader for unknown classes and for those which should not be loaded using this PluginClassLoder
		if ( ( nameOfResource == null ) || useParentClassLoader( name ) )
			return super.loadClass( name );

		Class<?> result = this.alreadyDefined.get( name );

		if ( result == null )
		{
			try
			{
				InputStream input = this.getInputStreamForEntry( nameOfResource );

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
				result = defineClass( name, classData, 0, classData.length );
				this.alreadyDefined.put( name, result );
			}
			catch ( IOException e )
			{
				if ( debugLoggingEnabled )
				{
					this.log.throwing( PluginClassLoader.class.getName( ), "loadClass", e );
				}
			}
		}

		return result;
	}

	private InputStream getInputStreamForEntry( String name ) throws IOException
	{
		InputStream is = null;
		if ( this.zip != null )
		{
			is = this.zip.getInputStream( this.zip.getEntry( name ) );
		}
		else
		{
			is = new FileInputStream( name );
		}
		return is;
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

	/**
	 * Finds all files in the given directory and goes on searching recursively if recurse is true. All files found will be placed in the
	 * set files.
	 * @param file
	 * @param files
	 * @param recurse
	 */
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

}
