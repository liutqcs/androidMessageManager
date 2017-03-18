package com.itheima26.smsmanager;

import com.itheima26.smsmanager.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.Toast;

public class NewMessageUI extends Activity implements OnClickListener {

	protected static final String TAG = "NewMessageUI";
	
	private final String[] projection = {
			"_id", 
			"data1", 
			"display_name"
	};
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int NAME_COLUMN_INDEX = 2;

	private AutoCompleteTextView actvNumber;

	private EditText etContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_message);
		
		initView();
	}

	private void initView() {
		actvNumber = (AutoCompleteTextView) findViewById(R.id.actv_new_message_number);
		etContent = (EditText) findViewById(R.id.et_new_message_content);
		
		findViewById(R.id.ib_new_message_select_contact).setOnClickListener(this);
		findViewById(R.id.btn_new_message_send).setOnClickListener(this);
		
		ContactAdapter mAdapter = new ContactAdapter(this, null);
		actvNumber.setAdapter(mAdapter);
		
		mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			
			/**
			 * 当自动提示文本框开始过滤查询时回调
			 * @param constraint 自动提示文本框输入的内容
			 */
			@Override
			public Cursor runQuery(CharSequence constraint) {
				Log.i(TAG, "开始过滤查询: " + constraint);
				
				// 根据constraint字段查询联系人的数据, 返回cursor
				
				// 选择条件: where data1 like '%12%'
				String selection = "data1 like ?";
				String selectionArgs[] = {"%" + constraint + "%"};
				Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, 
						projection, selection, selectionArgs, null);
				return cursor;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_new_message_send:	// 发送消息
			String address = actvNumber.getText().toString();
			String content = etContent.getText().toString();
			
			if(TextUtils.isEmpty(address) || TextUtils.isEmpty(content)) {
				Toast.makeText(this, "请正确输入", 0).show();
				break;
			}
			
			Utils.sendMessage(this, address, content);
			finish();
			break;
		case R.id.ib_new_message_select_contact:		// 选择联系人
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setData(Contacts.CONTENT_URI);
			startActivityForResult(intent, 100);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 接收使用startActivityForResult开启的activity返回的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
//			Log.i(TAG, "返回的Uri: " + uri);
			
			// 根据uri查询联系人的id
			long contactID = Utils.getContactID(getContentResolver(), uri);
			Log.i(TAG, "联系人的id: " + contactID);
			
			// 根据联系人的id查询联系人的号码, 显示到自动提示文本框中
			if(contactID != -1) {
				String address = Utils.getContactAddress(getContentResolver(), contactID);
				if(!TextUtils.isEmpty(address)) {
					actvNumber.setText(address);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	class ContactAdapter extends CursorAdapter {

		public ContactAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.contact_item, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			String name = cursor.getString(NAME_COLUMN_INDEX);
			
			TextView tvName = (TextView) view.findViewById(R.id.tv_contact_item_name);
			TextView tvAddress = (TextView) view.findViewById(R.id.tv_contact_item_address);
			
			tvName.setText(name);
			tvAddress.setText(address);
		}
		
	}
}
