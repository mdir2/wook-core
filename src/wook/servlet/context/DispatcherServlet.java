package wook.servlet.context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wook.web.annotation.util.AnnotationUtil;

/**
 * @author wook
 */
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int initCnt = -1;
	public static final String REDIRECT = "redirect:";
	public static final String FORWARD = "forward:";
	public static final String PREFIX = "PREFIX";
	public static final String SUFFIX = "SUFFIX";
	private String contextConfigLocation;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		this.contextConfigLocation = context.getInitParameter("contextConfigLocation");
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}
	

	/**
	 * do process
	 */
	@SuppressWarnings("unchecked")
	protected void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		initCnt++;
		if(initCnt > 0) {
			RequestDispatcher dispatcher = null;
			Class<?> clazz = null;
			Method method = null;
			Object value = null;
			
			Map<String, Object> object = GenericBeanFactory.getInstance().getBeanMap();
			Iterator<String> iterator = object.keySet().iterator();
			String url = request.getRequestURI();
			
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				value = object.get(key);
				clazz = (Class<?>) value.getClass();
				
				if(AnnotationUtil.isController(clazz)) {
					if((method = AnnotationUtil.getRequestMapping(clazz, url)) != null) break;
				}
			}
			
			if(method != null) {
				try {
					Class<?>[] classArr = method.getParameterTypes();
					Object[] objArr = new Object[classArr.length];
					Enumeration<String> paramNames = request.getParameterNames();
					
					for (int i = 0; i < classArr.length; i++) {
						if(classArr[i].isInstance(request)) {
							objArr[i] = request;
						} else if(classArr[i].isInstance(response)) {
							objArr[i] = response;
						} else if(Map.class.isAssignableFrom(classArr[i])) {
							try {
								objArr[i] = (Map<String, Object>) classArr[i].newInstance();
								while(paramNames.hasMoreElements()) {
									String paramName = paramNames.nextElement();
									((Map<String, Object>) objArr[i]).put(paramName, request.getParameter(paramName));
								}
							} catch (InstantiationException e) {
								e.printStackTrace();
							}
						} else {
							try {
								Method[] methodArr = classArr[i].getMethods();
								objArr[i] = classArr[i].newInstance();
								
								for (Method method_ : methodArr) {
									if(method_.getName().startsWith("set")) {
										String initName = method_.getName().replace("set", "");
										StringBuilder stringBuilder = new StringBuilder(initName);
										stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.toString().charAt(0)));
										String methodName = stringBuilder.toString();
										
											Class<?> returnType = method_.getParameterTypes()[0];
											while (paramNames.hasMoreElements()) {
												String paramName = paramNames.nextElement();
												if(methodName.equals(paramName)) {
													if(returnType.getName().equals("int")) {
														method_.invoke(objArr[i], Integer.parseInt(request.getParameter(paramName)));
													} else if(returnType.getName().equals("long")) {
														method_.invoke(objArr[i], Long.parseLong(request.getParameter(paramName)));
													} else if(returnType.getName().equals("float")) {
														method_.invoke(objArr[i], Float.parseFloat(request.getParameter(paramName)));
													} else if(returnType.getName().equals("double")) {
														method_.invoke(objArr[i], Double.parseDouble(request.getParameter(paramName)));
													} else if(returnType.getName().equals("boolean")) {
														method_.invoke(objArr[i], Boolean.parseBoolean(request.getParameter(paramName)));
													} else if(returnType.isAssignableFrom(Object.class)) {
														method_.invoke(objArr[i], request.getParameter(paramName));
													} else {
														method_.invoke(objArr[i], request.getParameter(paramName));
													}
												}
											}
										paramNames = request.getParameterNames();
									}
								}
							} catch (InstantiationException e) {
								e.printStackTrace();
							}
						}
					}
					
					String view = (String) method.invoke(value, objArr);
					if(view.startsWith(REDIRECT) || view.startsWith(FORWARD)) {
						if(view.startsWith(REDIRECT)) {
							response.sendRedirect(view.replace(REDIRECT, ""));
						} else {
							view = view.replace(FORWARD, "");
						}
					} else {
						ResourceBundle resource = ResourceBundle.getBundle(contextConfigLocation);
						view = new StringBuilder()
									.append(resource.getString(PREFIX)) 
									.append(view)
									.append(resource.getString(SUFFIX)).toString();
					}
					dispatcher = request.getRequestDispatcher(view);
					dispatcher.forward(request, response);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}
}
