package wook.servlet.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import wook.web.common.dbcp.SqlSessionTemplate;

/**
 * @author wook
 */
public class ContextLoader implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		String contextConfigLocation = servletContext.getInitParameter("contextConfigLocation");
		String sqlMapConfig = servletContext.getInitParameter("sqlMapConfig");
		
		SqlSessionTemplate sqlSessionTemplate = SqlSessionTemplate.getInstance(sqlMapConfig);
		BeanFactory beanFactory = new BeanFactory(){};
		beanFactory.setSqlSessionTemplate(sqlSessionTemplate);
		
		beanFactory = GenericBeanFactory.getInstance(contextConfigLocation);
		beanFactory.setUnmodifiable(BeanFactory.objectMap);
	}
}
