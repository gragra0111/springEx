package springbook.user.service;

import static springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserService.MIN_RECOMMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserServiceTest {
	@Autowired
	UserService userService;
	@Autowired
	UserDao userDao;
	@Autowired
	DataSource dataSource;
	List<User> users;
	
	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("1PARK","박영진","1",Level.BASIC,MIN_LOGCOUNT_FOR_SILVER-1,0),
				new User("2GOO","구본식","2",Level.BASIC,MIN_LOGCOUNT_FOR_SILVER,0),
				new User("3JUNG","정광운","3",Level.SILVER,60,MIN_RECOMMEND_FOR_GOLD-1),
				new User("4GANG","강유성","4",Level.SILVER,60,MIN_RECOMMEND_FOR_GOLD),
				new User("5YOON","윤성진","5",Level.GOLD,100,Integer.MAX_VALUE)
		);
	}
	
/*
	@Test
	public void upgradeLevels() {
		userDao.deleteAll();
		for(User user : users) {
			userDao.add(user);
		}
		
		userService.upgradeLevels();
		
		checkLevelUpgraded(users.get(0), false);
		checkLevelUpgraded(users.get(1), true);
		checkLevelUpgraded(users.get(2), false);
		checkLevelUpgraded(users.get(3), true);
		checkLevelUpgraded(users.get(4), false);
	}
*/
/*
	@Test
	public void add() {
		userDao.deleteAll();
		
		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}
*/	
	private void checkLevelUpgraded(User user, Boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if(upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	//UserService의 테스트용 대역 클래스
	static class TestUserService extends UserService {
		private String id;
		
		private TestUserService(String id) {
			this.id = id;
		}
		
		protected void upgradeLevel(User user) {
			System.out.println(user.getId() + " : " + id);
			if(user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}
	
	static class TestUserServiceException extends RuntimeException {	
	}
	
	@Test
	public void upgradeAllOrNothing() throws Exception {
		UserService testUserService = new TestUserService(users.get(3).getId());	//예외를 발생시킬 네 번째 사용자의 id를 넣어서 테스트용 UserService 대역 오브젝트를 생성한다.
		testUserService.setUserDao(this.userDao);	//userDao를 수동 DI 해준다.
		testUserService.setDataSource(this.dataSource);
		userDao.deleteAll();
		for(User user : users) {
			userDao.add(user);
		}
		
		try {
			testUserService.upgradeLevels();
			fail("TestUserServiceException expected");	//TestUserService는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
		} catch(TestUserServiceException e) {	//TestUserService가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패
			
		}
		checkLevelUpgraded(users.get(1), false);	//예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인
	}

}