/**
 * 
 */
package core.fire;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将数据文件逐行解析为java bean
 * <p>
 * 解析规则
 * <p>
 * <ul>
 * <li>java类需提供常用getter\setter方法</li>
 * <li>数据文件前三行为元数据行，第一行为字段名，第二、三行自行使用，第四行开始是数据本身</li>
 * <li>列之间用tab键分隔</li>
 * <li>类字段命名应与数据文件字段名一致，若不一致应使用别名注解{@link PropertyAlias}将类字段映射到数据文件字段</li>
 * <li>数据文件配置的字段值不允许空着</li>
 * <li>数据文件支持UTF-8编码</li>
 * <li>自动跳过空行</li>
 * </ul>
 * 
 * @author lhl
 *
 *         2015年10月15日 下午3:48:51
 */
public class DataParser
{
    /**
     * 将数据文件按行解析为对象列表
     * 
     * @param path relative class path
     * @param type
     * @return
     * @throws RuntimeException
     */
    public static <T> List<T> parse(String path, Class<T> type) throws RuntimeException {
        try {
            // 1.将数据文件解析为map列表，一行数据代表一个map
            List<Map<String, String>> maps = TxtParser.parseDataToMap(path);

            // 2.预处理属性别名，准备解析器
            Map<String, String> keyToPropertyOverrides = findOverrideProperties(type);
            BeanProcessor beanProcessor = new BeanProcessor(keyToPropertyOverrides);

            // 3.实施解析操作
            return beanProcessor.toBeanList(maps, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查找属性别名
     * 
     * @param type
     * @return
     */
    private static <T> Map<String, String> findOverrideProperties(Class<T> type) {
        Map<String, String> ret = new HashMap<>();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            PropertyAlias anno = field.getAnnotation(PropertyAlias.class);
            if (anno != null) {
                ret.put(anno.name(), field.getName());
            }
        }
        return ret;
    }

    private static class TxtParser
    {
        // 带BOM的UTF-8格式文件头部3字节
        private static byte[] BOM_HEADER = { -17, -69, -65 };

        /**
         * 将数据文件按行解析为包含map的list，一行数据解析为一个map，map的key为数据字段名，value为字段值
         * <p>
         * 数据文件仅支持UTF8编码
         * 
         * @param path 数据文件相对类路径
         * @return
         * @throws IOException
         */
        private static List<Map<String, String>> parseDataToMap(String path) throws IOException {
            // 预处理文件，去除BOM文件头
            InputStream in = DataParser.class.getClassLoader().getResourceAsStream(path);
            if (in == null) {
                throw new IllegalArgumentException("文件" + path + "不存在");
            }

            preprocessBOM(in);

            try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(in))) {
                // 第一行，表头
                String tableHead = reader.readLine();
                String[] fields = tableHead.split("\t");
                // 跳过第二行(注释)
                reader.readLine();
                // 跳过第三行(字段类型)
                reader.readLine();

                List<Map<String, String>> ret = new ArrayList<>();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    ret.add(lineToMap(path, fields, line, reader.getLineNumber()));
                }
                return ret;
            }
        }

        private static Map<String, String> lineToMap(String path, String[] keys, String line, int lineNo) {
            String[] values = line.split("\t");

            if (values.length != keys.length) {
                String msg = "文件" + path + " key与value个数不一致, 行号: " + lineNo + ", key:" + Arrays.toString(keys)
                        + ", value:" + Arrays.toString(values);
                throw new IllegalStateException(msg);
            }

            Map<String, String> ret = new LinkedHashMap<>();
            for (int i = 0; i < keys.length; i++) {
                ret.put(keys[i], values[i]);
            }
            return ret;
        }

