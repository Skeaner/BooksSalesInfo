package one.skean.booksalesinfo.saxhandlers;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import one.skean.booksalesinfo.baseclass.BeanDefaultHandler;
import one.skean.booksalesinfo.bean.SaleDetial;

public class SaleDetailSAXHandler extends BeanDefaultHandler {
	private List<SaleDetial> datas;
	private String currentTag;
	private SaleDetial currentDetail;

	public SaleDetailSAXHandler() {
		datas = new ArrayList<SaleDetial>();
	}

	@Override
	public Object getBeanObject() {
		// TODO Auto-generated method stub
		return datas;
	}

	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("完成解析");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
		currentTag = localName;
		if (currentTag.equals("saleDetail")) {
			currentDetail = new SaleDetial();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		currentTag = null;
		if (localName.equals("saleDetail")) {
			datas.add(currentDetail);
			currentDetail = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String mString = new String(ch, start, length);
		System.out.println(currentTag + mString);
		if (currentDetail != null) {
			if ("id".equals(currentTag)) currentDetail.setId(mString);
			if ("schoolname".equals(currentTag)) currentDetail.setSchoolName(mString);
			if ("devicecode".equals(currentTag)) currentDetail.setDeviceCode(mString);
			if ("bookname".equals(currentTag)) currentDetail.setBookName(mString);
			if ("storex".equals(currentTag)) currentDetail.setStorex(mString);
			if ("bookcode".equals(currentTag)) currentDetail.setBookCode(mString);
			if ("saleprice".equals(currentTag)) currentDetail.setSalePrice(mString);
			if ("saletime".equals(currentTag)) currentDetail.setSaleTime(mString);
		}
	}
}
