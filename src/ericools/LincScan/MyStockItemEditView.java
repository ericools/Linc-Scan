package ericools.LincScan;

import java.util.ArrayList;

import ericools.LincScan.R;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class MyStockItemEditView {
	
	/* 
	 * Display of the record currently edited
	 */
	
	private ArrayList<TextView> fields;
	private ArrayList<TextView> fieldsDescriptions;
	
	private MyStockItem currentItem;
	
	public MyStockItemEditView(LincActivity activity) {
		fields = new ArrayList<TextView>();
		fieldsDescriptions = new ArrayList<TextView>();
    	fields.add( (TextView)activity.findViewById(R.id.inputArea) );
    	fields.add( (TextView)activity.findViewById(R.id.inputSection) );
    	fields.add( (TextView)activity.findViewById(R.id.inputDepartment) );
    	fields.add( (TextView)activity.findViewById(R.id.inputCategory) );
    	fields.add( (TextView)activity.findViewById(R.id.inputPrice) );
    	fields.add( (TextView)activity.findViewById(R.id.inputQuantity) );
    	fields.add( (TextView)activity.findViewById(R.id.inputSKU) );
    	
    	
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descArea) );
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descSection) );
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descDepartment) );
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descCategory) );
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descPrice) );
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descQuantity) );
    	fieldsDescriptions.add( (TextView)activity.findViewById(R.id.descSKU) );
    	
    	// Field 3 is reserved for Category and set to invisible
    	
    	fields.get(3).setVisibility(View.INVISIBLE);
    	fields.get(3).setMaxHeight(0);
    	fieldsDescriptions.get(3).setVisibility(View.INVISIBLE);
    	fieldsDescriptions.get(3).setMaxHeight(0);
	}
	
	public void updateView() {
		for (int i=0;i<6;++i)
		 {
	    	fields.get(i).setText(currentItem.getStockItemValue(i));
	     }
		// the sixth edit field is the SKU field (8)
		fields.get(6).setText(currentItem.getStockItemValue(8));
	}
	
	public void setActiveEditField(int fieldno) {
		for (int i=0;i<7;++i)
		 {
	    	fieldsDescriptions.get(i).setTextColor(Color.LTGRAY);
	     }	
		if (fieldno==8) {
			fieldsDescriptions.get(6).setTextColor(Color.RED);
		} else {
			fieldsDescriptions.get(fieldno).setTextColor(Color.RED);
		}
		
	}
	
	public void setCurrentMyStockItem (MyStockItem item){
		currentItem=item;
		updateView();
	}
}
