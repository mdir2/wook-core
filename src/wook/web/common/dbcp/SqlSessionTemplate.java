package wook.web.common.dbcp;

import java.io.Reader;
import java.lang.reflect.Method;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlSessionTemplate {
	private static SqlSessionFactory factory;
	public static SqlSessionTemplate instance;
	
	public static synchronized SqlSessionTemplate getInstance(String sqlMapConfig) {
		if(instance == null) instance = new SqlSessionTemplate(sqlMapConfig);
		return instance;
	}
	
	public SqlSessionTemplate() {}
	
	public SqlSessionTemplate(String config) {
		try {
			Reader reader = Resources.getResourceAsReader(config);
			factory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object selectOne(String queryId) {
		return execute("selectOne", queryId, null);
	}
	
	public Object selectOne(String queryId, Object param) {
		return execute("selectOne", queryId, param);
	}
	
	public Object selectList(String queryId) {
		return (Object) execute("selectList", queryId, null);
	}
	
	public Object selectList(String queryId, Object param) {
		return (Object) execute("selectList", queryId, param);
	}
	
	public int insert(String queryId) {
		return (int) execute("insert", queryId, null);
	}
	
	public int insert(String queryId, Object param) {
		return (int) execute("insert", queryId, param);
	}
	
	public int update(String queryId) {
		return (int) execute("update", queryId, null);
	}
	
	public int update(String queryId, Object param) {
		return (int) execute("update", queryId, param);
	}
	
	public int delete(String queryId) {
		return (int) execute("delete", queryId, null);
	}
	
	public int delete(String queryId, Object param) {
		return (int) execute("delete", queryId, param);
	}
	
	public Object execute(String methodNm, String queryId, Object param) {
		Object obj = null;
		SqlSession session = factory.openSession();
		Method method = null;
		try {
			if(param == null) {
				method = SqlSession.class.getMethod(methodNm, new Class[]{String.class});				
				obj = method.invoke(session, new Object[]{queryId});
			} else {
				method = SqlSession.class.getMethod(methodNm, new Class[]{String.class, Object.class});
				obj = method.invoke(session, new Object[]{queryId, param});
			}
			
			if(methodNm.contains("insert") || methodNm.contains("delete") || methodNm.contains("update")) session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(session != null) session.close();
		}
		return obj;
	}
}
