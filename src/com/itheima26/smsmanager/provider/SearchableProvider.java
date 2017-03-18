package com.itheima26.smsmanager.provider;

import com.itheima26.smsmanager.utils.Sms;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class SearchableProvider extends SearchRecentSuggestionsProvider {

	private final String AUTHORITY = "com.itheima26.smsmanager.provider.SearchableProvider";
	public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	private static final String TAG = "SearchableProvider";
	
	private final String[] sms_projection = {
			"_id",
			"address",
			"body"
	};
	
	private final String[] columnNames = {
			BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_QUERY
	};
	
	public SearchableProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// 查询短信数据库, 根据传递过来的字符串进行模糊查询
		Log.i(TAG, "用户输入的值: " + selectionArgs[0]);
		String where = "body like '%" + selectionArgs[0] + "%'";
		Cursor cursor = getContext().getContentResolver().query(Sms.SMS_URI, sms_projection, where, null, null);
		
		if(cursor != null && cursor.getCount() > 0) {
			
			/*
			 * 定义出matrix游标结果集, 并指定其列 
			 * _ID, 
			 * SUGGEST_COLUMN_TEXT_1(显示在上面: 号码), 
			 * SUGGEST_COLUMN_TEXT_2(显示在下面: 内容)
			 */
			MatrixCursor mc = new MatrixCursor(columnNames);
			
			String[] columnValues;
			while(cursor.moveToNext()) {
				columnValues = new String[columnNames.length];
				
				// 把当前cursor中的一行数据设置到columnValues. 添加给matrixCursor
				columnValues[0] = cursor.getString(0);
				columnValues[1] = cursor.getString(1);
				columnValues[2] = cursor.getString(2);
				// 设置当用户点击item时, 传递过去的参数为cursor中第2列: 短信的内容body
				columnValues[3] = cursor.getString(2);
				
				mc.addRow(columnValues);
			}
			return mc;
		}
		return null;
	}
	
	
}
