package springbook.learningtest.jdk.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

import springbook.learningtest.jdk.UppercaseHandler;

public class DynamicProxyTest {
	@Test
	public void simpleProxy() {
		Hello proxiedHello = (Hello)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Hello.class}, new UppercaseHandler(new HelloTarget()));
	}
	
	@Test
	public void proxyFactoryBean() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		pfBean.addAdvice(new UppercaseAdvice());
		
		Hello proxidHello = (Hello) pfBean.getObject();
		
		assertThat(proxidHello.sayHello("Toby"), is("HELLO TOBY"));
		assertThat(proxidHello.sayHi("Toby"), is("HI TOBY"));
		assertThat(proxidHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
	}
	
	static class UppercaseAdvice implements MethodInterceptor {
		public Object invoke(MethodInvocation invocation) throws Throwable {
			String ret = (String)invocation.proceed();
			return ret.toUpperCase();
		}
	}
	
	static interface Hello {
		String sayHello(String name);
		String sayHi(String name);
		String sayThankYou(String name);
	}
	
	static class HelloTarget implements Hello {
		public String sayHello(String name) {return "Hello " + name;};
		public String sayHi(String name) {return "Hi " + name;};
		public String sayThankYou(String name) {return "Thank You " + name;};
	}
	
	@Test
	public void pointcutAdvisor() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		pointcut.setMappedName("sayH*");
		
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		
		Hello proxidHello = (Hello) pfBean.getObject();
		
		assertThat(proxidHello.sayHello("Toby"), is("HELLO TOBY"));
		assertThat(proxidHello.sayHi("Toby"), is("HI TOBY"));
		assertThat(proxidHello.sayThankYou("Toby"), is("Thank You Toby"));
	}
	
	@Test
	public void classNamePointcutAdvisor() {
		//포인트컷 준비
		NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut() {
			public ClassFilter getClassFilter() {	//익명 내부 클래스 방식으로 클래스 정의
				return new ClassFilter() {
					public boolean matches(Class<?> clazz) {
						return clazz.getSimpleName().startsWith("HelloT");	//클래스 이름이 HelloT로 시작하는 것만 선정한다.
					}
				};
			}
		};
		classMethodPointcut.setMappedName("sayH*");	//sayH로 시작하는 메소드 이름을 가진 메소드만 선정한다.
		
		//테스트
		chackAdviced(new HelloTarget(), classMethodPointcut, true);
		
		class HelloWorld extends HelloTarget {};
		chackAdviced(new HelloWorld(), classMethodPointcut, true);
		
		class HelloToby extends HelloTarget {};
		chackAdviced(new HelloToby(), classMethodPointcut, true);
	}

	private void chackAdviced(Object target, Pointcut pointcut, boolean adviced) {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(target);
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		if(adviced) {
			assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
			assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
			assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
		} else {
			assertThat(proxiedHello.sayHello("Toby"), is("Hello Toby"));
			assertThat(proxiedHello.sayHi("Toby"), is("Hi Toby"));
			assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
		}
	}
}
