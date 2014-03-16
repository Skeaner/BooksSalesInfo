package one.skean.booksalesinfo.bean;

import java.util.List;

public class SaleCount {

	private String totalnum; // 总销量
	private String totalmoney; // 总金额

	private List<TopSale> schoolList; // 前三名加盟商
	private List<TopSale> cityList; // 前三名城市
	private List<TopSale> bookList; // 前三名的书籍

	public String getTotalnum() {
		return totalnum;
	}

	public void setTotalnum(String totalnum) {
		this.totalnum = totalnum;
	}

	public String getTotalmoney() {
		return totalmoney;
	}

	public void setTotalmoney(String totalmoney) {
		this.totalmoney = totalmoney;
	}

	public List<TopSale> getSchoolList() {
		return schoolList;
	}

	public void setSchoolList(List<TopSale> schoolList) {
		this.schoolList = schoolList;
	}

	public List<TopSale> getCityList() {
		return cityList;
	}

	public void setCityList(List<TopSale> cityList) {
		this.cityList = cityList;
	}

	public List<TopSale> getBookList() {
		return bookList;
	}

	public void setBookList(List<TopSale> bookList) {
		this.bookList = bookList;
	}

}
