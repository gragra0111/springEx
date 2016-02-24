package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestAppContext.class, AppContext.class})
public class UserServiceTest {
	@Autowired
	ApplicationContext context;
	@Autowired
	UserService userService;
	@Autowired
	UserService testUserService;
	@Autowired
	UserDao userDao;
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired
	MailSender mailSender;
	List<User> users;
	
	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("1PARK","박영진","1",Level.BASIC,MIN_LOGCOUNT_FOR_SILVER-1,0, "park@naver.com"),
				new User("2GOO","구본식","2",Level.BASIC,MIN_LOGCOUNT_FOR_SILVER,0, "goo@naver.com"),
				new User("3JUNG","정광운","3",Level.SILVER,60,MIN_RECOMMEND_FOR_GOLD-1, "jung@naver.com"),
				new User("4GANG","강유성","4",Level.SILVER,60,MIN_RECOMMEND_FOR_GOLD, "gang@naver.com"),
				new User("5YOON","윤성진","5",Level.GOLD,100,Integer.MAX_VALUE, "yoon@naver.com")
		);
	}
	
	@Test
	public void upgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		MockUserDao mockUserDao = new MockUserDao(this.users);
		//userServiceImpl.setUserDao(mockUserDao);
		
		MockMailSender mockMailSender = new MockMailSender();
		//userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "2GOO", Level.SILVER);
		checkUserAndLevel(updated.get(1), "4GANG", Level.GOLD);
		
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel){
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}
	private void checkLevelUpgraded(User user, Boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if(upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
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
	
	//UserService의 테스트용 대역 클래스
	public static class TestUserService extends UserServiceImpl {
		private String id = "4GANG";
		
		protected void upgradeLevel(User user) {
			if(user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
		
		public List<User> getAll() {
			for(User user : super.getAll()) {
				super.update(user);
			}
			return null;
		}
	}
	
	@SuppressWarnings("serial")
	static class TestUserServiceException extends RuntimeException {}
	
	@Test/*(expected=TransientDataAccessResourceException.class)*/
	public void readOnlyTransactionAttribute() {
		testUserService.getAll();
	}
	
	@Test
	public void upgradeAllOrNothing() {
		userDao.deleteAll();
		for(User user : users) {
			userDao.add(user);
		}
		
		try {
			this.testUserService.upgradeLevels();
			fail("TestUserServiceException expected");	//TestUserService는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
		} catch(TestUserServiceException e) {	//TestUserService가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패
		}
		checkLevelUpgraded(users.get(1), false);	//예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인
	}
	
	//목 오브젝트로 만든 메일 전송 확인용 클래스
	static class MockMailSender implements MailSender {
		private List<String> requests = new ArrayList<String>();
		
		public List<String> getRequests() {
			return requests;
		}
		
		@Override
		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);
		}

		@Override
		public void send(SimpleMailMessage[] mailMessage) throws MailException {
			// TODO Auto-generated method stub
		}
	}
	
	//upgradeLevels 메소드의 단위 테스트를 위한 목 오브젝트
	static class MockUserDao implements UserDao {
		private List<User> users;
		private List<User> updated = new ArrayList<User>();
		
		private MockUserDao(List<User> users) {
			this.users = users;
		}
		
		public List<User> getUpdated() {
			return this.updated;
		}
		
		public List<User> getAll() { 
			return this.users;
		}
		
		public void update(User user) {
			updated.add(user);
		}
		
		@Override
		public void add(User user) { throw new UnsupportedOperationException(); }
		@Override
		public User get(String id) { throw new UnsupportedOperationException(); }
		@Override
		public void deleteAll() { throw new UnsupportedOperationException(); }
		@Override
		public int getCount() { throw new UnsupportedOperationException(); }
	}
	
	@Test
	public void transactionSync() {
		userService.deleteAll();
		
		userService.add(users.get(0));
		userService.add(users.get(1));
	}
	
}