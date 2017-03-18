package com.itheima26.smsmanager;

import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.Sms;
import com.itheima26.smsmanager.utils.Utils;

/**
 * @author andong
 * 会话
 */
public class ConversationUI extends Activity implements OnClickListener, 
	OnItemClickListener, OnItemLongClickListener {
	
	protected static final String TAG = "ConversationUI";
	private static final int SEARCH_ID = 0;
	private static final int EDIT_ID = 1;
	private static final int CANCEL_EDIT_ID = 2;
	
	private final int LIST_STATE = -1;
	private final int EDIT_STATE = -2;
	private int currentState = LIST_STATE;		// 当前默认的状态为列表状态
	
	private HashSet<Integer> mMultiDeleteSet;
	
	
	private String[] projection = {
			"sms.thread_id AS _id",
			"sms.body AS body",
			"groups.msg_count AS count",
			"sms.date AS date",
			"sms.address AS address"
	};
	private final int THREAD_ID_COLUMN_INDEX = 0;
	private final int BODY_COLUMN_INDEX = 1;
	private final int COUNT_COLUMN_INDEX = 2;
	private final int DATE_COLUMN_INDEX = 3;
	private final int ADDRESS_COLUMN_INDEX = 4;
	private ConversationAdapter mAdapter;
	
	private Button btnNewMessage;
	private Button btnSelectAll;
	private Button btnCancelSelect;
	private Button btnDeleteMessage;
	private ListView mListView;
	private ProgressDialog mProgressDialog;
	private boolean isStop = false;		// 是否停止

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		
		initView();
		prepareData();
		
	}
	
	@Override
	public void onBackPressed() {
		if(currentState == EDIT_STATE) {
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			return;
		}
		super.onBackPressed();
	}

	/**
	 * 此方法是创建options菜单调用, 只会被调用一次
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SEARCH_ID, 0, "搜索");
		menu.add(0, EDIT_ID, 0, "编辑");
		menu.add(0, CANCEL_EDIT_ID, 0, "取消编辑");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 当菜单将要显示在屏幕上时, 回调此方法
	 * 控制显示哪一个菜单
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(currentState == EDIT_STATE) {
			// 显示取消编辑, 隐藏另外两个
			menu.findItem(SEARCH_ID).setVisible(false);
			menu.findItem(EDIT_ID).setVisible(false);
			menu.findItem(CANCEL_EDIT_ID).setVisible(true);
		} else {
			menu.findItem(SEARCH_ID).setVisible(true);
			menu.findItem(EDIT_ID).setVisible(true);
			menu.findItem(CANCEL_EDIT_ID).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * 当options菜单被选中时回调
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SEARCH_ID:		// 搜索菜单被选中
			onSearchRequested();		// 呼出搜索对话框
			break;
		case EDIT_ID:		// 编辑菜单
			currentState = EDIT_STATE;
			refreshState();
			break;
		case CANCEL_EDIT_ID:	// 取消编辑
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 刷新状态
	 */
	private void refreshState() {
		if(currentState == EDIT_STATE) {
			// 新建信息隐藏, 其他按钮显示, 每一个item都要显示一个checkBox 
			btnNewMessage.setVisibility(View.GONE);
			btnSelectAll.setVisibility(View.VISIBLE);
			btnCancelSelect.setVisibility(View.VISIBLE);
			btnDeleteMessage.setVisibility(View.VISIBLE);
			
			if(mMultiDeleteSet.size() == 0) {
				// 没有选中任何checkbox
				btnCancelSelect.setEnabled(false);
				btnDeleteMessage.setEnabled(false);
			} else {
				btnCancelSelect.setEnabled(true);
				btnDeleteMessage.setEnabled(true);
			}
			
			// 全选
			btnSelectAll.setEnabled(mMultiDeleteSet.size() != mListView.getCount());
		} else {
			// 新建信息显示, 其他的隐藏
			btnNewMessage.setVisibility(View.VISIBLE);
			btnSelectAll.setVisibility(View.GONE);
			btnCancelSelect.setVisibility(View.GONE);
			btnDeleteMessage.setVisibility(View.GONE);
		}
	}

	private void initView() {
		mMultiDeleteSet = new HashSet<Integer>();
		
		mListView = (ListView) findViewById(R.id.lv_conversation);
		btnNewMessage = (Button) findViewById(R.id.btn_conversation_new_message);
		btnSelectAll = (Button) findViewById(R.id.btn_conversation_select_all);
		btnCancelSelect = (Button) findViewById(R.id.btn_conversation_cancel_select);
		btnDeleteMessage = (Button) findViewById(R.id.btn_conversation_delete_message);
		
		btnNewMessage.setOnClickListener(this);
		btnSelectAll.setOnClickListener(this);
		btnCancelSelect.setOnClickListener(this);
		btnDeleteMessage.setOnClickListener(this);
		
		mAdapter = new ConversationAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	/**
	 * 异步查询数据
	 */
	private void prepareData() {
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		String selection = null;
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		if(!TextUtils.isEmpty(title)) {
			setTitle(title);
			String threadIDs = intent.getStringExtra("threadIDs");
			selection = "thread_id in " + threadIDs;
		}
		
		asyncQuery.startQuery(0, mAdapter, Sms.CONVERSATION_URI, projection, selection, null, "date desc");
	}
	
	class ConversationAdapter extends CursorAdapter {
		
		private ConversationHolderView mHolder;

		public ConversationAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		/**
		 * 创建一个view
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder = new ConversationHolderView();
			mHolder.checkBox = (CheckBox) view.findViewById(R.id.cb_conversation_item);
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_conversation_item_body);
			view.setTag(mHolder);
			return view;
		}

		/**
		 * 绑定数据
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (ConversationHolderView) view.getTag();
			int id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int count = cursor.getInt(COUNT_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			// 判断当前的状态是否是编辑
			if(currentState == EDIT_STATE) {
				// 显示checkbox
				mHolder.checkBox.setVisibility(View.VISIBLE);
				
				// 当前的会话id是否存在与deleteSet集合中
				mHolder.checkBox.setChecked(mMultiDeleteSet.contains(id));
			} else {
				// 隐藏checkbox
				mHolder.checkBox.setVisibility(View.GONE);
			}
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				// 显示号码
				mHolder.tvName.setText(address + "(" + count + ")");
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				// 显示名称
				mHolder.tvName.setText(contactName + "(" + count + ")");
				
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
	
	public class ConversationHolderView {
		public CheckBox checkBox;
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_conversation_new_message: // 新建信息
			startActivity(new Intent(this, NewMessageUI.class));
			break;
		case R.id.btn_conversation_select_all: // 全选
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(-1);		// 复位到初始的位置
			
			while(cursor.moveToNext()) {
				mMultiDeleteSet.add(cursor.getInt(THREAD_ID_COLUMN_INDEX));
			}
			mAdapter.notifyDataSetChanged();	// 刷新数据
			refreshState();
			break;
		case R.id.btn_conversation_cancel_select: // 取消选择
			mMultiDeleteSet.clear();
			mAdapter.notifyDataSetChanged();	// 刷新数据
			refreshState();
			break;
		case R.id.btn_conversation_delete_message: // 删除信息
			showConfirmDeleteDialog();
			break;
		default:
			break;
		}
	}

	/**
	 * 确认删除对话框
	 */
	private void showConfirmDeleteDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);		// 设置图标
		builder.setTitle("删除");
		builder.setMessage("确认删除选中的会话吗?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "确认删除");
				
				// 弹出进度对话框
				showDeleteProgressDialog();
				isStop = false;
				// 开启子线程, 真正删除短信, 每删除一条短信, 更新进度条
				new Thread(new DeleteRunnable()).start();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	
	/**
	 * 弹出删除进度对话框
	 */
	@SuppressWarnings("deprecation")
	private void showDeleteProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		// 设置最大值
		mProgressDialog.setMax(mMultiDeleteSet.size());
		// 设置进度条的演示为长条
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "终止删除");
				isStop = true;
			}
		});
		mProgressDialog.show();
		mProgressDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				currentState = LIST_STATE;
				refreshState();
			}
		});
	}
	
	/**
	 * @author andong
	 * 删除会话的任务
	 */
	class DeleteRunnable implements Runnable {

		@Override
		public void run() {
			// 删除会话
			
			Iterator<Integer> iterator = mMultiDeleteSet.iterator();
			
			int thread_id;
			String where;
			String[] selectionArgs;
			while(iterator.hasNext()) {
				
				if(isStop) {
					break;
				}
				
				thread_id = iterator.next();
				where = "thread_id = ?";
				selectionArgs = new String[]{String.valueOf(thread_id)};
				getContentResolver().delete(Sms.SMS_URI, where, selectionArgs);
				
				SystemClock.sleep(2000);
				
				// 更新进度条
				mProgressDialog.incrementProgressBy(1);
			}
			
			mMultiDeleteSet.clear();
			mProgressDialog.dismiss();
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 把当前被点击的item的会话id添加到集合中, 刷新checkbox
		Cursor cursor = mAdapter.getCursor();
		// 移动到当前被点的索引
		cursor.moveToPosition(position);
		
		// 会话的id
		int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
		String address = cursor.getString(ADDRESS_COLUMN_INDEX);
		
		if(currentState == EDIT_STATE) {
			
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_conversation_item);
			
			if(checkBox.isChecked()) {
				// 移除id
				mMultiDeleteSet.remove(thread_id);
			} else {
				mMultiDeleteSet.add(thread_id);
			}
			checkBox.setChecked(!checkBox.isChecked());
			
			// 每一次点击刷新一下按钮的状态
			refreshState();
		} else {
			Intent intent = new Intent(this, ConversationDetailUI.class);
			intent.putExtra("thread_id", thread_id);
			intent.putExtra("address", address);
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		
		// 判断的当前会话是否添加过群组
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String thread_id = cursor.getString(THREAD_ID_COLUMN_INDEX);
		
		String groupName = getGroupName(thread_id);
		if(!TextUtils.isEmpty(groupName)) {
			Toast.makeText(this, "该会话已经存放在\"" + groupName + "\"中", 0).show();
		} else {
			// 弹出选择群组对话框
			showSelectGroupDialog(thread_id);
		}
		return true;
	}
	
	/**
	 * 弹出选择群组的对话框
	 * @param thread_id
	 */
	private void showSelectGroupDialog(final String thread_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("选择将要加入的群组");
		// 查出所有的群组
		Cursor cursor = getContentResolver().query(Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			final String[] groupNameArray = new String[cursor.getCount()];
			final String[] groupIDArray = new String[cursor.getCount()];
			
			while(cursor.moveToNext()) {
				groupIDArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("_id"));
				groupNameArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("group_name"));
			}
			
			builder.setItems(groupNameArray, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					Log.i(TAG, "当前选中的群组是: " + groupNameArray[which]);
					
					// 把当前会话添加到选中的群组中
					addGroup(groupIDArray[which], thread_id);
				}
			});
			builder.show();
		}
	}
	
	/**
	 * 添加到群组
	 * @param group_id
	 * @param thread_id
	 */
	private void addGroup(String group_id, String thread_id) {
		// 往关联关系表中添加一条数据
		ContentValues values = new ContentValues();
		values.put("group_id", group_id);
		values.put("thread_id", thread_id);
		Uri uri = getContentResolver().insert(Sms.THREAD_GROUP_INSERT_URI, values);
		
		if(ContentUris.parseId(uri) != -1) {
			Toast.makeText(this, "添加成功", 0).show();
		} else {
			Toast.makeText(this, "添加失败", 0).show();
		}
	}

	/**
	 * 根据会话的id获取群组的名称
	 * @param thread_id
	 * @return
	 */
	private String getGroupName(String thread_id) {
		// 根据会话的id获取群组的id 
		String selection = "thread_id = " + thread_id;
		Cursor cursor = getContentResolver().query(Sms.THREAD_GROUP_QUERY_ALL_URI, new String[]{"group_id"}, 
				selection, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			String groupId = cursor.getString(0);
			cursor.close();
			//如果群组id不为null
			if(!TextUtils.isEmpty(groupId)) {
				// 取群组表中把对应名称取出
				selection = "_id = " + groupId;
				cursor = getContentResolver().query(Sms.GROUPS_QUERY_ALL_URI, new String[]{"group_name"}, 
						selection, null, null);
				if(cursor != null && cursor.moveToFirst()) {
					String groupName = cursor.getString(0);
					return groupName;
				}
			}
		}
		return null;
	}
}
