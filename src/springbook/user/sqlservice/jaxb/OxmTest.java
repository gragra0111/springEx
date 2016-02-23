package springbook.user.sqlservice.jaxb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration	//클래스 이름 + 'context.xml' 파일을 사용하는 애플리케이션 컨텍스트로 만들어서 테스트가 사용할 수 있게 해준다.
public class OxmTest {
	@Autowired
	Unmarshaller unmarshaller;
	
	@Test
	public void unmarshallSqlMap() throws XmlMappingException, IOException, JAXBException {
		Source xmlSource = new StreamSource(getClass().getResourceAsStream("sqlmap.xml"));	//InputStream을 이용하는 Source 타입의 StreamSource를 만든다.
		
		Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(xmlSource);	//어떤 OXM 기술이든 언마샬은 이 한 줄로 가능.
		
		List<SqlType> sqlList = sqlmap.getSql();
		
		assertThat(sqlList.size(), is(3));
		assertThat(sqlList.get(0).getKey(), is("add"));
		assertThat(sqlList.get(0).getValue(), is("insert"));
		assertThat(sqlList.get(1).getKey(), is("get"));
		assertThat(sqlList.get(1).getValue(), is("select"));
		assertThat(sqlList.get(2).getKey(), is("delete"));
		assertThat(sqlList.get(2).getValue(), is("delete"));
	}
}
