package one.skean.booksalesinfo.saxhandlers;

import one.skean.booksalesinfo.baseclass.BeanDefaultHandler;
import one.skean.booksalesinfo.bean.Login;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

public class LoginSAXHandler extends BeanDefaultHandler {
	private Login login;
	private String currebtTag;

	public LoginSAXHandler() {
		login = new Login();
	}

	@Override
	public Object getBeanObject() {
		// TODO Auto-generated method stub
		return login;
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
		currebtTag = localName;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		currebtTag = null;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String mString = new String(ch, start, length);
		if (login != null) {
			Log.i("skean", "当前tag:" + currebtTag + "   login is null:" + (login == null));
			if ("is_pass".equals(currebtTag)) login.setIspass(Boolean.parseBoolean(mString));
			else if ("message".equals(currebtTag)) login.setResultMessage(mString);
			else if ("id".equals(currebtTag)) login.setUserId(Integer.parseInt(mString));
			else if ("last_login".equals(currebtTag)) login.setLastlogin(mString);
			else if ("name".equals(currebtTag)) login.setUsername(mString);
		}
	}

}
