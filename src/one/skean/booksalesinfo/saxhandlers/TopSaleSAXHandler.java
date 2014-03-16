package one.skean.booksalesinfo.saxhandlers;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import one.skean.booksalesinfo.baseclass.BeanDefaultHandler;
import one.skean.booksalesinfo.bean.TopSale;

public class TopSaleSAXHandler extends BeanDefaultHandler {
	private String currentTag;
	private List<TopSale> rankingList;
	private TopSale currentTopSale;

	public TopSaleSAXHandler() {
		rankingList = new ArrayList<TopSale>();
	}

	public Object getBeanObject() {
		return rankingList;
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
		System.out.println("完成解析");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
		currentTag = localName;
		if (currentTag.equals("topSale")) currentTopSale = new TopSale();
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("topSale")) {
			rankingList.add(currentTopSale);
			currentTopSale = null;
		}
		currentTag = null;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String mString = new String(ch, start, length);
		if (currentTopSale != null) {
			if ("id".equals(currentTag)) currentTopSale.setId(mString);
			if ("name".equals(currentTag)) currentTopSale.setName(mString);
			if ("num".equals(currentTag)) currentTopSale.setNum(mString);
			if ("money".equals(currentTag)) currentTopSale.setMoney(mString);
		}
	}
}
