package com.itheima26.smsmanager.utils;

import android.net.Uri;

public class Sms {

	/**
	 * 查询会话的uri
	 */
	public static final Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");
	
	/**
	 * 操作sms表的的uri
	 */
	public static final Uri SMS_URI = Uri.parse("content://sms/");
	
	/**
	 * 收件箱的uri
	 */
	public static final Uri INBOX_URI = Uri.parse("content://sms/inbox");
	
	/**
	 * 发件箱的uri
	 */
	public static final Uri OUTBOX_URI = Uri.parse("content://sms/outbox");
	
	/**
	 * 已发送的uri
	 */
	public static final Uri SENT_URI = Uri.parse("content://sms/sent");
	
	/**
	 * 草稿箱的uri
	 */
	public static final Uri DRAFT_URI = Uri.parse("content://sms/draft");
	
	/**
	 * 添加到群组的uri
	 */
	public static final Uri GROUPS_INSERT_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/insert");
	
	/**
	 * 查询所有群组的uri
	 */
	public static final Uri GROUPS_QUERY_ALL_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups");
	
	/**
	 * 查询所有关联关系表中的内容的uri
	 */
	public static final Uri THREAD_GROUP_QUERY_ALL_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group");
	
	/**
	 * 添加到关联关系表的uri
	 */
	public static final Uri THREAD_GROUP_INSERT_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group/insert");
	
	/**
	 * 更新群组表的uri
	 */
	public static final Uri GROUPS_UPDATE_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/update");
	
	/**
	 * 删除群组的uri(删除群组会删除其所带的关联关系)
	 */
	public static final Uri GROUPS_SINGLE_DELETE_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/delete/#");
	
	
	public static final int RECEVIE_TYPE = 1;	// 短信类型: 接收的
	public static final int SEND_TYPE = 2;		// 短信类型: 发送的
	
}
