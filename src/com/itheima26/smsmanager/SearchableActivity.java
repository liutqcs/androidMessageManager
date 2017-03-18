package com.itheima26.smsmanager;

import com.itheima26.smsmanager.ConversationUI.ConversationHolderView;
import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.Sms;
import com.itheima26.smsmanager.utils.Utils;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchableActivity extends ListActivity {

	private static final String TAG = "SearchableActivity";
	
	private final String[] projection = {
			"thread_id as _id",
			"address",
			"date",
			"body"
	};
	
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int BODY_COLUMN_INDEX = 3;

	private String query;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("搜索结果");
		Intent intent = getIntent();
		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			Log.i(TAG, "需要查询的字符串: " + query);
			
			// 开始搜索, 列出结果
			init();
		}
	}
	
	private void init() {
		ListView mListView = getListView();
		
		SearchableAdapter mAdapter = new SearchableAdapter(this, null);
		mListView.setAdapter(mAdapter);
		
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		String selection = "body like '%" + query + "%'";
		asyncQuery.startQuery(0, mAdapter, Sms.SMS_URI, projection, selection, null, "date desc");
	}

	class SearchableAdapter extends CursorAdapter {
		
		private SearchableHolderView mHolder;

		public SearchableAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder = new SearchableHolderView();
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_conversation_item_body);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (SearchableHolderView) view.getTag();
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				// 显示号码
				mHolder.tvName.setText(address);
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				// 显示名称
				mHolder.tvName.setText(contactName);
				
				Bitmap contactIcon = Utils.getContactIcon(getContentResolver(), address);
				if(contactIcon != null) {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				} else {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				}
			}
			
			String strDate = null;
			if(DateUtils.isToday(date)) {
				// 显示时间
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				// 显示日期
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);
			
			mHolder.tvBody.setText(body);
		}
	}
	

	public class SearchableHolderView {
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}
}
