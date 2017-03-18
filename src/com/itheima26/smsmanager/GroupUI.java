package com.itheima26.smsmanager;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.Sms;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author andong
 * 群组
 */
public class GroupUI extends ListActivity implements OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "GroupUI";
	private GroupAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		init();
		prepareData();
	}
	
	private void prepareData() {
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		asyncQuery.startQuery(0, mAdapter, Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);
	}

	private void init() {
		ListView mListView = getListView();
		
		mAdapter = new GroupAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_group, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_create_group) {
//			Log.i(TAG, "创建群组");
			
			showCreateGroupDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 弹出新建群组对话框
	 */
	private void showCreateGroupDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("新建群组");
		final AlertDialog dialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		view.findViewById(R.id.btn_create_group).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String groupName = etName.getText().toString();
				if(!TextUtils.isEmpty(groupName)) {
					createGroup(groupName);
					dialog.dismiss();
				}
			}

		});
		
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		
		// 获得对话框窗体的属性
		LayoutParams lp = dialog.getWindow().getAttributes();

		// 整个屏幕的宽度
		
		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		
		dialog.getWindow().setAttributes(lp);
		
	}
	

	/**
	 * 创建群组
	 * @param groupName
	 */
	private void createGroup(String groupName) {
		ContentValues values = new ContentValues();
		values.put("group_name", groupName);
		Uri uri = getContentResolver().insert(Sms.GROUPS_INSERT_URI, values);
		if(ContentUris.parseId(uri) >= 0) {
			Toast.makeText(this, "群组创建成功", 0).show();
		}
	}
	
	class GroupAdapter extends CursorAdapter {

		public GroupAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.group_item, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvName = (TextView) view.findViewById(R.id.tv_group_item_name);
			
			tvName.setText(cursor.getString(cursor.getColumnIndex("group_name")));
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 把当前点击的群组的在关联关系表中所有的会话id取出来
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		String group_name = cursor.getString(cursor.getColumnIndex("group_name"));
		
		String selection = "group_id = " + group_id;
		Cursor c = getContentResolver().query(Sms.THREAD_GROUP_QUERY_ALL_URI, new String[]{"thread_id"}, 
				selection, null, null);
		if(c != null && c.getCount() > 0) {
			// (1, 2, 3)
			StringBuilder sb = new StringBuilder("(");
			
			while(c.moveToNext()) {
				sb.append(c.getString(0) + ", ");
			}
			c.close();
			// (1, 2, 3)
			String threadIDs = sb.substring(0, sb.lastIndexOf(", ")) + ")";
			
			// 把会话id传递给会话页面
			Intent intent = new Intent(this, ConversationUI.class);
			intent.putExtra("title", group_name);
			intent.putExtra("threadIDs", threadIDs);
			startActivity(intent);
		}
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		
		showOperatorDialog(group_id);
		return true;
	}
	
	/**
	 * 弹出操作对话框
	 */
	private void showOperatorDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setItems(new String[]{"修改", "删除"}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					Log.i(TAG, "修改");
					// 弹出一个修改对话框
					showUpdateGroupDialog(group_id);
				} else {
					Log.i(TAG, "删除");
					showDeleteGroupDialog(group_id);
				}
			}

		});
		builder.show();
	}

	/**
	 * 确定删除指定群组id的群组
	 * @param group_id
	 */
	private void showDeleteGroupDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("删除");
		builder.setMessage("删除群组将会删除群组中所包含的所有短信的关联, 确认继续?");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteGroup(group_id);
			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}
	
	/**
	 * 删除群组
	 * @param group_id
	 */
	private void deleteGroup(String group_id) {
		
		Uri uri = ContentUris.withAppendedId(Sms.GROUPS_SINGLE_DELETE_URI, Long.valueOf(group_id));
		
		int count = getContentResolver().delete(uri, null, null);
		if(count > 0) {
			Toast.makeText(this, "删除成功", 0).show();
		} else {
			Toast.makeText(this, "删除失败", 0).show();
		}
	}
	
	/**
	 * 弹出修改群组的对话框
	 */
	private void showUpdateGroupDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("修改群组");
		final AlertDialog updateGroupDialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		Button btnCreateGroup = (Button) view.findViewById(R.id.btn_create_group);
		btnCreateGroup.setText("确认修改");
		btnCreateGroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 更新群组
				String group_name = etName.getText().toString();
				if(!TextUtils.isEmpty(group_name)) {
					updateGroup(group_id, group_name);
					updateGroupDialog.dismiss();
				}
			}
		});
		updateGroupDialog.setView(view, 0, 0, 0, 0);
		updateGroupDialog.show();
		
		LayoutParams lp = updateGroupDialog.getWindow().getAttributes();
		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		updateGroupDialog.getWindow().setAttributes(lp);
	}
	
	/**
	 * 更新群组
	 * @param group_id
	 * @param group_name
	 */
	private void updateGroup(String group_id, String group_name) {
		ContentValues values = new ContentValues();
		values.put("group_name", group_name);
		String where = "_id = " + group_id;
		int count = getContentResolver().update(Sms.GROUPS_UPDATE_URI, values, where, null);
		if(count > 0) {
			Toast.makeText(this, "修改成功", 0).show();
		} else {
			Toast.makeText(this, "修改失败", 0).show();
		}
	}
}
