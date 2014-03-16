package one.skean.booksalesinfo.ui;

import java.util.HashMap;
import java.util.Map;

import one.skean.booksalesinfo.R;
import one.skean.booksalesinfo.baseclass.BeanDefaultHandler;
import one.skean.booksalesinfo.baseclass.NetActivity;
import one.skean.booksalesinfo.bean.Login;
import one.skean.booksalesinfo.saxhandlers.LoginSAXHandler;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends NetActivity implements OnClickListener, OnCheckedChangeListener {
	private EditText edtName, edtPassWord;
	private Button btnLogin, btnCancel;
	private CheckBox ckbRememberPW;

	private SharedPreferences preferences;
	private Handler mHandler = new Handler(this);
	private ProgressDialog progressDialog;

	private static final int MSG_VALIDATE = 3;
	private static final int MSG_INVALIDATE = 4;
	private static final int MSG_NULLINPUT = 5;
	private static final String LOGIN_PREFERENCES = "login_preferences";
	private static final String KEY_NAME = "name";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_REMEMBER = "remember_pw";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		edtName = (EditText) findViewById(R.id.edt_name);
		edtPassWord = (EditText) findViewById(R.id.edt_password);
		btnLogin = (Button) findViewById(R.id.btn_login);
		btnCancel = (Button) findViewById(R.id.btn_quit);
		ckbRememberPW = (CheckBox) findViewById(R.id.ckb_remember_pw);
		btnCancel.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		ckbRememberPW.setOnCheckedChangeListener(this);
		preferences = getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
		initStatus();
		initProgressDialog();
	}

	public void onClick(View v) {
		// 当点击的退出按键时退出
		if (v.getId() == R.id.btn_quit) {
			finish();
			return;
		}
		final String name = edtName.getText().toString();
		final String password = edtPassWord.getText().toString();
		// 用户名或者密码为空时发送空输入的消息
		if (name.equals("") || password.equals("")) {
			mHandler.sendEmptyMessage(MSG_NULLINPUT);
			edtPassWord.setText("");
			return;
		}
		progressDialog.show();
		// 新开线程验证登陆
		new Thread() {
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "login");
				params.put("loginname", name);
				params.put("password", password);
				BeanDefaultHandler handler = new LoginSAXHandler();
				Login result = (Login) getWebData(params, handler);
				if (result != null) {
					if (result.isIspass()) {
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("userId", result.getUserId() + "");
						bundle.putString("name", result.getUsername());
						bundle.putString("lastlogin", result.getLastlogin());
						msg.setData(bundle);
						msg.what = MSG_VALIDATE;
						mHandler.sendMessage(msg);
					}
					else {
						Message msg = new Message();
						msg.what = MSG_INVALIDATE;
						msg.obj = result.getResultMessage();
						mHandler.sendMessage(msg);
					}
				}
			}
		}.start();
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Editor editor = preferences.edit();
		editor.putBoolean(KEY_REMEMBER, isChecked);
		editor.commit();
	}

	private void initStatus() {
		if (preferences.getBoolean(KEY_REMEMBER, false)) {
			ckbRememberPW.setChecked(true);
			edtName.setText(preferences.getString(KEY_NAME, ""));
			edtPassWord.setText(preferences.getString(KEY_PASSWORD, ""));
		}
	}

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(this, R.style.custom_progress);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("登陆中...");
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (!(msg.what == MSG_NULLINPUT)) {
			progressDialog.dismiss();
		}
		super.handleMessage(msg);
		switch (msg.what) {
		case MSG_NULLINPUT:
			Toast.makeText(getApplicationContext(), "用户名, 密码不能为空, 请修改!", Toast.LENGTH_SHORT).show();
			break;
		case MSG_INVALIDATE:
			Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
			break;
		case MSG_VALIDATE:
			// 通过登陆验证并且选择记住密码的情况下记录下用户名密码
			if (preferences.getBoolean(KEY_REMEMBER, false)) {
				Editor editor = preferences.edit();
				editor.putString(KEY_NAME, edtName.getText().toString());
				editor.putString(KEY_PASSWORD, edtPassWord.getText().toString());
				editor.commit();
			}
			Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
			intent.putExtras(msg.getData());
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
		return true;
	}
}
