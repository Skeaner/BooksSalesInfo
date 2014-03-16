package one.skean.booksalesinfo.baseclass;

import java.io.InputStream;
import java.util.Map;

import one.skean.booksalesinfo.R;
import one.skean.booksalesinfo.util.NetTool;
import one.skean.booksalesinfo.util.SaxXmlTools;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.Toast;

public class NetActivity extends Activity implements Callback {
	private Handler netHandler;
	protected static final int MSG_NET_ERROR = 1;
	protected static final int MSG_PARSE_ERROR = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		netHandler = new Handler(this);
	}

	/**
	 * NetActivity的请求网络数据的方法
	 * @param requestParams 网络请求的参数Map
	 * @param handler 解析xml数据用的SAXhandler
	 */
	protected Object getWebData(Map<String, String> requestParams, BeanDefaultHandler handler) {
		String url = getResources().getString(R.string.host_address) + "/android/xml.do";
		InputStream instream;
		try {
			instream = NetTool.getStreamFromUrl(url, requestParams);
		}
		catch (Exception e) {
			netHandler.sendEmptyMessage(MSG_NET_ERROR);
			e.printStackTrace();
			return null;
		}
		try {
			SaxXmlTools.parseWithHandler(instream, handler);
			return handler.getBeanObject();
		}
		catch (Exception e) {
			netHandler.sendEmptyMessage(MSG_PARSE_ERROR);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_NET_ERROR:
			Toast.makeText(getApplicationContext(), "网络错误, 请检查网络!", Toast.LENGTH_SHORT).show();
			break;
		case MSG_PARSE_ERROR:
			Toast.makeText(getApplicationContext(), "数据解析错误, 请重试!", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return false;
	}

}
