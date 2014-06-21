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

import javax.swing.table.AbstractTableModel;

import thobe.logfileviewer.kernel.source.LogLine;

/**
 * @author Thomas Obenaus
 * @source ConsoleTableModel.java
 * @date Jun 9, 2014
 */
@SuppressWarnings ( "serial")
public class ConsoleTableModel extends AbstractTableModel
{
	private static String	columnNames[]	= new String[]
											{ "LineNo", "Time", "Entry" };
	private List<LogLine>	entries;
	private long			memory;

	public ConsoleTableModel( )
	{
		this.entries = new ArrayList<>( );
		this.memory = 0;
	}

	public void addLine( LogLine line )
	{
		int rows = this.entries.size( );
		this.entries.add( line );
		this.memory += line.getMem( );
		this.fireTableRowsInserted( rows, rows + 1 );
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

	public void addBlock( List<LogLine> block )
	{
		if ( !block.isEmpty( ) )
		{
			int rows = this.getRowCount( );
			this.entries.addAll( block );
			this.fireTableRowsInserted( rows, rows + block.size( ) );

			// collect mem-information
			for ( LogLine l : block )
				this.memory += l.getMem( );
		}
	}

	public void clear( )
	{
		int lastRow = this.entries.size( );
		this.entries.clear( );
		this.memory = 0;
		this.fireTableRowsDeleted( 0, lastRow );
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
	public int getRowCount( )
	{
		return this.entries.size( );
	}

	@Override
	public Object getValueAt( int row, int col )
	{
		LogLine line = this.entries.get( row );

		if ( col == 0 )
			return row;
		if ( col == 1 )
			return line.getTimeStampStr( );
		return line.getData( );
	}

	public long getMem( )
	{
		return this.memory;
	}
}
