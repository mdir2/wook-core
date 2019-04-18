package wook.servlet.context;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import wook.web.common.dbcp.SqlSessionTemplate;

/**
 * @author wook
 */
public abstract class BeanFactory {
	protected static Map<String, Object> objectMap = new LinkedHashMap<>();
	protected static Map<String, Object> classMap = new LinkedHashMap<>();
	protected static Set<String> interfaceSet = new LinkedHashSet<>();
	public static final String SCAN_PATH = "SCAN_PATH";
	
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		objectMap.put(SqlSessionTemplate.class.getName(), sqlSessionTemplate);
	}
	
	protected void setBean(String key, Object value) {
		objectMap.put(key, value);
	}
	
	public Object getBean(Class<?> clazz) {
		return objectMap.get(clazz.getName());
	}
	
	public Map<String, Object> getBeanMap() {
		return objectMap;
	}
	
	private static Set<String> getObject(String packageNm) {
		Set<String> classes = new HashSet<>();
		packageNm = "./" + packageNm.replace(".", "/");
		URL url = Thread.currentThread().getContextClassLoader().getResource(packageNm);
		
		if (url == null) {
			return null;
		} else {
			String target = url.getFile();
			if(target == null) {
				return null;
				
			} else {
				File directory = new File(target);
				getClassName(directory, classes);
			}
		}
		return classes;
	}
	
	protected static Object[] getClazz(String contextConfigLocation) {
		return getObject(ResourceBundle.getBundle(contextConfigLocation).getString(SCAN_PATH)).toArray();
	}
	
	private static String getFileName(File file) {
		String fileNm = file.getName();
		String exts = ".class";
		String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath().replaceAll("\\/", "\\\\");
		String filePath = new StringBuilder("\\").append(file.getPath()).toString();
		String filePackage = filePath.replace(classPath, "").replaceAll("\\\\", ".").replace(exts, "");
		
		if(fileNm.endsWith(exts))
			return filePackage;
		else
			return null;
	}
	
	private static void getClassName(File file, Set<String> classes) {
		if(file.isDirectory()) {
			File[] paths = file.listFiles();
			for (File path : paths) {
				getClassName(path, classes);
			}
		} else {
			String fileNm = getFileName(file);
			if(fileNm != null) classes.add(fileNm);
		}
	}
	
	protected void setUnmodifiable(Map<String, Object> objectMap) {
		objectMap = Collections.unmodifiableMap(objectMap);
	}
}
