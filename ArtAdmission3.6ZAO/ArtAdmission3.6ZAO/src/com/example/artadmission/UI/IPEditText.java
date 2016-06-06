package com.example.artadmission.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.example.artadmission.R;
import com.example.artadmission.define_var;
/**
 * 自定义控件实现ip地址特殊输入
 * 
 * @author 子墨
 * 
 *         2015-1-4
 */
public class IPEditText extends LinearLayout {

	private EditText mFirstIP;
	private EditText mSecondIP;
	private EditText mThirdIP;
	private EditText mFourthIP;
	private String mText1;
	private String mText2;
	private String mText3;
	private String mText4;

	private SharedPreferences mPreferences;
	//	private boolean changed = false;
	public IPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

		/**
		 * 初始化控件
		 */
		View view = LayoutInflater.from(context).inflate(
				R.layout.custom_my_edittext, this);
		mFirstIP = (EditText)findViewById(R.id.ip_first);
		mSecondIP = (EditText)findViewById(R.id.ip_second);
		mThirdIP = (EditText)findViewById(R.id.ip_third);
		mFourthIP = (EditText)findViewById(R.id.ip_fourth);

		String ip[] = define_var.RHOST.split("\\.");
		if(ip.length == 4)
		{
			mFirstIP.setText(ip[0]);
			mSecondIP.setText(ip[1]);
			mThirdIP.setText(ip[2]);
			mFourthIP.setText(ip[3]);
			mText1 = ip[0];
			mText2 = ip[1];
			mText3 = ip[2];
			mText4 = ip[3];
//			mFirstIP.setFocusable(false);
//			mSecondIP.setFocusable(false);
//			mThirdIP.setFocusable(false);
			mFourthIP.setFocusable(true);
			mFourthIP.requestFocus();
			mFourthIP.setSelection(mFourthIP.getText().toString().length());
		}
		mPreferences = context.getSharedPreferences("config_IP",
				Context.MODE_PRIVATE);

