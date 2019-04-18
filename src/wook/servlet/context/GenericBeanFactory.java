package wook.servlet.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.annotation.Resource;

import wook.web.annotation.util.AnnotationUtil;

/**
 * @author wook
 */
public class GenericBeanFactory extends BeanFactory {
	
	private static Logger log = Logger.getGlobal();
	
	public static GenericBeanFactory instance;
	
	public static synchronized GenericBeanFactory getInstance() {
		if(instance == null) instance = new GenericBeanFactory();
		return instance;
	}
	
	public static synchronized GenericBeanFactory getInstance(String contextConfigLocation) {
		setBean(contextConfigLocation);
		return getInstance();
	}
	
	private GenericBeanFactory() {}
	
	/**
	 * to set Bean using config
	 */
	private static synchronized void setBean(String contextConfigLocation) {		
		Object[] classArr = getClazz(contextConfigLocation);
		
		for (int i = 0; i < classArr.length; i++) {
			try {
				Class<?> clazz = Class.forName(classArr[i].toString());
				makeBean(clazz, false);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		Iterator<String> interfaceIter = interfaceSet.iterator();
		Iterator<String> objectIter = objectMap.keySet().iterator();
		Iterator<String> classIter = classMap.keySet().iterator();
		
		while (interfaceIter.hasNext()) { // interface find
			try {
				String key = interfaceIter.next();
				Class<?> interface_ = Class.forName(key);
				
				while (objectIter.hasNext()) {
					String objectKey = objectIter.next();
					Object subObject = objectMap.get(objectKey);
					Class<?> subClass = subObject.getClass();
					
					if(interface_.isAssignableFrom(subClass)) {
						objectMap.put(key, subObject);
						objectIter = objectMap.keySet().iterator();
						break;
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		while (classIter.hasNext()) {
			String key = classIter.next();
			Class<?> clazz = (Class<?>) classMap.get(key);
			makeBean(clazz, true);
		}
		
		log.info("Container is created " + objectMap.keySet());
	}
	
	/**
	 * to make Bean
	 */
	private static void makeBean(Class<?> clazz, boolean isClassMap) {
		Object bean = null;
		Field[] fieldArr = clazz.getDeclaredFields();
		if(clazz.isInterface()) {
			interfaceSet.add(clazz.getName());
		} else { // clazz.isInterface == false
			if(fieldArr.length <= 0) {
				if(AnnotationUtil.isClassAnnotation(clazz)) {
					try {
						objectMap.put(clazz.getName(), clazz.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			} else {
				for (int i = 0; i < fieldArr.length; i++) {
					Annotation[] annArr = fieldArr[i].getAnnotations();
					if(annArr.length <= 0) {
						try {
							if(AnnotationUtil.isClassAnnotation(clazz)) objectMap.put(clazz.getName(), clazz.newInstance());
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						for (int j = 0; j < annArr.length; j++) {
							if(annArr[j] instanceof Resource) {
								Class<?> fieldClazz = fieldArr[i].getType();
								
								if(fieldClazz.isInterface() && !isClassMap) {
									classMap.put(clazz.getName(), clazz);
									interfaceSet.add(fieldClazz.getName());
									continue;
								} else {
									if(!objectMap.containsKey(fieldClazz.getName())) {
										if(!clazz.isInterface()) {
											makeBean(fieldClazz, isClassMap);
											j--;
										} else {
											return;
										}
									} else {
										try {
											fieldArr[i].setAccessible(true);
											bean = objectMap.get(clazz.getName());
											
											if(bean == null) bean = clazz.newInstance();
											
											Object value = fieldArr[i].get(bean);
											value = objectMap.get(fieldClazz.getName());
											fieldArr[i].set(bean, value);
											objectMap.put(clazz.getName(), bean);
										} catch (InstantiationException | IllegalAccessException e) {
											e.printStackTrace();
										}
									}
								}
							}
						} // for annArr close
					}
				} // for fieldArr close
			}
		}
	}
}