        /**
         * 预处理BOM，去除BOM文件头
         * 
         * @param in
         * @throws IOException
         */
        private static void preprocessBOM(InputStream in) throws IOException {
            byte[] bomHeader = new byte[3];
            in.mark(0);
            in.read(bomHeader);
            if (!Arrays.equals(bomHeader, BOM_HEADER)) {
                in.reset();
            }
        }
    }

    public static class BeanProcessor
    {
        private static final int PROPERTY_NOT_FOUND = -1;

        // key=属性别名，value=属性真名
        private final Map<String, String> keyToPropertyOverrides;

        public BeanProcessor() {
            keyToPropertyOverrides = new HashMap<>();
        }

        public BeanProcessor(Map<String, String> keyToPropertyOverride) {
            if (keyToPropertyOverride == null)
                throw new NullPointerException("keyToPropertyOverride");

            this.keyToPropertyOverrides = keyToPropertyOverride;
        }

        /**
         * 利用{@code maps}生成类{@code type}的多个实例
         * 
         * @param maps
         * @param type
         * @return
         * @throws Exception
         */
        public <T> List<T> toBeanList(List<Map<String, String>> maps, Class<T> type) throws Exception {
            List<T> ret = new ArrayList<>(maps.size());
            if (maps.isEmpty()) {
                return ret;
            }

            for (Map<String, String> map : maps) {
                ret.add(toBean(map, type));
            }

            return ret;
        }

        /**
         * 利用{@code properties}生成类{@code type}的实例
         * 
         * @param map
         * @param type
         * @return
         * @throws Exception
         */
        public <T> T toBean(Map<String, String> properties, Class<T> type) throws Exception {
            PropertyDescriptor[] props = propertyDescriptors(type);
            String[] keys = properties.keySet().toArray(new String[properties.size()]);
            int[] keyToProperty = mapKeysToProperties(keys, props);
            String[] vals = properties.values().toArray(new String[properties.size()]);
            return createBean(vals, type, props, keyToProperty);
        }

        /**
         * Returns a PropertyDescriptor[] for the given Class.
         *
         * @param c The Class to retrieve PropertyDescriptors for.
         * @return A PropertyDescriptor[] describing the Class.
         * @throws IntrospectionException
         */
        private PropertyDescriptor[] propertyDescriptors(Class<?> c) throws IntrospectionException {
            BeanInfo beanInfo = Introspector.getBeanInfo(c);
            return beanInfo.getPropertyDescriptors();
        }

        /**
         * @param keys 配置key
         * @param props 类属性
         * @return key到属性名位置的映射，比如第n个key对应的属性位置在key[n]
         * @throws Exception
         */
        private int[] mapKeysToProperties(String[] keys, PropertyDescriptor[] props) throws Exception {
            int cols = keys.length;
            int[] keysToProperty = new int[cols];
            Arrays.fill(keysToProperty, PROPERTY_NOT_FOUND);

            for (int col = 0; col < cols; col++) {
                String colName = keys[col];
                if (null == colName || 0 == colName.length()) {
                    throw new IllegalArgumentException("第" + (col + 1) + "个字段对应的key为空");
                }
                String propertyName = keyToPropertyOverrides.get(colName);
                if (propertyName == null) {
                    propertyName = colName;
                }
                for (int i = 0; i < props.length; i++) {
                    if (propertyName.equalsIgnoreCase(props[i].getName())) {
                        keysToProperty[col] = i;
                        break;
                    }
                }
            }
            return keysToProperty;
        }

        /**
         * 创建实例
         * 
         * @param values
         * @param type
         * @param props
         * @param keyToProperty
         * @return
         * @throws Exception
         */
        private <T> T createBean(String[] values, Class<T> type, PropertyDescriptor[] props, int[] keyToProperty)
                throws Exception {
            T bean = type.newInstance();

            for (int i = 0; i < keyToProperty.length; i++) {
                if (keyToProperty[i] == PROPERTY_NOT_FOUND)
                    continue;

                PropertyDescriptor prop = props[keyToProperty[i]];

                processField(bean, prop, values[i]);
            }

            return bean;
        }

        /**
         * 字段赋值
         * 
         * @param bean
         * @param prop
         * @param strVal
         */
        private <T> void processField(T bean, PropertyDescriptor prop, String strVal) {
            Class<?> propType = prop.getPropertyType();

            Object value = null;
            if (propType != null) {
                value = processValue(strVal, propType);
            }

            try {
                callSetter(bean, prop, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                String out = "无法将值[" + strVal + "]赋值到类[" + bean.getClass() + "]实例的[" + prop.getName() + "]字段，请检查类型是否匹配";
                throw new IllegalStateException(out, e);
            }
        }

        /**
         * 解析String类型的值为具体属性类型
         * 
         * @param val
         * @param propType
         * @return
         */
        private Object processValue(String val, Class<?> propType) {
            if (!propType.isPrimitive() && val == null) {
                return null;
            }

            if (propType.equals(String.class)) {
                return val;
            } else if (propType.equals(Integer.TYPE) || propType.equals(Integer.class)) {
                return Integer.valueOf(val);
            } else if (propType.equals(Boolean.TYPE) || propType.equals(Boolean.class)) {
                return Boolean.valueOf(val);
            } else if (propType.equals(Long.TYPE) || propType.equals(Long.class)) {
                return Long.valueOf(val);
            } else if (propType.equals(Double.TYPE) || propType.equals(Double.class)) {
                return Double.valueOf(val);
            } else if (propType.equals(Float.TYPE) || propType.equals(Float.class)) {
                return Float.valueOf(val);
            } else if (propType.equals(Short.TYPE) || propType.equals(Short.class)) {
                return Short.valueOf(val);
            } else if (propType.equals(Byte.TYPE) || propType.equals(Byte.class)) {
                return Byte.valueOf(val);
            } else {
                return val;
            }
        }

        /**
         * Calls the setter method on the target object for the given property.
         * If no setter method exists for the property, this method does
         * nothing.
         * 
         * @param target The object to set the property on.
         * @param prop The property to set.
         * @param value The value to pass into the setter.
         * @throws InvocationTargetException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        private void callSetter(Object target, PropertyDescriptor prop, Object value)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Method setter = prop.getWriteMethod();

            if (setter == null) {
                return;
            }

            Class<?>[] params = setter.getParameterTypes();
            if (value instanceof String && params[0].isEnum()) {
                value = Enum.valueOf(params[0].asSubclass(Enum.class), (String) value);
            }

            // Don't call setter if the value object isn't the right type
            if (this.isCompatibleType(value, params[0])) {
                setter.invoke(target, new Object[] { value });
            } else {
                throw new IllegalArgumentException(
                        "Cannot set " + prop.getName() + ": incompatible types, cannot convert "
                                + value.getClass().getName() + " to " + params[0].getName());
                // value cannot be null here because isCompatibleType allows
                // null
            }
        }

        /**
         * The setter method for the property might take an Integer or a
         * primitive int. This method returns true if the value can be
         * successfully passed into the setter method. Remember, Method.invoke()
         * handles the unwrapping of Integer into an int.
         *
         * @param value The value to be passed into the setter method.
         * @param type The setter's parameter type (non-null)
         * @return boolean True if the value is compatible (null => true)
         */
        private boolean isCompatibleType(Object value, Class<?> type) {
            if (value == null || type.isInstance(value)) {
                return true;
            } else if (type.equals(Integer.TYPE) && value instanceof Integer) {
                return true;
            } else if (type.equals(Long.TYPE) && value instanceof Long) {
                return true;
            } else if (type.equals(Double.TYPE) && value instanceof Double) {
                return true;
            } else if (type.equals(Float.TYPE) && value instanceof Float) {
                return true;
            } else if (type.equals(Short.TYPE) && value instanceof Short) {
                return true;
            } else if (type.equals(Byte.TYPE) && value instanceof Byte) {
                return true;
            } else if (type.equals(Character.TYPE) && value instanceof Character) {
                return true;
            } else if (type.equals(Boolean.TYPE) && value instanceof Boolean) {
                return true;
            }
            return false;
        }
    }

    /**
     * 字段别名，当类字段名与数据文件字段名不一致时，用该注解加在类字段名上以关联类字段映射和数据文件字段
     * 
     * @author lhl
     *
     *         2016年1月9日 下午2:07:44
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PropertyAlias {
        String name();
    }
}
