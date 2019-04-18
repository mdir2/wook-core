package wook.web.annotation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import wook.web.annotation.Controller;
import wook.web.annotation.Repository;
import wook.web.annotation.RequestMapping;
import wook.web.annotation.Service;

public class AnnotationUtil {

	/**
	 * to know that is Controller Class or not using Controller Annotation
	 */
	public static boolean isController(Class<?> clazz) {
		Controller controller = clazz.getAnnotation(Controller.class);
		if(controller != null) {
			return true;
		}
		return false;
	}
	
	public static boolean isFieldAnnotation(Class<?> clazz) {
		try {
			Annotation[] annArr = (Annotation[]) clazz.getDeclaredAnnotations();
			for (Annotation ann : annArr) {
				if(ann instanceof Resource) {
					return true;
				}
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isClassAnnotation(Class<?> clazz) {
		try {
			Annotation[] annArr = (Annotation[]) clazz.getDeclaredAnnotations();
			for (Annotation ann : annArr) {
				if((ann instanceof Repository || ann instanceof Service) && !clazz.isInterface()) {
					return true;
				}
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * to get Method using RequestMapping Annotation
	 */
	public static Method getRequestMapping(Class<?> clazz, String url) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			RequestMapping mapping = method.getAnnotation(RequestMapping.class);
			if(mapping != null) {
				for (String value : mapping.value()) {
					if(url.equals(value)) {
						return method;
					}
				}
			}
		}
		return null;
	}
}
