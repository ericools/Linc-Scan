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
	
    public static final int ENTRY_AREA      = 0;
    public static final int ENTRY_SECTION   = 1;
    public static final int ENTRY_DEPARTMENT = 2;
    public static final int ENTRY_CATEGORY  = 3;
    public static final int ENTRY_PRICE     = 4;
    public static final int ENTRY_QUATITY   = 5;
    public static final int ENTRY_USER      = 6;
    public static final int ENTRY_TIMESTAMP = 7;
    public static final int ENTRY_SKU       = 8;
    public static final int ENTRY_PREFIX = 9;
    public static final int ENTRY_SUFFIX = 10;

	public static String[] entryCaptions = {"Area","Section","Department","Category","Price","Quantity","User","Timestamp","SKU", "Prefix","Suffix"};
	
	public long dbId;
	
	public MyStockItem(){
		stockItemValues=new ArrayList<String>();
		for (int i=MyStockItem.ENTRY_AREA; i < MyStockItem.ENTRY_SUFFIX+1; ++i)
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
			 * 9 : Prefix 
			 * 10 : Suffix 
			 */
			stockItemValues.add("");
		}
		stockItemValues.set(MyStockItem.ENTRY_CATEGORY, "0000");
		stockItemValues.set(MyStockItem.ENTRY_QUATITY, "0");
		stockItemValues.set(MyStockItem.ENTRY_USER, "0000");
		counter=0;
		quantity=0;
		dbId=-1;
	}
	
	public void setTimestamp(String times) {
		stockItemValues.set(MyStockItem.ENTRY_TIMESTAMP, times);
	}
	
	public void selectNextField() {
        Log.d("MyStockItem::selectNextField", "Counter: " + counter);
        switch(counter) {
            case MyStockItem.ENTRY_SECTION:
                // from SKU:
                // - goto department, if department prefix/suffix values are loaded
                // - else, jump to SKU 
                if(LincActivity.isDepartmentPrefixSuffixValuesLoaded()) {
                    setCurrentFieldNumber(MyStockItem.ENTRY_DEPARTMENT);
                } else {
                    setCurrentFieldNumber(MyStockItem.ENTRY_SKU);
                }
            break;

            case MyStockItem.ENTRY_DEPARTMENT:
                // jump to SKU after Department
                Log.d("MyStockItem::selectNextField", 
                      "On Department jumping to SKU");
                setCurrentFieldNumber(MyStockItem.ENTRY_SKU);
            break;

            case MyStockItem.ENTRY_SKU:
                // jump to price after SKU
                setCurrentFieldNumber(MyStockItem.ENTRY_PRICE);
            break;

            default:
                setCurrentFieldNumber(counter+1);
        }
	}

	public void selectPreviousField() {
        switch(counter) {
            case MyStockItem.ENTRY_SKU:
                // from SKU:
                // - go back to department, if prefix/suffix values loaded,
                // - else go back to section 
                if(LincActivity.isDepartmentPrefixSuffixValuesLoaded()) {
                    setCurrentFieldNumber(MyStockItem.ENTRY_DEPARTMENT);
                } else {
                    setCurrentFieldNumber(MyStockItem.ENTRY_SECTION);
                }
                break;

            case MyStockItem.ENTRY_PRICE:
                // go back to SKU after price
                setCurrentFieldNumber(MyStockItem.ENTRY_SKU);
                break;

            default:
				setCurrentFieldNumber(counter-1);
        }
	}
	
	public void setCurrentFieldNumber(int number)
	{
        Log.d("MyStockItem::setCurrentFieldNumber", 
              "Request received to set current field number to: " + number);
		if (number>-1 && (number < MyStockItem.ENTRY_USER || number == MyStockItem.ENTRY_SKU)) {
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
		if (counter < MyStockItem.ENTRY_QUATITY || counter == MyStockItem.ENTRY_SKU)
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
        Log.d("MyStockItem::setCurrentField",
              "Request to set current" +
              " field: " + counter + 
              " to: " + input +
              " with override: " + override);

		if (input != null && input.length()>0)
		{
			if (counter < MyStockItem.ENTRY_PRICE || counter == MyStockItem.ENTRY_SKU)
			{
				switch (counter) {
				case MyStockItem.ENTRY_AREA:
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

				case MyStockItem.ENTRY_SECTION:
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

                case MyStockItem.ENTRY_DEPARTMENT: 
                    Log.d("MyStockItem::setCurrentField",
                          "Entering input into department field and then moving to SKU field.");
                    stockItemValues.set(counter, input);
                    // department field updated...also check for prefix/suffix
                    applyDepartmentPrefixSuffix();
                    //selectNextField();
                    break;

				case MyStockItem.ENTRY_SKU:
					if (LincActivity.getAllowedSKUs().containsKey(input)) {
						stockItemValues.set(counter, input);
						stockItemValues.set(
                                MyStockItem.ENTRY_DEPARTMENT, 
                                LincActivity.getAllowedSKUs().get(input).x.toString()
                                );
						stockItemValues.set(
                                MyStockItem.ENTRY_PRICE, 
                                LincActivity.getAllowedSKUs().get(input).y.toString()
                                );
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
                                Log.d("MyStockItem","Dept. prefix/suffix values are loaded... " +
                                      "hence dept. will be left untouched.");
                            } else {
                                Log.d("MyStockItem","Dept. prefix/suffix values NOT loaded... " + 
                                      "hence dept. will be set to 0.");
                                stockItemValues.set(MyStockItem.ENTRY_DEPARTMENT, "0");
                            }

							stockItemValues.set(MyStockItem.ENTRY_PRICE, "");
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

    // reads current department value 
    // checks if some prefix/suffix value exists for this department in presets
    // if yes sets prefix/suffix to relevant fields
    private void applyDepartmentPrefixSuffix() {
    	String[] item;
        String dept = getStockItemValue(MyStockItem.ENTRY_DEPARTMENT);

    	Log.d("LincScan", "Applying prefix/suffix to SKU, if applicable, as per the Department.");

        if(LincActivity.departmentPrefixSuffix.containsKey(dept)) {
            Log.d("LincScan", "Yep, We have prefix/suffix value(s) for department: " + dept);

            item = LincActivity.departmentPrefixSuffix.get(dept);
            stockItemValues.set(MyStockItem.ENTRY_PREFIX, item[0]);
            stockItemValues.set(MyStockItem.ENTRY_SUFFIX, item[1]);

            Log.d("LincScan", "Prefix/Suffix field set as - preifx: " + getStockItemValue(MyStockItem.ENTRY_PREFIX) + 
                                                         ", suffix: " + getStockItemValue(MyStockItem.ENTRY_SUFFIX));
        } else {
            stockItemValues.set(MyStockItem.ENTRY_PREFIX, "");
            stockItemValues.set(MyStockItem.ENTRY_SUFFIX, "");
            Log.d("LincScan", "No prefix/suffix value(s) found for department: " + dept);    			
        };
    }

}
