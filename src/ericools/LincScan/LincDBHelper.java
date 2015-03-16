package ericools.LincScan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LincDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "lincscandata";

	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_CREATE = "create table inventory_entries (_id integer primary key autoincrement, "
			+ "area text not null, " 
			+ "section text not null, " 
			+ "department text not null, " 
			+ "category text not null, " 
			+ "price text not null, " 
			+ "quantity text not null, " 
			+ "user text not null, " 
			+ "timestamp text not null, "
			+ "sku text not null, "
			+ "prefix text not null, "
			+ "suffix text not null);";

	public LincDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(LincDBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS inventory_entries");
		onCreate(database);
	}
}
