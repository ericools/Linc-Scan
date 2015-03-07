package ericools.LincScan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LincDBAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_AREA = "area";
	public static final String KEY_SECTION = "section";
	public static final String KEY_DEPARTMENT = "department";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_PRICE = "price";
	public static final String KEY_QUANTITY = "quantity";
	public static final String KEY_USER = "user";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_SKU = "sku";
	private static final String DATABASE_TABLE = "inventory_entries";
	private Context context;
	private SQLiteDatabase database;
	private LincDBHelper dbHelper;

	public LincDBAdapter(Context context) {
		this.context = context;
	}

	public LincDBAdapter open() throws SQLException {
		dbHelper = new LincDBHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public long createEntry(String area, String section, String department, String category, String price, String quantity, String user, String timestamp, String sku) {
		ContentValues initialValues = createContentValues(area, section, department, category, price, quantity, user, timestamp, sku);

		return database.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean updateEntry(long rowId, String area, String section, String department, String category, String price, String quantity, String user, String timestamp, String sku) {
		ContentValues updateValues = createContentValues(area, section, department, category, price, quantity, user, timestamp, sku);

		return database.update(DATABASE_TABLE, updateValues, KEY_ROWID + "="
				+ rowId, null) > 0;
	}

	public boolean deleteAllEntries() {
		return database.delete(DATABASE_TABLE, null, null) > 0;
	}
	
	public boolean deleteEntry(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllEntries() {
		return database.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_AREA, KEY_SECTION, KEY_DEPARTMENT, KEY_CATEGORY, KEY_PRICE, KEY_QUANTITY, KEY_USER, KEY_TIMESTAMP, KEY_SKU }, null, null, null,
				null, null);
	}
	
	public double getTotals(String area, String section, String department) {
		if (area.length()>0 && section.length()>0 && department.length()>0) {
			String query="SELECT SUM(" + KEY_PRICE + "*" + KEY_QUANTITY + ") FROM " + DATABASE_TABLE + " WHERE " + KEY_AREA + "=" + area.replaceFirst("^0+(?!$)", "") + " AND " + KEY_SECTION + "=" + section.replaceFirst("^0+(?!$)", "") + " AND " + KEY_DEPARTMENT + "=" + department.replaceFirst("^0+(?!$)", "") + ";";
			Cursor cursor=database.rawQuery(query,null);
			if (cursor.moveToFirst()) {
				return cursor.getDouble(0);
			}
		}
		return 0.0;
	}

	public Cursor fetchEntry(long rowId) throws SQLException {
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_AREA, KEY_SECTION, KEY_DEPARTMENT, KEY_CATEGORY, KEY_PRICE, KEY_QUANTITY, KEY_USER, KEY_TIMESTAMP, KEY_SKU },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(String area, String section, String department, String category, String price, String quantity, String user, String timestamp, String sku) {
		ContentValues values = new ContentValues();
		values.put(KEY_AREA, area.replaceFirst("^0+(?!$)", "")); // make sure there are no leading zeros in database
		values.put(KEY_SECTION, section.replaceFirst("^0+(?!$)", ""));
		values.put(KEY_DEPARTMENT, department.replaceFirst("^0+(?!$)", ""));
		values.put(KEY_CATEGORY, category.replaceFirst("^0+(?!$)", ""));
		values.put(KEY_PRICE, price);
		values.put(KEY_QUANTITY, quantity);
		values.put(KEY_USER, user.replaceFirst("^0+(?!$)", ""));
		values.put(KEY_TIMESTAMP, timestamp);
		values.put(KEY_SKU, sku);
		return values;
	}
}
