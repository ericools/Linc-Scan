package ericools.LincScan;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import ericools.LincScan.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LincActivity extends Activity {
	
	private Bundle keyboardSettings;
	private static MyStockItemList itemList;
	
	private static Context context;
	
	private static MyStockItemEditView myEditView;
	
	private static MyEditText myEditText;
	
	private static HashSet<Integer> allowedAreas;
	private static HashSet<Integer> allowedSections;
	private static HashSet<Integer> allowedDepartments;
	private static HashMap<String, SKUTuple<Integer, Double> > allowedSKUs;
    private static HashMap<Integer, String[]> departmentPrefixSuffix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.buttonScan).setOnClickListener(scanCode);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        itemList = new MyStockItemList(this);
        

        loadKeyboardSettings();
        
        myEditText = (MyEditText)findViewById(R.id.editText1);
    	TextView tv= (TextView)findViewById(R.id.textView2);
    	TextView totalstv= (TextView)findViewById(R.id.textViewTotals);
    	myEditView=new MyStockItemEditView(this);
    	myEditText.keyPressedView=tv;
    	myEditText.totalsView=totalstv;
    	myEditText.setKeyBoardSettings(keyboardSettings);
    	myEditText.setMyStockItemList(itemList);
    	myEditText.setEditView(myEditView);
    	myEditText.newMyStockItem();
    	myEditText.requestFocus();
       
        itemList.loadFromDB();
        
        context = this;
        
        allowedAreas = new HashSet<Integer>();
        allowedSections = new HashSet<Integer>();
        allowedDepartments = new HashSet<Integer>();
        
        allowedSKUs = new HashMap<String, SKUTuple<Integer, Double> >();

        departmentPrefixSuffix = new HashMap<Integer, String[]>();

        loadAllowedValues();
        loadSKUFile();
        loadDepartmentPrefixSuffix();
    };
    
    public static Context getActivity() {
        return context;
    }
    
    public static MyStockItemEditView getEditView() {
    	return myEditView;
    }
    
    public static MyStockItemList getStockItemList() {
    	return itemList;
    }
    
    public static MyEditText getEditText() {
    	return myEditText;
    }
    
    public static HashSet<Integer> getAllowedAreas() {
    	return allowedAreas;
    }
    
    public static HashSet<Integer> getAllowedSections() {
    	return allowedSections;
    }
    
    public static HashSet<Integer> getAllowedDepartments() {
    	return allowedDepartments;
    }
    
    public static HashMap<String, SKUTuple<Integer, Double > > getAllowedSKUs() {
    	return allowedSKUs;
    }
    
    protected void onDestroy() {
        super.onDestroy();
        itemList.close();
    }
    
    private void parseValueRanges(final String input, HashSet<Integer> set) {
    	// Parse a string of type "0-9,12,14,30-31"
		String in=input.replaceAll("\\s","");
		String[] temp=in.split(",");
		for (int i=0;i<temp.length;++i) {
			String[] temp2=temp[i].split("-");
			if (temp2.length>0) {
				set.add(Integer.valueOf(temp2[0]));
				if (temp2.length==2) {
					for (int j=Integer.valueOf(temp2[0]);j<Integer.valueOf(temp2[1]);++j) {
						set.add(j+1);
					}
				}
			}
		}
    }

    private final Button.OnClickListener scanCode = new Button.OnClickListener() {

      public void onClick(View v) {
    	  IntentIntegrator integrator = new IntentIntegrator(LincActivity.this);
          integrator.initiateScan();
      }
    };
    
    private void loadKeyboardStandard() {
    	/*
    	 * Load some standard keyboard bindings (only called if no saved settings found)
    	 */
    	String[] keyNames= {"RecordUp","RecordDown","RecordSave","RecordEdit","RecordDelete","EntryClearLast","EntrySaveAndNext","Price"};
	    int[] keyCodes= {19,20,61,73,76,74,66,71};
		keyboardSettings.putStringArray("KeyNames", keyNames);
		keyboardSettings.putIntArray("KeyCodes",keyCodes);
		for (int i=0;i<keyNames.length;++i)
		{
			keyboardSettings.putInt(keyNames[i], keyCodes[i]);
		}
		saveKeyboardSettings();
    }
    
    private void loadSKUFile() {
    	/*
    	 * Load allowed values for areas, sections, departments
    	 */
    	try {
    		File root = Environment.getExternalStorageDirectory();
    	    File listappDir = new File(root.toString() + "/LincScanData");
    	    listappDir.mkdirs();
    		File readfile = new File(listappDir + "/LincSKUFile.txt");
    		FileInputStream fis=new FileInputStream(readfile);
            BufferedInputStream  bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            String aString=dis.readLine();
            while(aString != null) {
            	// file contains: sku,description,department,price,quantity
            	String[] splitted=aString.split(",");
            	if (splitted.length==5) {
            		Integer depvalue=Integer.valueOf(splitted[2]);
            		Double pricevalue=Double.valueOf(splitted[3]);
            		SKUTuple<Integer, Double> tuple= new SKUTuple<Integer, Double>(depvalue, pricevalue);
            		allowedSKUs.put(splitted[0], tuple );
            	}
            	aString=dis.readLine();
            }
            fis.close();
            bis.close();
            dis.close();
		} catch (FileNotFoundException e) {
			loadKeyboardStandard();
		} catch (IOException e) {
			loadKeyboardStandard();
		}
    }
    
    private void loadAllowedValues() {
    	/*
    	 * Load allowed values for areas, sections, departments
    	 */
    	try {
    		File root = Environment.getExternalStorageDirectory();
    	    File listappDir = new File(root.toString() + "/LincScanData");
    	    listappDir.mkdirs();
    		File readfile = new File(listappDir + "/LincAllowedValues.txt");
    		FileInputStream fis=new FileInputStream(readfile);
            BufferedInputStream  bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            String aString=dis.readLine();
            if (aString!=null) {
            	parseValueRanges(aString,allowedAreas);
            }
            aString=dis.readLine();
            if (aString!=null) {
            	parseValueRanges(aString,allowedSections);
            }
            aString=dis.readLine();
            if (aString!=null) {
            	parseValueRanges(aString,allowedDepartments);
            }
            fis.close();
            bis.close();
            dis.close();
		} catch (FileNotFoundException e) {
			loadKeyboardStandard();
		} catch (IOException e) {
			loadKeyboardStandard();
		}
    }


    private void loadDepartmentPrefixSuffix() {
    	/*
    	 * Load prefix and suffix for a department as or if specified in department.csv
    	 */
    	Log.d("LincScan","Starting to load depatt. prefix/suffix values");
        try {
            File root = Environment.getExternalStorageDirectory();
            File listappDir = new File(root.toString() + "/LincScanData");
            listappDir.mkdirs();
            File readfile = new File(listappDir + "/department.csv");
            FileInputStream fis=new FileInputStream(readfile);
            BufferedInputStream  bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            String aString;

            // csv rows with bad / corrup data
            StringBuilder failedRows = new StringBuilder();
            int rowSrNo = 0;

            Log.d("LincScan", "department.csv found. reading rows one by one...");
            
            while ((aString=dis.readLine()) != null) {
                rowSrNo++;
                
                Log.d("LincScan", "Row " + rowSrNo + " : " + aString);
                String[] parts = aString.split(",");
                
                int departmentId = 0;
                String prefix = "";
                String suffix = "";
                
                if(parts.length >= 2) {
                	try {
                		// get departmentID
                		departmentId = Integer.parseInt(parts[0]);
                		
                		// get prefix
                		prefix = parts[1];

                		// optional, get suffix
                		if(parts.length >= 3) {
                			suffix = parts[2];
                		}
                		
                		departmentPrefixSuffix.put(
            				departmentId, 
            				new String[]{prefix, suffix}
                		);
                		
                		Log.d("LincScan", "row " + rowSrNo + ": parsed successfully!");
                		                		
                	} catch (Exception e) {
                		// numberformat exception: 1st col should be a number. found text.
                		addFailedRow(failedRows, rowSrNo, aString, "1st Col Type Found: Text. Required: Number.");
                        Log.d("LincScan", "row parsing failed : number format exception. 1st col has non-integer value");
                	}                	
                } else {
                    // the row has less than 2 cols, we require minimum 2 cols in a csv row
                    addFailedRow(failedRows, rowSrNo, aString, "Found cols: " + parts.length + ". Min. Required: 2.");
                    Log.d("LincScan", "row parsing failed : csv row should have at least 2 cols.");
                }
            }

            // do we have any failed rows ? if yes, report to user
            if(failedRows.length() > 0) {
            	Log.d("LincScan", "We have failed rows. Must show AlertDialog for the same.");
                showDepartmentPrefixSuffixAlertDialog("Following rows failed to load:\n" +  failedRows.toString());
            }
            
            fis.close();
            bis.close();
            dis.close();
        } catch (FileNotFoundException e) {
            // no file, no problem, ignore it.
            //showDepartmentPrefixSuffixAlertDialog();
        	Log.d("LincScan","No department.csv found... No prefix/suffix values loaded.");
        } catch (IOException e) {
        	Log.d("LincScan", "department.csv found... but not readable... must show a AlertDialog");
            showDepartmentPrefixSuffixAlertDialog("Error reading department.csv");
        }
    }
    
    private void addFailedRow(StringBuilder sb, int rowSrNo, String row, String errorMsg) {
    	sb.append(rowSrNo);
        sb.append(". ");
        sb.append(row);
        sb.append(" : ");
        sb.append(errorMsg);
        sb.append("\n");
    }

    private void showDepartmentPrefixSuffixAlertDialog(String errMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Loading Department Prefix Suffix values");

        builder.setMessage(errMsg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO: handle the OK
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void loadKeyboardSettings() {
    	/*
    	 * Load keyboard settings from file
    	 */
    	keyboardSettings = new Bundle();
    	try {
    		FileInputStream fis=openFileInput("LincKeyboardSettings_v2.txt");
            BufferedInputStream  bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
			String[] aSplitArray = null;
			String[] bSplitArray = null;
			String aString=dis.readLine();
			String bString=dis.readLine();
			if (aString!=null && bString!=null && aString.length()>0)
			{
				aSplitArray = aString.split(",");
				bSplitArray = bString.split(",");
				if (aSplitArray.length==bSplitArray.length)
				{
				   	String[] keyNames=new String[aSplitArray.length];
				   	int[] keyCodes=new int[aSplitArray.length];
				   	for (int i=0;i<aSplitArray.length;++i)
				   	{
				   		keyNames[i]=aSplitArray[i];
				   		keyCodes[i]=Integer.valueOf(bSplitArray[i]);
				   		keyboardSettings.putInt(keyNames[i], keyCodes[i]);
				   	}
					keyboardSettings.putStringArray("KeyNames", keyNames);
					keyboardSettings.putIntArray("KeyCodes",keyCodes);
				}
            }
            else 
            {
            	loadKeyboardStandard();
            }
            // dispose all the resources after using them.
            fis.close();
            bis.close();
            dis.close();
		} catch (FileNotFoundException e) {
			loadKeyboardStandard();
		} catch (IOException e) {
			loadKeyboardStandard();
		}
    }
    
    private void saveKeyboardSettings() {
    	/*
    	 * Save current keyboard settings to file
    	 */
    	try {
    		String[] keyNames=keyboardSettings.getStringArray("KeyNames");
    		int[] keyCodes=keyboardSettings.getIntArray("KeyCodes");
    		FileOutputStream fos=openFileOutput("LincKeyboardSettings_v2.txt", Context.MODE_PRIVATE);
    		for (int i=0;i<keyNames.length;++i)
    		{
    			fos.write(keyNames[i].getBytes());
    			if (i<keyNames.length-1)
    			{
    				fos.write(",".getBytes());
    			}
    		}
    		fos.write("\n".getBytes());
    		
    		for (int i=0;i<keyCodes.length;++i)
    		{
    			fos.write(Integer.valueOf(keyCodes[i]).toString().getBytes());
    			if (i<keyCodes.length-1)
    			{
    				fos.write(",".getBytes());
    			}
    		}
    		fos.write("\n".getBytes());
    		
    		fos.close();
    	}
    	catch (FileNotFoundException e) {
    		Log.e("Linc", "Could not write keyboard settings file.");
    	} catch (IOException e) {
    		Log.e("Linc", "Could not write keyboard settings file.");
		}
    	
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	/*
    	 * Create a menu if menu is pressed (from menu.xml)
    	 */
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	/*
    	 * What should happen for different menu options
    	 */
        switch (item.getItemId()) {
            case R.id.menuexport:
				MyEditText et = (MyEditText)findViewById(R.id.editText1);
				et.saveRecord();
				itemList.export();
                break;
            case R.id.keyboardsettings:
				Intent intent=new Intent(this, LincKeyboardSettings.class);
				intent.putExtras(keyboardSettings);
				startActivityForResult(intent, 0);
               break;
            case R.id.deletedata:
            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            	    
            	    public void onClick(DialogInterface dialog, int which) {
            	        switch (which){
            	        case DialogInterface.BUTTON_POSITIVE:
            	        	File root = Environment.getExternalStorageDirectory();
            			    File listappDir = new File(root.toString() + "/LincScanData");
            	        	if (listappDir.isDirectory()) {
            	                String[] children = listappDir.list();
            	                for (int i = 0; i < children.length; i++) {
            	                    new File(listappDir, children[i]).delete();
            	                }
            	            }
            	        	
            	        	// load from non-existent autosave (to delete all entries in current list)
            	        	itemList.deleteDB();
            	        	MyEditText et = (MyEditText)findViewById(R.id.editText1);
            	        	et.reset();
            	            break;
            	        }
            	    }
            	};

            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
            	    .setNegativeButton("No", dialogClickListener).show();
               break;
        }
        return true;
    }

    public static boolean isDepartmentPrefixSuffixValuesLoaded() {
        return (! departmentPrefixSuffix.isEmpty());
    }
    
    private void applyDepartmentPrefixSuffix(String contents) {
    	TextView inputDept = (TextView)findViewById(R.id.inputDepartment);
    	TextView inputSku = (TextView)findViewById(R.id.inputSKU);
    	String prefix, suffix, sku;
    	String[] item;
    	
    	Log.d("LincScan", "Applying prefix/suffix to SKU, if applicable, as per the Department.");
    	try {
    		int deptId = Integer.parseInt(inputDept.getText().toString());
    		
    		if(departmentPrefixSuffix.containsKey(deptId)) {
    			Log.d("LincScan", "Yep, We have prefix/suffix value(s) for department: " + deptId);
    		
    			item = departmentPrefixSuffix.get(deptId);
    			prefix = item[0];
    			suffix = item[1];
    			sku = prefix + contents + suffix;
    			inputSku.setText(sku);
    		
    			Log.d("LincScan", "Prefix/Suffix applied to SKU as - preifx: " + prefix + ", barcode: " + contents + ", suffix: " + suffix);
    			   		
    		} else {
    			Log.d("LincScan", "No prefix/suffix value(s) found for department: " + deptId);    			
    		};
    		
    	} catch (Exception e) {
    		// departmentId should be a number, but found text
    		String errorMsg = "DepartmentID should be Number. Found text. Not applying any prefix/suffix."; 
            Log.d("LincScan", errorMsg);
    		showDepartmentPrefixSuffixAlertDialog(errorMsg);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	/*
    	 * Called if settings Activity is finished
    	 * Update keyboard settings also in edit text class
    	 */
    	if (requestCode==IntentIntegrator.REQUEST_CODE) {
    	 IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
         if (result != null) {
           String contents = result.getContents();
           if (contents != null) {
        	   MyEditText et = (MyEditText)findViewById(R.id.editText1);
        	   // et.setText(result.getContents());
        	   et.setText(contents);
        	   applyDepartmentPrefixSuffix(contents);
           }
         }
    	}
    	else {
	        if(resultCode==RESULT_OK){
	            Toast.makeText(this, "Keyboard settings saved", Toast.LENGTH_LONG).show();
	            keyboardSettings=data.getExtras();
	            MyEditText et = (MyEditText)findViewById(R.id.editText1);
	            et.setKeyBoardSettings(keyboardSettings);
	            saveKeyboardSettings();
	        }
    	}
    }
}