		OperatingEditText(context);
	}
	/**
	 * 获得EditText中的内容,当每个Edittext的字符达到三位时,自动跳转到下一个EditText,当用户点击.时,
	 * 下一个EditText获得焦点
	 */
	private void OperatingEditText(final Context context) {
		mFirstIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				if (s != null && s.length() > 0) {
					if (s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))!=0) {
						mText1 = s.toString().trim().substring(0, s.length() - 1);
						mFirstIP.setText(mText1);
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText1.length());
						editor.commit();
						mSecondIP.setFocusable(true);
						mSecondIP.requestFocus();
						
					} 	
					else if ((s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))==0)) {
						mText1 = s.toString().trim().substring(1, s.length());
//						mText1.replace(".", "");
						mFirstIP.setText(mText1);
					}
					else if (s.length() > 2)
					{
						mText1 = s.toString().trim();
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText1.length());
						editor.commit();
						mSecondIP.setFocusable(true);
						mSecondIP.requestFocus();
					}
					else mText1 = mFirstIP.getText().toString();
				}		
				//				changed = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		//		mFirstIP.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		//		    @Override
		//		    public void onFocusChange(View v, boolean hasFocus) {
		//		        // TODO Auto-generated method stub
		//		    	mText1 = mFirstIP.getText().toString();
		//		    }});
		//		mSecondIP.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		//		    @Override
		//		    public void onFocusChange(View v, boolean hasFocus) {
		//		        // TODO Auto-generated method stub
		//		    	mText2 = mSecondIP.getText().toString();
		//		    }});
		//		mThirdIP.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		//		    @Override
		//		    public void onFocusChange(View v, boolean hasFocus) {
		//		        // TODO Auto-generated method stub
		//		    	mText3 = mThirdIP.getText().toString();
		//		    }});
		//		mFourthIP.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		//		    @Override
		//		    public void onFocusChange(View v, boolean hasFocus) {
		//		        // TODO Auto-generated method stub
		//		    	mText4 = mFourthIP.getText().toString();
		//		    }});
		mSecondIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				//				if (s != null && s.length() > 0) {
				//					if (s.length() > 2 || s.toString().trim().contains(".")) {
				//						if (s.toString().trim().contains(".")&&s.toString().indexOf(".")!=0) {
				//							mText2 = s.toString().substring(0, s.length() - 1);
				//							mSecondIP.setText(mText2);
				//						} else {
				//							mText2 = s.toString().trim();
				//						}
				//
				//						if (Integer.parseInt(mText2) > 255) {
				//							Toast.makeText(context, "请输入合法的ip地址",
				//									Toast.LENGTH_LONG).show();
				//							return;
				//						}
				//
				//						Editor editor = mPreferences.edit();
				//						editor.putInt("IP_SECOND", mText2.length());
				//						editor.commit();
				//
				//						mThirdIP.setFocusable(true);
				//						mThirdIP.requestFocus();
				//					}
				//					else mText2 = mSecondIP.getText().toString();
				//				}
				if (s != null && s.length() > 0) {
					if (s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))!=0) {
						mText2 = s.toString().trim().substring(0, s.length() - 1);
						mSecondIP.setText(mText2);
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText2.length());
						editor.commit();
						mThirdIP.setFocusable(true);
						mThirdIP.requestFocus();
					} 	
					else if ((s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))==0)) {
						mText2 = s.toString().trim().substring(1, s.length());
//						mText2.replace(".", "");
						mSecondIP.setText(mText2);
					}
					else if (s.length() > 2)
					{
						mText2 = s.toString().trim();
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText2.length());
						editor.commit();
						mThirdIP.setFocusable(true);
						mThirdIP.requestFocus();
					}
					else mText2 = mSecondIP.getText().toString();
				}	

				/**
				 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
				 */
				if (start == 0 && s.length() == 0) {
					mFirstIP.setFocusable(true);
					mFirstIP.requestFocus();
					mFirstIP.setSelection(mFirstIP.getText().toString().length());
					System.out.println("----2到1");
				}
				//				changed = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mThirdIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				//				if (s != null && s.length() > 0) {
				//					if (s.length() > 2 || s.toString().trim().contains(".")) {
				//						if (s.toString().trim().contains(".")&&s.toString().indexOf(".")!=0) {
				//							mText3 = s.toString().substring(0, s.length() - 1);
				//							mThirdIP.setText(mText3);
				//						} else {
				//							mText3 = s.toString().trim();
				//						}
				//
				//						if (Integer.parseInt(mText3) > 255) {
				//							Toast.makeText(context, "请输入合法的ip地址",
				//									Toast.LENGTH_LONG).show();
				//							return;
				//						}
				//
				//						Editor editor = mPreferences.edit();
				//						editor.putInt("IP_THIRD", mText3.length());
				//						editor.commit();
				//
				//						mFourthIP.setFocusable(true);
				//						mFourthIP.requestFocus();
				//					}
				//					else mText3 = mThirdIP.getText().toString();
				//				}
				if (s != null && s.length() > 0) {
					if (s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))!=0) {
						mText3 = s.toString().trim().substring(0, s.length() - 1);
						mThirdIP.setText(mText3);
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText3.length());
						editor.commit();
						mFourthIP.setFocusable(true);
						mFourthIP.requestFocus();
					} 	
					else if ((s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))==0)) {
						mText3 = s.toString().trim().substring(1, s.length());
//						mText3.replace(".", "");
						mThirdIP.setText(mText3);
					}
					else if (s.length() > 2)
					{
						mText3 = s.toString().trim();
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText3.length());
						editor.commit();
						mFourthIP.setFocusable(true);
						mFourthIP.requestFocus();
					}
					else mText3 = mThirdIP.getText().toString();
				}	
				/**
				 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
				 */
				if (start == 0 && s.length() == 0) {
					mSecondIP.setFocusable(true);
					mSecondIP.requestFocus();
					mSecondIP.setSelection(mSecondIP.getText().toString().length());
					System.out.println("----3到2");
				}
				//				changed = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mFourthIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				if (s != null && s.length() > 0) {
					if (s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))!=0) {
						mText4 = s.toString().trim().substring(0, s.length() - 1);
						mFourthIP.setText(mText4);
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText4.length());
						editor.commit();
//						mFirstIP.setFocusable(true);
//						mFirstIP.requestFocus();
					} 	
					else if ((s.toString().trim().contains(".")&&(s.toString().trim().indexOf("."))==0)) {
						mText4 = s.toString().trim().substring(1, s.length());
//						mText4.replace(".", "");
						mFourthIP.setText(mText4);
					}
					else if (s.length() > 2)
					{
						mText4 = s.toString().trim();
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText4.length());
						editor.commit();
//						mFirstIP.setFocusable(true);
//						mFirstIP.requestFocus();
					}
					else mText4 = mFourthIP.getText().toString();
				}	

				/**
				 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
				 */
				if (start == 0 && s.length() == 0) {
					mThirdIP.setFocusable(true);
					mThirdIP.requestFocus();
//					mThirdIP.setSelection(mPreferences.getInt("IP_THIRD", 0));
					mThirdIP.setSelection(mThirdIP.getText().toString().length());
					System.out.println("----4到3");
				}
				//				changed = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	public String getText(Context context) {
		//		if(!changed)
		//		{
		//			String ip[] = define_var.RHOST.split("\\.");
		//			if(ip.length == 4)
		//			{
		//				mText1 = ip[0];
		//				mText2 = ip[1];
		//				mText3 = ip[2];
		//				mText4 = ip[3];
		//			}
		//		}
		//		if (TextUtils.isEmpty(mText1) || TextUtils.isEmpty(mText2)
		//				|| TextUtils.isEmpty(mText3) || TextUtils.isEmpty(mText4)) {
		//			Toast.makeText(context, "请输入合法的ip地址", Toast.LENGTH_LONG).show();
		//		}
		return mFirstIP.getText().toString() + "." + mSecondIP.getText().toString() + "." + mThirdIP.getText().toString() + "." + mFourthIP.getText().toString();
	}
}
