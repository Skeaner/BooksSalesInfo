package one.skean.booksalesinfo.saxhandlers;

import java.util.ArrayList;

import one.skean.booksalesinfo.baseclass.BeanDefaultHandler;
import one.skean.booksalesinfo.bean.SaleCount;
import one.skean.booksalesinfo.bean.TopSale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SaleCountSAXHandler extends BeanDefaultHandler {
	private String currentTag;
	private SaleCount saleCount;
	private TopSale currentTopSale;
	private ArrayList<TopSale> currentList;

	public SaleCountSAXHandler() {
		saleCount = new SaleCount();
	}

	@Override
	public Object getBeanObject() {
		// TODO Auto-generated method stub
		return saleCount;
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("完成解析");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
		currentTag = localName;
		if (currentTag.equals("schoolList") || currentTag.equals("cityList")
			|| currentTag.equals("bookList")) {
			currentList = new ArrayList<TopSale>();
		}
		if (currentTag.equals("topSale")) {
			currentTopSale = new TopSale();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentTag = null;
		if (localName.equals("schoolList")) saleCount.setSchoolList(currentList);
		if (localName.equals("cityList")) saleCount.setCityList(currentList);
		if (localName.equals("bookList")) saleCount.setBookList(currentList);
		if (localName.equals("topSale")) currentList.add(currentTopSale);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		String mString = new String(ch, start, length);
		System.out.println(currentTag + mString);
		if (saleCount != null) {
			if ("totalnum".equals(currentTag)) saleCount.setTotalnum(mString);
			if ("totalmoney".equals(currentTag)) saleCount.setTotalmoney(mString);
			if ("id".equals(currentTag)) currentTopSale.setId(mString);
			if ("name".equals(currentTag)) currentTopSale.setName(mString);
			if ("num".equals(currentTag)) currentTopSale.setNum(mString);
			if ("money".equals(currentTag)) currentTopSale.setMoney(mString);
		}
	}
}
