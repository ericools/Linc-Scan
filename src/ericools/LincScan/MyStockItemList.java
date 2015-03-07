package ericools.LincScan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ericools.LincScan.R;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class MyStockItemList {
	
	/*
	 * Handling list of all records
	 *  
	 *  Plays beep if price*quantity > 100.0 for an added record
	 *  Responsible for loading / saving Autosave and export
	 *  All UI diplay stuff regarding the list happens here
	 *
	 */
	
	private MediaPlayer mMediaPlayer;
	private ArrayList < MyStockItem > stockItems;
	private ArrayList < TableRow > allTableRows;
	private LincActivity mainActivity;
	private TableLayout tableLayout;
	private int currentSelection;
	
	private LincDBAdapter dbAdapter;
	
	public MyStockItemList(LincActivity act) {
		mainActivity=act;
		stockItems = new ArrayList < MyStockItem >();
		allTableRows = new ArrayList < TableRow >();
	
		tableLayout = new TableLayout(mainActivity);
		tableLayout.setLayoutParams(new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
		tableLayout.setStretchAllColumns(true);
		
		
		ScrollView  fl = (ScrollView) mainActivity.findViewById(R.id.frameLayout1);
		fl.addView(tableLayout);
		fl.setVerticalScrollBarEnabled(true);
		
		dbAdapter = new LincDBAdapter(mainActivity);
		dbAdapter.open();
		
		
		createTableHeader();
		
		currentSelection=-1;
	}
	
	public void close() {
		dbAdapter.close();
	}
	
	public void clear() {
		tableLayout.removeAllViews();
		allTableRows.clear();
		stockItems.clear();
		currentSelection=-1;
		createTableHeader();
	}
	
	private void createTableHeader() {
		TableRow tr = new TableRow(mainActivity);
		tr.setLayoutParams(new LayoutParams(
                  LayoutParams.FILL_PARENT,
                  LayoutParams.WRAP_CONTENT));
		
		for (int i=0;i<6;++i)
		{
			
			TextView tv = new TextView(mainActivity);
			tv.setLayoutParams(new LayoutParams(
	                LayoutParams.FILL_PARENT,
	                LayoutParams.WRAP_CONTENT));
			tv.setGravity(Gravity.CENTER);
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(15);
			tv.setText(MyStockItem.entryCaptions[i]);
			if (i!=3)
			{
				tr.addView(tv);
			}
		}
		TableRow tr2 = new TableRow(mainActivity);
		tr2.setLayoutParams(new LayoutParams(
                  LayoutParams.FILL_PARENT,
                  LayoutParams.WRAP_CONTENT));
		tr2.setMinimumHeight(2);
		tr2.setBackgroundColor(Color.YELLOW);
		
		tableLayout.addView(tr);
		tableLayout.addView(tr2);
	}
	
	public int getCurrentSelection()
	{
		return currentSelection;
	}
	
	public void loadFromDB() {

    	clear();
		
		Cursor allDBEntries=dbAdapter.fetchAllEntries();
		if (allDBEntries.getCount()>0) {	
			if (allDBEntries.moveToFirst()) {
				MyStockItem newitem=new MyStockItem();
				newitem.dbId=allDBEntries.getLong(0);
				for (int i=0;i<9;++i) {
					 newitem.setStockItemValue(i, allDBEntries.getString(i+1));
					}
				try {
					int quant=Integer.parseInt(allDBEntries.getString(6));
					newitem.setQuantity(quant);
				}
				catch (NumberFormatException e) {
					Log.i("LincScan", "couldnt convert integer quantity from autosave");
				}
				this.addItemToMemory(newitem);
			}
			while (allDBEntries.moveToNext()) {
				MyStockItem newitem=new MyStockItem();
				newitem.dbId=allDBEntries.getLong(0);
				for (int i=0;i<9;++i) {
					 newitem.setStockItemValue(i, allDBEntries.getString(i+1));
					}
				try {
					int quant=Integer.parseInt(allDBEntries.getString(6));
					newitem.setQuantity(quant);
				}
				catch (NumberFormatException e) {
					Log.i("LincScan", "couldnt convert integer quantity from autosave");
				}
				this.addItemToMemory(newitem);
			}
		  selectNewPosition(0);
		}
	}
	
	public void deleteDB() {
		dbAdapter.deleteAllEntries();
		clear();
	}
	
	public void export() {
    	Time timest = new Time();
    	timest.setToNow();
    	saveFromDBToCSV("LincScanData", "export_" + timest.format2445() + ".csv" );
    	saveFromDBToCSV("LincScanDataBackup", "autosave_" + timest.format2445() + ".csv" );
    	// delete the internal SQLite database entries
    	deleteDB();
    	
    	// delete all autosave files
    	deleteFile("LincScanData", "autosave.csv");
    	deleteFile("LincScanDataBackup", "autosave_backup.csv");
    	deleteFile("LincScanDataBackup", "autosave_backup_memory.csv");
	}
	
	private void saveFromDBToCSV(String directory, String filename) {
			try {
			    File root = Environment.getExternalStorageDirectory();
			    File listappDir = new File(root.toString() + "/" + directory);
			    listappDir.mkdirs();
			    if (listappDir.canWrite()){
			        File savefile = new File(listappDir + "/" + filename);
			        FileWriter filewriter = new FileWriter(savefile,false);
			        BufferedWriter out = new BufferedWriter(filewriter);
			        out.write("Area/Section/Department/Category/Price/Quantity/User/Timestamp/SKU\n");
			        Cursor allDBEntries=dbAdapter.fetchAllEntries();
					if (allDBEntries.getCount()>0) {	
						if (allDBEntries.moveToFirst()) {
							for (int i=1;i<10;++i) { // do not write the dbId!
								out.write(allDBEntries.getString(i));
									if (i<9)
					        		{
					        			out.write(",");
					        		}
								}
							out.write("\n");
						}
						while (allDBEntries.moveToNext()) {
							for (int i=1;i<10;++i) {
								out.write(allDBEntries.getString(i));
									if (i<9)
					        		{
					        			out.write(",");
					        		}
								}
							out.write("\n");
						}
					}
			        out.close();
			    }
			} catch (IOException e) {
			    Log.e("LincScan", "Could not write file - " + e.getMessage());
			}
	}
	
	private void saveFromMemoryToCSV(String directory, String filename) {
		if (stockItems.size()>0) {
			try {
			    File root = Environment.getExternalStorageDirectory();
			    File listappDir = new File(root.toString() + "/" + directory);
			    listappDir.mkdirs();
			    if (listappDir.canWrite()){
			        File savefile = new File(listappDir + "/" + filename);
			        FileWriter filewriter = new FileWriter(savefile,false);
			        BufferedWriter out = new BufferedWriter(filewriter);
			        out.write("Area/Section/Department/Category/Price/Quantity/User/Timestamp/SKU\n");
			        for (int i=0;i<stockItems.size();++i)
			        {
			        	MyStockItem current=stockItems.get(i);
			        	for (int j=0; j<9; ++j)
			        	{
			        		out.write(current.getStockItemValue(j));
			        		if (j<8)
			        		{
			        			out.write(",");
			        		}
			        	}
			        	out.write("\n");
			        }
			        out.close();
			    }
			} catch (IOException e) {
			    Log.e("LincScan", "Could not write file - " + e.getMessage());
			}
		}
	}
	
	private void deleteFile(String directory, String filename) {
		File root = Environment.getExternalStorageDirectory();
	    File listappDir = new File(root.toString() + "/" + directory);
	    if (listappDir.canWrite()){
	        File delfile = new File(listappDir + "/" + filename);
	        delfile.delete();
	    }
	}
	
	public void autoSave() {
		// do the autosave from the internal SQLite DB
		saveFromDBToCSV("LincScanData", "autosave.csv");
		// do it again to a backup file (if first save crashes the program and it started the autosave, autosave.csv may be lost)
		saveFromDBToCSV("LincScanDataBackup", "autosave_backup.csv");
		// if there was some problem with the database also do a backup from the entries in the memory
		saveFromMemoryToCSV("LincScanDataBackup", "autosave_backup_memory.csv");
	}
	
	public void selectNext() {
		selectNewPosition(currentSelection+1);	
	}
	
	public void selectPrevious() {
		selectNewPosition(currentSelection-1);
	}
	
	public void selectNewPosition(int index) {
		if (index>-1 && index<allTableRows.size())
		{
			if (currentSelection>-1 && currentSelection<allTableRows.size() )
			{
				TableRow currentRow=allTableRows.get(currentSelection);
				for (int i=0;i<currentRow.getChildCount();++i)
				{
					TextView currentView=(TextView)currentRow.getChildAt(i);
					currentView.setBackgroundColor(Color.BLACK);
					currentView.setTextColor(Color.LTGRAY);
				}
			}
			TableRow currentRow=allTableRows.get(index);
			for (int i=0;i<currentRow.getChildCount();++i)
			{
				TextView currentView=(TextView)currentRow.getChildAt(i);
				currentView.setBackgroundColor(Color.argb(170,255, 140, 0));
				currentView.setTextColor(Color.WHITE);
			}
			currentSelection=index;
		}
		
		ScrollView  fl = (ScrollView) mainActivity.findViewById(R.id.frameLayout1);
		// put the scrolling into runnable so that the views are already updated when scrolling
		fl.post(new Runnable() { 
		    public void run() {
		    	ScrollView  fl = (ScrollView) mainActivity.findViewById(R.id.frameLayout1);
		    	int height=tableLayout.getChildAt(0).getMeasuredHeight();
				fl.smoothScrollTo(0,currentSelection*height);
		    } 
		}); 
	}
	
	public void deleteCurrentMyStockItem()
	{
		if (currentSelection>-1) {
			dbAdapter.deleteEntry(stockItems.get(currentSelection).dbId);
			tableLayout.removeView(allTableRows.get(currentSelection));
			allTableRows.remove(currentSelection);
			stockItems.remove(currentSelection);
			if (allTableRows.size()>0)
			{
				if (currentSelection>0)	{
					selectPrevious();
				}
				else {
					selectNewPosition(0);
				}
			}
			else 
			{
				currentSelection=-1;
			}
		}
		autoSave();
	}
	
	public void activeEdit() {
		TableRow currentRow=allTableRows.get(currentSelection);
		for (int i=0;i<currentRow.getChildCount();++i)
		{
			TextView currentView=(TextView)currentRow.getChildAt(i);
			currentView.setBackgroundColor(Color.argb(170,255, 0, 0));
			currentView.setTextColor(Color.WHITE);
		}
	}
	
	public Double getTotals(MyStockItem item) {
		return Double.valueOf(dbAdapter.getTotals(item.getStockItemValue(0), item.getStockItemValue(1), item.getStockItemValue(2)));
	}
	
	public void addMyStockItem(MyStockItem item) {
		
		item.dbId=dbAdapter.createEntry(item.getStockItemValue(0),item.getStockItemValue(1),item.getStockItemValue(2),item.getStockItemValue(3),item.getStockItemValue(4),item.getStockItemValue(5),item.getStockItemValue(6),item.getStockItemValue(7),item.getStockItemValue(8));
		
		addItemToMemory(item);
		autoSave();
	}
	
	
	
	public void addItemToMemory(MyStockItem item) {
		stockItems.add(item);
		TableRow tr = new TableRow(mainActivity);
		tr.setLayoutParams(new LayoutParams(
                  LayoutParams.FILL_PARENT,
                  LayoutParams.WRAP_CONTENT));
		for (int i=0;i<6;++i)
		{
			TextView tv = new TextView(mainActivity);
			tv.setLayoutParams(new LayoutParams(
	                  LayoutParams.WRAP_CONTENT,
	                  LayoutParams.WRAP_CONTENT));
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(15);
			tv.setText(item.getStockItemValue(i));
			if (i!=3)
			{
				tr.addView(tv);
			}
		}
		
		allTableRows.add(tr);
		
		tableLayout.addView(tr);

		selectNewPosition(allTableRows.size()-1);
	}
	
	public void testTotalPrice()
	{
		try 
		{
			Double price=Double.valueOf(stockItems.get(currentSelection).getStockItemValue(4));
			Double quant=Double.valueOf(stockItems.get(currentSelection).getStockItemValue(5));
			
			if (!price.isNaN() && !quant.isNaN())
			{
				double total=price.doubleValue()*quant.doubleValue();
				
				if (total>100.0)
				{
					playBeep();
				}
			}
		}
		catch (NumberFormatException e)
		{
			Log.i("Linc","Price or quantity was not a number when testing values" + e.getMessage());
		}
	}
	
	public void itemUpdated(int index) {
		TableRow currentRow=allTableRows.get(index);
		for (int i=0;i<currentRow.getChildCount();++i)
		{
			TextView currentView=(TextView)currentRow.getChildAt(i);
			int textpos=i;
			if (i>2) {textpos++;} // this is because of missing "Category"
			currentView.setText(stockItems.get(index).getStockItemValue(textpos));
		}
		Time timest = new Time();
		timest.setToNow();
		stockItems.get(index).setTimestamp(timest.format2445());
		MyStockItem item=stockItems.get(index);
		
		dbAdapter.updateEntry(item.dbId, item.getStockItemValue(0),item.getStockItemValue(1),item.getStockItemValue(2),item.getStockItemValue(3),item.getStockItemValue(4),item.getStockItemValue(5),item.getStockItemValue(6),item.getStockItemValue(7),item.getStockItemValue(8));
		
		selectNewPosition(currentSelection); // to get the color back to standard selection color
		autoSave();
	}
	
	public MyStockItem getCurrentSelectedMyStockItem() {
		if (currentSelection>-1) {
			return stockItems.get(currentSelection);
		}
		return new MyStockItem();
	}
	

	private void playBeep () {
        try {
            mMediaPlayer = MediaPlayer.create(mainActivity, R.raw.beep8);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("beep", "error: " + e.getMessage(), e);
        }
    }
	
	public void playErrorBeep () {
        try {
            mMediaPlayer = MediaPlayer.create(mainActivity, R.raw.beep10);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("beep", "error: " + e.getMessage(), e);
        }
    }

    protected void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
   
}
