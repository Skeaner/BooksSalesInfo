package one.skean.booksalesinfo.util;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class NetTool {
	public static InputStream getStreamFromUrl(String url, Map<String, String> requestParams)
		throws Exception {
		StringBuilder params = new StringBuilder();
		if (requestParams != null && !requestParams.isEmpty()) {
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.append(entry.getKey());
				params.append("=");
				params.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				params.append("&");
			}
			if (params.length() > 0) params.deleteCharAt(params.length() - 1);
		}

		byte[] data = params.toString().getBytes();
		URL realUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		outStream.write(data);
		outStream.flush();
		if (conn.getResponseCode() == 200) {
			return conn.getInputStream();
		}
		else {
			throw new Exception("formNetTool:网络连接错误!");
		}
	}
}
