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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.table.AbstractTableModel;

import thobe.logfileviewer.kernel.source.logline.LogLine;
import thobe.logfileviewer.kernel.util.SizeOf;

/**
 * @author Thomas Obenaus
 * @source ConsoleTableModel.java
 * @date Jun 9, 2014
 */
@SuppressWarnings ( "serial")
public class ConsoleTableModel extends AbstractTableModel
{
	/**
	 * Memory (in bytes) needed for one line of the table
	 */
	private static long		BYTES_FOR_ONE_TABLE_ENTRY	= 2048;
	private static String	columnNames[]				= new String[]
														{ "LineNo", "Time", "Entry" };
	private List<LogLine>	entries;
	private long			memory;
	private int				maxNumberOfConsoleEntries;
	private int				linesToRemoveOnReachingMaxNumberOfConsoleEntries;
	private Timer			bufferOverflowWatcherTimer;

	public ConsoleTableModel( )
	{
		this.entries = new ArrayList<>( );
		this.memory = 0;
		this.maxNumberOfConsoleEntries = 100000;
		this.linesToRemoveOnReachingMaxNumberOfConsoleEntries = ( int ) ( this.maxNumberOfConsoleEntries * 0.4 );

		bufferOverflowWatcherTimer = new Timer( "ConsoleTableModel.BufferOverFlowWatcher.Timer" );
		bufferOverflowWatcherTimer.schedule( new BufferOverFlowWatcher( this ), 4000, 4000 );
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		if ( columnIndex == 2 )
			return true;
		return false;
	}

	private synchronized void removeEntriesIfMaxNumberOfConsoleEntriesWasReached( )
	{
		if ( this.entries.size( ) >= this.maxNumberOfConsoleEntries )
		{
			// compute lines to be removed:
			int linesToRemove = ( this.entries.size( ) - this.maxNumberOfConsoleEntries ) + this.linesToRemoveOnReachingMaxNumberOfConsoleEntries;

			// for memory only
			for ( int i = 0; i < linesToRemove; ++i )
				this.memory -= ( this.entries.get( i ).getMemory( ) + SizeOf.REFERENCE + BYTES_FOR_ONE_TABLE_ENTRY );

			// remove the entries from 0 to (linesToRemove-1)
			this.entries.subList( 0, linesToRemove ).clear( );

			this.fireTableRowsDeleted( 0, linesToRemove - 1 );
		}// if ( this.entries.size( ) >= this.maxNumberOfConsoleEntries ) .
	}

	public synchronized void addLine( LogLine line )
	{
		int rows = this.entries.size( );
		this.entries.add( line );
		this.memory += line.getMemory( );
		this.fireTableRowsInserted( rows, rows + 1 );
	}

	/**
	 * Returns true if the data of the {@link LogLine} specified by the row-index matches the given {@link Pattern}.
	 * @param row
	 * @param pattern
	 * @return
	 */
	public boolean matches( int row, Pattern pattern )
	{
		if ( this.getRowCount( ) <= row )
			return false;

		if ( pattern == null || pattern.pattern( ).trim( ).isEmpty( ) )
			return false;

		boolean result = false;

		try
		{
			Matcher m = pattern.matcher( this.entries.get( row ).getData( ) );
			result = m.find( );
		}
		catch ( PatternSyntaxException e )
		{}

		return result;
	}

	@Override
	public String getColumnName( int column )
	{
		if ( column == 0 )
			return columnNames[0];
		if ( column == 1 )
			return columnNames[1];
		if ( column == 2 )
			return columnNames[2];
		return "";
	}

	public synchronized void addBlock( List<LogLine> block )
	{
		if ( !block.isEmpty( ) )
		{
			int rows = this.getRowCount( );
			this.entries.addAll( block );
			this.fireTableRowsInserted( rows, rows + block.size( ) - 1 );

			// collect mem-information
			for ( LogLine l : block )
				this.memory += l.getMemory( ) + SizeOf.REFERENCE + BYTES_FOR_ONE_TABLE_ENTRY;
		}// if ( !block.isEmpty( ) ) .
	}

	public synchronized void clear( )
	{
		int lastRow = this.entries.size( );
		this.entries.clear( );
		this.memory = 0;
		this.fireTableRowsDeleted( 0, lastRow - 1 );
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		if ( columnIndex == 0 )
			return Integer.class;
		if ( columnIndex == 1 )
			return String.class;
		if ( columnIndex == 2 )
			return String.class;
		return super.getColumnClass( columnIndex );
	}

	@Override
	public int getColumnCount( )
	{
		return columnNames.length;
	}

	@Override
	public synchronized int getRowCount( )
	{
		return this.entries.size( );
	}

	@Override
	public synchronized Object getValueAt( int row, int col )
	{
		if ( this.entries.size( ) <= row )
			return null;

		LogLine line = this.entries.get( row );

		if ( col == 0 )
			return line.getId( );
		if ( col == 1 )
			return line.getTimeStampStr( );
		return line.getData( );
	}

	public long getMem( )
	{
		// mem + the size of the reference and house-keeping for the list of LogLines
		return this.memory + SizeOf.REFERENCE + SizeOf.HOUSE_KEEPING_ARRAY;
	}

	public int getMaxNumberOfConsoleEntries( )
	{
		return maxNumberOfConsoleEntries;
	}

	public void quit( )
	{
		this.bufferOverflowWatcherTimer.cancel( );
	}

	/**
	 * Task that removes all lines from the console that exceed the max number of lines.
	 */
	private class BufferOverFlowWatcher extends TimerTask
	{
		private ConsoleTableModel	tableModel;

		public BufferOverFlowWatcher( ConsoleTableModel tableModel )
		{
			this.tableModel = tableModel;
		}

		@Override
		public void run( )
		{
			this.tableModel.removeEntriesIfMaxNumberOfConsoleEntriesWasReached( );
		}
	}
}
