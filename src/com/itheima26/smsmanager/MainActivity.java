package com.itheima26.smsmanager;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * @author andong
 * 主控制器
 */
public class MainActivity extends TabActivity implements OnClickListener {

    private TabHost mTabHost;
	private View mSlideView;	// 页签的滑动背景
	private int basicWidth = 0;	// 一等分的宽度
	private int startX = 0;		// 记住上一次移动完成之后的x轴的偏移量

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        
        initTabHost();
    }

	/**
	 * 初始化tabhost
	 */
	private void initTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mSlideView = findViewById(R.id.slide_view);
		final View llConversation = findViewById(R.id.ll_conversation);
		
		llConversation.setOnClickListener(this);
		findViewById(R.id.ll_folder).setOnClickListener(this);
		findViewById(R.id.ll_group).setOnClickListener(this);
		
		// 初始化滑动背景的宽和高
		
		// 获得视图树的观察者对象, 添加一个当全部布局(layout)完成时的监听事件
		llConversation.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			/**
			 * 全局布局完成时回调.
			 */
			@Override
			public void onGlobalLayout() {
				// 移除全局布局的监听事件
				llConversation.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				
				// 得到会话布局的参数, 设置给滑动块
				LayoutParams lp = (LayoutParams) mSlideView.getLayoutParams();
				lp.width = llConversation.getWidth();
				lp.height = llConversation.getHeight();
				lp.leftMargin = llConversation.getLeft();
				mSlideView.setLayoutParams(lp);
				
				basicWidth = findViewById(R.id.rl_conversation).getWidth();
			}
		});
		
		
		addTabSpec("conversation", "会话", R.drawable.tab_conversation, new Intent(this, ConversationUI.class));
		addTabSpec("folder", "文件夹", R.drawable.tab_folder, new Intent(this, FolderUI.class));
		addTabSpec("group", "群组", R.drawable.tab_group, new Intent(this, GroupUI.class));
	}

	/**
	 * 添加一个页签
	 * @param tag 标记
	 * @param label 标题
	 * @param icon 图标
	 * @param intent 指向的activity
	 */
	private void addTabSpec(String tag, String label, int icon, Intent intent) {
		TabSpec newTabSpec = mTabHost.newTabSpec(tag);
		// 设置页签的标题和图标
		newTabSpec.setIndicator(label, getResources().getDrawable(icon));
		// 设置页签指向的显示内容问activity
		newTabSpec.setContent(intent);
		
		// 添加页签
		mTabHost.addTab(newTabSpec);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ll_conversation: // 切换到会话页签
			if(!"conversation".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("conversation");
				startTranslateAnimation(startX, 0);
				startX = 0;
			}
			break;
		case R.id.ll_folder: // 切换到文件夹页签
			if(!"folder".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("folder");
				startTranslateAnimation(startX, basicWidth);
				startX = basicWidth;
			}
			break;
		case R.id.ll_group: // 切换到群组页签
			if(!"group".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("group");
				startTranslateAnimation(startX, basicWidth * 2);
				startX = basicWidth * 2;
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 给滑动块执行唯一动画
	 * @param fromXDelta 开始位移x轴的偏移量
	 * @param toXDelta	结束位移x轴的偏移量
	 */
	private void startTranslateAnimation(int fromXDelta, int toXDelta) {
		TranslateAnimation ta = new TranslateAnimation(
				fromXDelta, toXDelta, 0, 0);
		ta.setDuration(500);
		ta.setFillAfter(true);	// 停留在动画结束的位置上
		mSlideView.startAnimation(ta);
	}
}
