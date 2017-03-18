package com.itheima26.smsmanager;

import java.util.HashMap;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.CommonAsyncQuery.OnQueryNotifyCompleteListener;
import com.itheima26.smsmanager.utils.Utils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author andong
 * 文件夹
 */
public class FolderUI extends ListActivity implements OnQueryNotifyCompleteListener, OnItemClickListener {

	private int[] imageIDs;
	private String[] typeArrays;
	private HashMap<Integer, Integer> countMap;
	private FolderAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		initView();
	}

	private void initView() {
		ListView mListView = getListView();
		
		imageIDs = new int[] {
				R.drawable.a_f_inbox,
				R.drawable.a_f_outbox,
				R.drawable.a_f_sent,
				R.drawable.a_f_draft
		};
		
		typeArrays = new String[] {
				"收件箱",
				"发件箱",
				"已发送",
				"草稿箱"
		};
		
		countMap = new HashMap<Integer, Integer>();
		
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		
		Uri uri;
		for (int i = 0; i < 4; i++) {
			countMap.put(i, 0);
			
			uri = Utils.getUriFromIndex(i);
			
			asyncQuery.startQuery(i, null, uri, new String[]{"count(*)"}, null, null, null);
		}
		
		mAdapter = new FolderAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	class FolderAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return imageIDs.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if(convertView == null) {
				view = View.inflate(FolderUI.this, R.layout.folder_item, null);
			} else {
				view = convertView;
			}
			
			ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_folder_item_icon);
			TextView tvType = (TextView) view.findViewById(R.id.tv_folder_item_type);
			TextView tvCount = (TextView) view.findViewById(R.id.tv_folder_item_count);
			
			ivIcon.setImageResource(imageIDs[position]);
			tvType.setText(typeArrays[position]);
			tvCount.setText(countMap.get(position) + "");
			return view;
		}
		
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		if(cursor != null && cursor.moveToFirst()) {
			countMap.put(token, cursor.getInt(0));
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, FolderDetailUI.class);
		intent.putExtra("index", position);
		startActivity(intent);
	}

	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}
}
