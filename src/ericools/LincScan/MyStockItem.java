package ericools.LincScan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class MyStockItem {
	
	/* 
	 * Single record object
	 */

	private int quantity;
	private ArrayList<String> stockItemValues;
	private int counter;
	
	public static String[] entryCaptions = {"Area","Section","Department","Category","Price","Quantity","User","Timestamp","SKU"};
	
	public long dbId;
	
	public MyStockItem(){
		stockItemValues=new ArrayList<String>();
		for (int i=0;i<9;++i)
		{
			/*
			 * 0 : Area
			 * 1 : Section
			 * 2 : Department
			 * 3 : Category
			 * 4 : Price
			 * 5 : Quantity
			 * 6 : User
			 * 7 : Timestamp
			 * 8 : SKU
			 */
			stockItemValues.add("");
		}
		stockItemValues.set(3, "0000");
		stockItemValues.set(5, "0");
		stockItemValues.set(6, "0000");
		counter=0;
		quantity=0;
		dbId=-1;
	}
	
	public void setTimestamp(String times) {
		stockItemValues.set(7,times);
	}
	
	public void selectNextField() {
		if (counter==1)
		{
			// jump right to SKU after section
			setCurrentFieldNumber(8);
		}
		else { 
			if (counter==8) {
				// jump to price after SKU
				setCurrentFieldNumber(4);
			}
			else {
				setCurrentFieldNumber(counter+1);
			}
		}
	}

	public void selectPreviousField() {
		if (counter==8)
		{
			// go back to Section after SKU
			setCurrentFieldNumber(1);
		}
		else {
			if (counter==4) {
				// go back to SKU after price
				setCurrentFieldNumber(8);
			}
			else {
				setCurrentFieldNumber(counter-1);
			}
		}
	}
	
	public void setCurrentFieldNumber(int number)
	{
		if (number>-1 && (number<6 || number==8)) {
			counter=number;
		}
	}
	
	public void setQuantity(int quant) {
		this.quantity=quant;
	}
	
	public int getCurrentFieldNumber()
	{
		return counter;
	}
	
	public void clearCurrentField() {
		if (counter<5 || counter==8)
		{
			stockItemValues.set(counter, "");
		}
		else
		{
			stockItemValues.set(counter, "0");
			this.quantity=0;
		}
	}
	
	private void showAcceptDialog(final String input) {
		LincActivity.getStockItemList().playErrorBeep();
		DialogInterface.OnClickListener listenerAccept = new DialogInterface.OnClickListener() {
			 
			public void onClick(DialogInterface dialog, int which) {
				setCurrentField(input,true);
				selectNextField();
				LincActivity.getEditView().setActiveEditField(getCurrentFieldNumber());
				LincActivity.getEditText().refreshEditText();
				Toast.makeText(LincActivity.getActivity(), "Value accepted", Toast.LENGTH_SHORT).show();
			}
		};
	 
		DialogInterface.OnClickListener listenerDoesNotAccept = new DialogInterface.OnClickListener() {
	 
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(LincActivity.getActivity(), "Enter a new value", Toast.LENGTH_SHORT).show();
			}
		};
		
		//LayoutInflater li = LayoutInflater.from(LincApp.getAppContext());
		//View promptsView = li.inflate(R.layout.promptoutofrange, null);
		Builder builder = new AlertDialog.Builder(LincActivity.getActivity());
		AlertDialog dialog = builder.create();
		dialog.setTitle("Value not in pre-defined list");
		dialog.setMessage("Do you want to enter this value nonetheless?");
		dialog.setButton("Yes",listenerAccept);
		dialog.setButton2("No", listenerDoesNotAccept);
		dialog.setCancelable(false);
		dialog.show();
	}
	
	public boolean setCurrentField(final String input, boolean override){
		if (input != null && input.length()>0)
		{
			if (counter<4 || counter==8)
			{
				switch (counter) {
				case 0:
					if (LincActivity.getAllowedAreas().contains(Integer.valueOf(input))) {
						stockItemValues.set(counter, input);
					} else {
						if (override) {
							stockItemValues.set(counter, input);
							return true;
						} else {
							showAcceptDialog(input);
							return false;
						}
					}
					break;
				case 1:
					if (LincActivity.getAllowedSections().contains(Integer.valueOf(input))) {
						stockItemValues.set(counter, input);
					} else {
						if (override) {
							stockItemValues.set(counter, input);
							return true;
						} else {
							showAcceptDialog(input);
							return false;
						}
					}
					break;
				case 8:
					if (LincActivity.getAllowedSKUs().containsKey(input)) {
						stockItemValues.set(counter, input);
						stockItemValues.set(2,LincActivity.getAllowedSKUs().get(input).x.toString());
						stockItemValues.set(4,LincActivity.getAllowedSKUs().get(input).y.toString());
						selectNextField(); // skip one field more (price field)
					} else {
						if (override) {
							stockItemValues.set(counter, input);

                            // If there is no 'department.csv' present and loaded...
                            // which means no department prefix/suffix values loaded.
                            // hence there is no need for department and it should be set to 0
                            // But if the file exits and successfully parsed, we need department value,
                            // hence don't touch it and let the user fill it, like other values.
                            if(LincActivity.isDepartmentPrefixSuffixValuesLoaded()) {
                                Log.d("MyStockItem","Dept. prefix/suffix values are loaded... hence dept. will be left untouched.");
                            } else {
                                Log.d("MyStockItem","Dept. prefix/suffix values NOT loaded... hence dept. will be set to 0.");
                                stockItemValues.set(2,"0");
                            }

							stockItemValues.set(4,"");
							return true;
						} else {
							showAcceptDialog(input);
							return false;
						}
					}
					break;
				default:
					stockItemValues.set(counter, input);
				}
			}
			else if (counter==4)
			{
				double price=Double.parseDouble(input)/100.0;
				stockItemValues.set(counter, Double.valueOf(price).toString());
			}
			else
			{
                try
                {
                    int newvalue=this.quantity+Integer.parseInt(input);
                    this.quantity=newvalue;
                    stockItemValues.set(5,Integer.valueOf(newvalue).toString());
                } 
                catch (NumberFormatException e) 
                { 
                    Log.i("LincScan", "entered number not integer"); 
                }
            }
        }
		return true;
	}
	
    public String getStockItemValue(int index) {
    	return this.stockItemValues.get(index);
    }
    
    public void setStockItemValue(int index, String str) {
    	if (index>=0 && str != null)
		{
    		this.stockItemValues.set(index, str);
		}
    }
}
