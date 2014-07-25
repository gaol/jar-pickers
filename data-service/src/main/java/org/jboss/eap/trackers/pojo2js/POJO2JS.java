/**
 * 
 */
package org.jboss.eap.trackers.pojo2js;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lgao
 *
 * Class which is used to generate javascript for POJOs.
 * 
 */
public final class POJO2JS {

	private List<Class<?>> pojoClasses = new ArrayList<Class<?>>();
	
	public POJO2JS(){
		super();
	}
	
	public void addClass(Class<?> cls)
	{
		if (!pojoClasses.contains(cls))
		{
			pojoClasses.add(cls);
		}
	}
	
	public void addClass(String className)
	{
		try {
			Class<?> cls = Class.forName(className);
			addClass(cls);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return JavaScript string which represents the Java POJO.
	 * 
	 */
	public String toJS() 
	{
		StringBuilder sb = new StringBuilder();
		for (Class<?> cls: this.pojoClasses)
		{
			sb.append(cls2JS(cls));
		}
		return sb.toString();
	}

	private StringBuilder cls2JS(Class<?> cls) {
		StringBuilder sb = new StringBuilder();
		String jsFuncName = getjsFuncName(cls);
		sb.append("function " + jsFuncName + "(){}");
		sb.append(jsFuncName + ".prototype={");
		boolean isFirst = true;
		for (Field pojoField: getPojoFields(cls))
		{
			if(isFirst) {
				isFirst = false;
			}
			else {
				sb.append(",");
			}
			sb.append(jsFieldInit(pojoField));
		}
		sb.append("}");
		sb.append("\n");
		return sb;
	}

	private StringBuilder jsFieldInit(Field pojoField) {
		StringBuilder sb = new StringBuilder();
		sb.append(pojoField.getName());
		sb.append(":");
		sb.append(getDefaultValueString(pojoField));
		return sb;
	}

	private String getDefaultValueString(Field pojoField) {
		Class<?> cls = pojoField.getType();
		if (cls.isArray() || Iterable.class.isAssignableFrom(cls))
		{
			return "[]";
		}
		else
		{
			return "\'\'";
		}
	}

	/**
	 * Gets fields list which can be treated as a POJO field.
	 * (has getXXX() and setXXX() methods)
	 * 
	 */
	private List<Field> getPojoFields(Class<?> cls) {
		List<Field> result = new ArrayList<Field>();
		Field fields[] = cls.getDeclaredFields();
		for (Field field: fields)
		{
			String name = field.getName();
			boolean hasGetter = false;
			boolean hasSetter = false;
			try {
				cls.getMethod("get" + capitalize(name));
				hasGetter = true;
			} catch (NoSuchMethodException e) {
				try {
					cls.getMethod("is" + capitalize(name));
					hasGetter = true;
				} catch (NoSuchMethodException e1) {
					hasGetter = false;
				}
			}
			Class<?> fieldType = field.getType();
			try {
				cls.getMethod("set" + capitalize(name), fieldType);
				hasSetter = true;
			} catch (NoSuchMethodException e) {
				hasSetter = false;
			}
			if (hasGetter && hasSetter)
			{
				result.add(field);
			}
		}
		
		Class<?> parent = cls.getSuperclass();
		if (parent != null && !parent.equals(Object.class))
		{
			result.addAll(getPojoFields(parent));
		}
		return result;
	}
	
	private String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/**
	 * What the function name of the JavaScript should be.
	 * 
	 * It is the Class.getSimpleName() now. 
	 * 
	 */
	private String getjsFuncName(Class<?> cls) {
		return cls.getSimpleName();
	}
	
}
