package ericools.LincScan;

import java.text.DecimalFormat;

import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;


public class MyEditText extends EditText
{
	
	/* 
	 * Handles all the editing and keyboard controls
	 */
	
	public TextView keyPressedView;
	public TextView totalsView;
	private Bundle keyboardSettings;
	
	private MyStockItem currentItem;
    private MyStockItemList itemList;
	private MyStockItemEditView myEditView;
	private int editingItem;
	
	private boolean isNewItem;
	
	private MyStockItem lastItem;
	
    public MyEditText(Context context) {
		super(context);
		lastItem=null;
		keyPressedView=null;
	}
    
    public void setEditView(MyStockItemEditView ev) {
		myEditView=ev;
	}
    
    
    public void setKeyBoardSettings(Bundle keyb) {
    	keyboardSettings=keyb;
    }
    
    public void reset() {
    	lastItem=null;
    	newMyStockItem();
    }

    public MyEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public void setMyStockItemList(MyStockItemList il) {
    	itemList=il;
    }
    
    public void newMyStockItem()
    {
    	currentItem=new MyStockItem();	
    	myEditView.setCurrentMyStockItem(currentItem);
    	if (lastItem==null)
    	{
    		currentItem.setCurrentFieldNumber(0);
    		myEditView.setActiveEditField(0);
    	}
    	else
    	{
    		for (int i=0;i<2;++i)
    		{
    			// populate first three fields
    			currentItem.setStockItemValue(i, lastItem.getStockItemValue(i));
    		}
    		myEditView.setActiveEditField(8);
    		myEditView.updateView();
    		currentItem.setCurrentFieldNumber(8);
    	}
    	this.setText("");
    	editingItem=-1; // no item is edited
    	isNewItem=true;
    }
    
    public void setCurrentMyStockItem (MyStockItem item) {
    	currentItem=item;
    	currentItem.setCurrentFieldNumber(5);
    	myEditView.setCurrentMyStockItem(currentItem);
    	myEditView.setActiveEditField(5);
    	this.setText("");
    	isNewItem=false;
    }
    
    public void refreshEditText() {
    	if (currentItem.getCurrentFieldNumber()<5) {
    		// if it's not the quantity field...
    		this.setText(currentItem.getStockItemValue(currentItem.getCurrentFieldNumber()));
    	}
    	else 
    	{
    		this.setText("");
    	}
		myEditView.updateView();
    }
    
    public void saveRecord() {
    	if (currentItem.getCurrentFieldNumber()==5) // only save if quantity is currently being edited
    	{	
        	// first set the timestamp (editing finished here)
    		Time timest = new Time();
    		timest.setToNow();
    		currentItem.setTimestamp(timest.format2445());
    		if (parseEntry(this.getText().toString())!=null)
        	{
    			currentItem.setCurrentField(parseEntry(this.getText().toString()),false); // also update field before entering data into list
        	}
        	if (isNewItem) {
        	   itemList.addMyStockItem(currentItem);
        	   itemList.testTotalPrice();
        	}
        	else {
        	   itemList.itemUpdated(editingItem);
        	   itemList.testTotalPrice();
        	}
        	lastItem=currentItem;
        	
        	// entry was added, also update totals at that moment
        	Double totals=itemList.getTotals(currentItem);
        	DecimalFormat f = new DecimalFormat("#0.00"); 
    		totalsView.setText(f.format(totals));
    		
        	newMyStockItem();
    	}
    }
    
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
		if (keyPressedView!=null)
		{
			String keyCodeAsString=Integer.valueOf(keyCode).toString();
			keyPressedView.setText(keyCodeAsString);
		}
    	
    	if (keyCode==keyboardSettings.getInt("RecordUp"))
    	{
    		if (isNewItem) // only allow arrows if not editing an item
    		{
    			itemList.selectPrevious();
    		}
    		return true;
    	}
    	
    	if (keyCode==keyboardSettings.getInt("RecordDown"))
    	{ 
    		if (isNewItem) // only allow arrows if not editing an item
    		{
    			itemList.selectNext();
    		}
    		return true;
    	}
    	
    	if (keyCode==keyboardSettings.getInt("EntryClearLast"))
    	{
    		if (currentItem.getCurrentFieldNumber()==5 && Double.parseDouble(currentItem.getStockItemValue(5))!=0.0)
    		{
    			Time timest = new Time();
	    		timest.setToNow();
	    		currentItem.setTimestamp(timest.format2445());
	    		if (parseEntry(this.getText().toString())!=null)
	        	{
	    			currentItem.setCurrentField(parseEntry(this.getText().toString()),false); // also update field before entering data into list
	        	}
	        	if (isNewItem) {
	        	   itemList.addMyStockItem(currentItem);
	        	   itemList.testTotalPrice();
	        	}
	        	else {
	        	   itemList.itemUpdated(editingItem);
	        	   itemList.testTotalPrice();
	        	}
	        	lastItem=currentItem;
	        	
	        	//update totals
	        	Double totals=itemList.getTotals(currentItem);
	        	DecimalFormat f = new DecimalFormat("#0.00"); 
	    		totalsView.setText(f.format(totals));
	    		
	        	newMyStockItem();
    		}
    		else
    		{
	    		// clear current field and select previous
	    		currentItem.clearCurrentField();
	    		currentItem.selectPreviousField();
	    		currentItem.clearCurrentField();
	    		myEditView.setActiveEditField(currentItem.getCurrentFieldNumber());
	    		refreshEditText();
    		}
    		return true;
    	}
    	
