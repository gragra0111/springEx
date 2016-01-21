package springbook.user.service;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserService {
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
	private DataSource dataSource;
	UserDao userDao;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public void upgradeLevels() throws Exception {
		TransactionSynchronizationManager.initSynchronization();	//트랜잭션 동기화 관리자를 이용해 동기화 작업을 초기화한다.
		Connection c = DataSourceUtils.getConnection(dataSource);	//DB커넥션을 생성하고 (DB 커넥션 생성과 동기화를 함께 해주는 유틸리티 메소드)
		c.setAutoCommit(false);										//트랜잭션을 시작한다. 이후의 DAO 작업은 모두 여기서 시작한 트랜잭션 안에서 진행된다.
		
		try {
			List<User> users = userDao.getAll();
			for(User user : users) {
				if(canUpgradeLevel(user)) {
					upgradeLevel(user);
				}
			}
			c.commit();	//정상적으로 작업을 마치면 트랜잭션 커밋
		} catch(Exception e) {
			c.rollback();	//예외가 발생하면 롤백한다.
			throw e;
		} finally {
			DataSourceUtils.releaseConnection(c, dataSource);	//스프링 유틸리티 메소드를 이용해 DB 커넥션을 안전하게 닫는다.
			TransactionSynchronizationManager.unbindResource(this.dataSource);	//동기화 작업 종료
			TransactionSynchronizationManager.clearSynchronization();			//동기화 작업 정리
		}
	}

	private boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		switch(currentLevel) {
			case BASIC : return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
			case SILVER : return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
			case GOLD : return false;
			default : throw new IllegalArgumentException("Unknown Level : " + currentLevel);
		}
	}

	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		userDao.update(user);
	}
	
	public void add(User user) {
		if(user.getLevel() == null) {
			user.setLevel(Level.BASIC);
		}
		userDao.add(user);
	}
	
	
	
	
	
}
