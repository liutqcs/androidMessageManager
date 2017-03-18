package com.itheima26.smsmanager;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.CommonAsyncQuery.OnQueryNotifyCompleteListener;
import com.itheima26.smsmanager.utils.Utils;

public class FolderDetailUI extends Activity implements 
	OnQueryNotifyCompleteListener, OnItemClickListener,OnClickListener {

	private int index;
	private FolderDetailAdapter mAdapter;
	private final String[] projection = {
			"_id",
			"address",
			"date",
			"body"
	};
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int BODY_COLUMN_INDEX = 3;
	
	private HashMap<Integer, String> dateMap;		// 日期集合
	private HashMap<Integer, Integer> smsRealPositionMap;	// 短信真实索引的集合

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_detail);
		
		Intent intent = getIntent();
		
		index = intent.getIntExtra("index", -1);
		
		initTitle();
		initView();
		prepareData();
	}

	private void initView() {
		dateMap = new HashMap<Integer, String>();
		smsRealPositionMap = new HashMap<Integer, Integer>();
		
		ListView mListView = (ListView) findViewById(R.id.lv_folder_detail_sms);
		Button btn_new_sms =(Button) findViewById(R.id.btn_folder_detail_new_message);
		btn_new_sms.setOnClickListener(this);
		mAdapter = new FolderDetailAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	private void prepareData() {
		Uri uri = Utils.getUriFromIndex(index);
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		asyncQuery.startQuery(0, mAdapter, uri, projection, null, null, "date desc");
	}

	private void initTitle() {
		switch (index) {
		case 0:
			setTitle("收件箱");
			break;
		case 1:
			setTitle("发件箱");
			break;
		case 2:
			setTitle("已发送");
			break;
		case 3:
			setTitle("草稿箱");
			break;
		default:
			break;
		}
	}
	
	class FolderDetailAdapter extends CursorAdapter {
		
		private FolderDetailHolderView mHolder;

		public FolderDetailAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			return dateMap.size() + smsRealPositionMap.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 当position可以在日期集合中取到日期时, 返回的是TextView
			if(dateMap.containsKey(position)) {		// 当前需要显示日期
				TextView tvDate = new TextView(FolderDetailUI.this);
				tvDate.setBackgroundResource(android.R.color.darker_gray);
				tvDate.setTextSize(20);
				tvDate.setTextColor(Color.WHITE);
				tvDate.setGravity(Gravity.CENTER);
				tvDate.setText(dateMap.get(position));
				return tvDate;
			}
			
			// 返回的是短信的item
			Cursor mCursor = mAdapter.getCursor();
			mCursor.moveToPosition(smsRealPositionMap.get(position));
			
	        View v;
	        if (convertView == null || convertView instanceof TextView) {
	            v = newView(FolderDetailUI.this, mCursor, parent);
	        } else {
	            v = convertView;
	        }
	        bindView(v, FolderDetailUI.this, mCursor);
	        return v;
	    }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder = new FolderDetailHolderView();
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_conversation_item_body);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (FolderDetailHolderView) view.getTag();
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				mHolder.tvName.setText(address);
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				mHolder.tvName.setText(contactName);
				
				Bitmap contactIcon = Utils.getContactIcon(getContentResolver(), address);
				if(contactIcon == null) {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				} else {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				}
			}
			
			String strDate = null;
			if(DateUtils.isToday(date)) {
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);
			
			mHolder.tvBody.setText(body);
		}
		
	}
	
	public class FolderDetailHolderView {
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0) {
			
			java.text.DateFormat dateFormat = DateFormat.getDateFormat(this);
			int listViewIndex = 0;	// listview的索引
			
			String strDate;
			
			while(cursor.moveToNext()) {
				// 格式化后的日期
				strDate = dateFormat.format(cursor.getLong(DATE_COLUMN_INDEX));
				
				// 判断当前短信的日期, 是否存在日期集合中, 如果不存在, 存一份
				if(!dateMap.containsValue(strDate)) {
					dateMap.put(listViewIndex, strDate);
					listViewIndex++;
				}
				
				// 把当前短信的真实索引存放在smsRealPositionMap中
				smsRealPositionMap.put(listViewIndex, cursor.getPosition());
				listViewIndex++;
			}
			// 把游标复位到-1
			cursor.moveToPosition(-1);
		}
		
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(!dateMap.containsKey(position)) {
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(smsRealPositionMap.get(position));
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			Intent intent = new Intent(this, SmsDetailUI.class);
			intent.putExtra("index", index);
			intent.putExtra("address", address);
			intent.putExtra("date", date);
			intent.putExtra("body", body);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_folder_detail_new_message: // 新建信息
			startActivity(new Intent(this, NewMessageUI.class));
			break;
		default:
			break;
		}
	}
}