    	if (keyCode==keyboardSettings.getInt("Price"))
        {
        	
        	// save record
        	if (currentItem.getCurrentFieldNumber()==5 && Double.parseDouble(currentItem.getStockItemValue(5))!=0.0) // only save if quantity is currently being edited
        	{
        		if (parseEntry(this.getText().toString())!=null)
	        	{
        			String newprice=parseEntry(this.getText().toString());
		        	// first set the timestamp (editing finished here)
		    		Time timest = new Time();
		    		timest.setToNow();
		    		currentItem.setTimestamp(timest.format2445());
		        	if (isNewItem) {
		        	   itemList.addMyStockItem(currentItem);
		        	   itemList.testTotalPrice();
		        	}
		        	else {
		        	   itemList.itemUpdated(editingItem);
		        	   itemList.testTotalPrice();
		        	}
		        	lastItem=currentItem;
		        	
		        	//update totals
		        	Double totals=itemList.getTotals(currentItem);
		        	DecimalFormat f = new DecimalFormat("#0.00"); 
		    		totalsView.setText(f.format(totals));
		        	
		        	newMyStockItem();
		        	
		        	// now price is being edited, set the price to the entry when Price key was pressed
		        	currentItem.setCurrentField(newprice,false);
			    	currentItem.selectNextField();
			    	myEditView.setActiveEditField(currentItem.getCurrentFieldNumber());
			    	refreshEditText();
			    	
	        	}
        		else
        		{
        			itemList.playErrorBeep();
        			this.setText("");
        		}
        		return true;
        	}
        	if (currentItem.getCurrentFieldNumber()==4 || (currentItem.getCurrentFieldNumber()==5 && Double.parseDouble(currentItem.getStockItemValue(5))==0.0))
        	{
        		if (parseEntry(this.getText().toString())!=null)
	        	{
        			String newprice=parseEntry(this.getText().toString());
        			currentItem.setCurrentFieldNumber(4);
		        	currentItem.setCurrentField(newprice,false);
			    	currentItem.selectNextField();
			    	myEditView.setActiveEditField(currentItem.getCurrentFieldNumber());
			    	refreshEditText();	
	        	}
        		else
        		{
        			itemList.playErrorBeep();
        			this.setText("");
        		}
        		return true;
        	}
        	
        	return true;
        }
    	
        if (keyCode==keyboardSettings.getInt("EntrySaveAndNext")) 
        {
        	if (parseEntry(this.getText().toString())!=null)
        	{
		    	if (currentItem.setCurrentField(parseEntry(this.getText().toString()),false)) {
			    	currentItem.selectNextField();
			    	if (currentItem.getCurrentFieldNumber()==4) { // Department was changed, update totals
			    		Double totals=itemList.getTotals(currentItem);
			    		DecimalFormat f = new DecimalFormat("#0.00"); 
			    		totalsView.setText(f.format(totals));
			    	}
			    	myEditView.setActiveEditField(currentItem.getCurrentFieldNumber());
			    	refreshEditText();
		    	}
		    	else {
		    		// input not allowed
		    		refreshEditText();
		    	}
        	}
        	else
        	{
        		itemList.playErrorBeep();
        		this.setText("");
        	}
            return true;
        }
        
        if (keyCode==keyboardSettings.getInt("RecordSave"))
        {
        	
        	// save record
        	saveRecord();
        	return true;
        }
        
        if (keyCode==keyboardSettings.getInt("RecordEdit")){
        	// edit record
        	if (itemList.getCurrentSelection()>-1) {
        		editingItem=itemList.getCurrentSelection();
        		setCurrentMyStockItem(itemList.getCurrentSelectedMyStockItem());
        		itemList.activeEdit();
        	}
        	return true;
        }
        
        if (keyCode==keyboardSettings.getInt("RecordDelete")){
        	if (isNewItem)
        	{
        		// only delete item if it's not in editing mode at the moment
        		itemList.deleteCurrentMyStockItem();
        	}
        	return true;
        }
        // Handle all other keys in the default way
        return super.onKeyDown(keyCode, event);
    }
	
	private String parseEntry(String entrytext)
	{
		if (currentItem.getCurrentFieldNumber()==8) {
			return entrytext;
		}
		if (currentItem.getCurrentFieldNumber()<4)
		{
			if (entrytext.matches("^\\d+$"))
			{
				return entrytext;
			}
		}
		else
		{
			if (entrytext.matches("^[-]*\\d+$"))
			{
				return entrytext;
			}
			if (entrytext.matches("^\\d+[+]\\d+$"))
			{
				String[] numbers=entrytext.split("[+]");
				int result=Integer.parseInt(numbers[0])+Integer.parseInt(numbers[1]);
				return Integer.valueOf(result).toString();
			}
			if (entrytext.matches("^\\d+[-]\\d+$"))
			{
				String[] numbers=entrytext.split("[-]");
				int result=Integer.parseInt(numbers[0])-Integer.parseInt(numbers[1]);
				return Integer.valueOf(result).toString();
			}
			if (entrytext.matches("^\\d+[/]\\d+$"))
			{
				String[] numbers=entrytext.split("[/]");
				int result=Integer.parseInt(numbers[0])/Integer.parseInt(numbers[1]);
				return Integer.valueOf(result).toString();
			}
			if (entrytext.matches("^\\d+[*]+\\d+$"))
			{
				String[] numbers=entrytext.split("[*]");
				int result=Integer.parseInt(numbers[0])*Integer.parseInt(numbers[1]);
				return Integer.valueOf(result).toString();
			}
		}
		return null;
	}
}
