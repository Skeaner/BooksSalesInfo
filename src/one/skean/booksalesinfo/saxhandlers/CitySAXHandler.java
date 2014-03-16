package one.skean.booksalesinfo.saxhandlers;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import one.skean.booksalesinfo.baseclass.BeanDefaultHandler;
import one.skean.booksalesinfo.bean.City;

public class CitySAXHandler extends BeanDefaultHandler {
	private List<City> cities;
	private String currentTag;
	private City currentCity;

	public CitySAXHandler() {
		cities = new ArrayList<City>();
	}

	@Override
	public Object getBeanObject() {
		// TODO Auto-generated method stub
		return cities;
	}

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
		if (currentTag.equals("city")) currentCity = new City();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("city")) {
			cities.add(currentCity);
			currentCity = null;
		}
		currentTag = null;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		String mString = new String(ch, start, length);
		if (currentCity != null) {
			if ("id".equals(currentTag)) currentCity.setId(mString);
			if ("name".equals(currentTag)) currentCity.setName(mString);
		}
	}
}
