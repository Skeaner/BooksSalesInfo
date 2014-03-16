package one.skean.booksalesinfo.bean;

public class Login {
	
	private int userId;
	private String username;
	private String lastlogin;
	private boolean ispass;
	private String resultMessage;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public boolean isIspass() {
		return ispass;
	}
	public void setIspass(boolean ispass) {
		this.ispass = ispass;
	}
	public String getResultMessage() {
		return resultMessage;
	}
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getLastlogin() {
		return lastlogin;
	}
	public void setLastlogin(String lastlogin) {
		this.lastlogin = lastlogin;
	}
	
}
