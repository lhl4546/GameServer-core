/**
 * 
 */
package core.fire;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * 将properties配置文件内容映射到类中，properties的key必须与类字段名完全一致
 * 
 * @author lhl
 *
 *         2015年7月7日下午2:54:50
 */
public class ConfigParser
{
    /**
     * 从类路径加载配置并映射到类
     * 
     * @param file
     * @param clas
     * @throws RuntimeException
     */
    public static void parseFromClassPath(String file, Class<?> clas) throws RuntimeException {
        try {
            Properties prop = loadFromClassPath(file);
            assignment(clas, prop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从类路径加载配置
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private static Properties loadFromClassPath(String file) throws IOException {
        Properties prop = new Properties();
        try (InputStream inStream = ConfigParser.class.getClassLoader().getResourceAsStream(file)) {
            prop.load(inStream);
        }
        return prop;
    }

    /**
     * 给字段赋值
     * 
     * @param obj
     * @param prop
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void assignment(Class<?> obj, Properties prop)
            throws IllegalArgumentException, IllegalAccessException {
        Field[] allFields = obj.getFields();
        for (Field field : allFields) {
            Class<?> type = field.getType();
            String fieldName = field.getName();
            String value = prop.getProperty(fieldName);
            if (type == boolean.class) {
                field.setBoolean(obj, Boolean.parseBoolean(value));
            } else if (type == byte.class) {
                field.setByte(obj, Byte.parseByte(value));
            } else if (type == short.class) {
                field.setShort(obj, Short.parseShort(value));
            } else if (type == int.class) {
                field.setInt(obj, Integer.parseInt(value));
            } else if (type == float.class) {
                field.setFloat(obj, Float.parseFloat(value));
            } else if (type == double.class) {
                field.setDouble(obj, Double.parseDouble(value));
            } else if (type == long.class) {
                field.setLong(obj, Long.parseLong(value));
            } else if (type == String.class) {
                field.set(obj, value);
            }
        }
    }
}
