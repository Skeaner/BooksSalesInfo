package one.skean.booksalesinfo.util;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;


public class SaxXmlTools {


	public static void parseWithHandler(InputStream instream, DefaultHandler handler)throws Exception{
		SAXParserFactory spf=SAXParserFactory.newInstance();
		SAXParser saxParser=spf.newSAXParser();
		saxParser.parse(instream, handler);
		instream.close();
	}
}