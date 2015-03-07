package ericools.LincScan;

import ericools.LincScan.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


public class LincKeyboardSettings extends Activity {
	
	private class MyOnKeyListener implements OnKeyListener {

		public TextView textToSet;
		
		public boolean onKey(View v, int keyCode, KeyEvent event) {
		    if (keyCode!=KeyEvent.KEYCODE_BACK)
			    {
				String keyCodeAsString=Integer.valueOf(keyCode).toString();
		    	textToSet.setText(keyCodeAsString);
		    	return true;
			    }
		    return false;
		}
	}
	
	private void testAndSave()
	{
		TableLayout tl=(TableLayout)findViewById(R.id.settingsTableLayout1);
		 String[] keyNames=keyboardSettings.getStringArray("KeyNames");
		 for (int i=0;i<keyNames.length;++i) {
			 int key = Integer.valueOf(((TextView) ((TableRow) tl.getChildAt(i)).getChildAt(1)).getText().toString());
			 for (int j=0;j<keyNames.length;++j) {
				 if (i!=j)
				 {
					 int keycomp = Integer.valueOf(((TextView) ((TableRow) tl.getChildAt(j)).getChildAt(1)).getText().toString());
					 if (key==keycomp)
					 {
						 Toast toast = Toast.makeText(this, "Two keys are the same!", Toast.LENGTH_LONG);
						 toast.show();
						 return;
					 }
					 
				 }
			 }
		 }
		 
		 int[] keyCodes=keyboardSettings.getIntArray("KeyCodes");
		 
		 for (int i=0;i<keyNames.length;++i) {
			 int key = Integer.valueOf(((TextView) ((TableRow) tl.getChildAt(i)).getChildAt(1)).getText().toString());
			 keyCodes[i]=key;
			 keyboardSettings.putInt(keyNames[i], key);
		 }
		 keyboardSettings.putIntArray("KeyCodes",keyCodes);
		 
		 Intent in = new Intent();
		 in.putExtras(keyboardSettings);
	     
		 setResult(RESULT_OK,in);
	     
		 this.finish();
	}
	
	
	 private Bundle keyboardSettings;
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.linckeyboardsettings);
		 keyboardSettings=getIntent().getExtras();
		 TableLayout tl=(TableLayout)findViewById(R.id.settingsTableLayout1);
		 String[] keyNames=keyboardSettings.getStringArray("KeyNames");
		 for (int i=0;i<keyNames.length;++i)
		 {
			 TableRow tr = new TableRow(this);
				tr.setLayoutParams(new LayoutParams(
		                  LayoutParams.FILL_PARENT,
		                  LayoutParams.WRAP_CONTENT));
					
					TextView tv = new TextView(this);
					tv.setLayoutParams(new LayoutParams(
			                LayoutParams.FILL_PARENT,
			                LayoutParams.WRAP_CONTENT));
					tv.setTextColor(Color.WHITE);
					tv.setClickable(true);
					tv.setFocusable(false);
					tv.setFocusableInTouchMode(true);
					
					
					tv.setOnFocusChangeListener(new OnFocusChangeListener() {
						
						public void onFocusChange(View v, boolean hasFocus) {
							if (hasFocus)
							{
								((TextView) v).setTextColor(Color.BLUE);
							}
							else
							{
								((TextView) v).setTextColor(Color.WHITE);
							}
						}
					});
					tv.setTextSize(20);
					tv.setText(keyNames[i] + "   ");
					
					
					TextView tv2 = new TextView(this);
					tv2.setLayoutParams(new LayoutParams(
			                LayoutParams.WRAP_CONTENT,
			                LayoutParams.WRAP_CONTENT));
					
					tv2.setText(Integer.valueOf(keyboardSettings.getInt(keyNames[i])).toString());
					tv2.setTextSize(20);
					MyOnKeyListener keylist=new MyOnKeyListener();
					keylist.textToSet=tv2;
					tv.setOnKeyListener(keylist);
					
					tr.addView(tv);
					tr.addView(tv2);
					
				
					tl.addView(tr);
		 }
		 Button but=new Button(this);
			but.setText("Save");
			but.setLayoutParams(new LayoutParams(
	                LayoutParams.WRAP_CONTENT,
	                LayoutParams.WRAP_CONTENT));
			but.setGravity(Gravity.CENTER);
			but.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					testAndSave();
				}
			});
		 tl.addView(but);
	 }
}
