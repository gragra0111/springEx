package springbook.user.domain;

public class User {
	String id;
	String name;
	String password;
	Level lev;
	int login;
	int recommend;
	private String email;
	
	public User() {
		// TODO Auto-generated constructor stub
	}
	
	public User(String id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}
	
	public User(String id, String name, String password, Level lev, int login, int recommend, String email) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.lev = lev;
		this.login = login;
		this.recommend = recommend;
		this.email = email;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public Level getLevel() {
		return lev;
	}

	public void setLevel(Level level) {
		this.lev = level;
	}

	public int getLogin() {
		return login;
	}

	public void setLogin(int login) {
		this.login = login;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	//다음 레벨을 반환해주는 메소드
	public void upgradeLevel() {
		Level nextLevel = this.lev.nextLevel();
		if(nextLevel == null) {
			throw new IllegalStateException(this.lev + "은 업그레이드가 불가능합니다.");
		} else {
			this.lev = nextLevel;
		}
	}

}
