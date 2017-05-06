/**
 * 
 */
package core.fire.txt;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将数据文件逐行解析为java bean
 * <p>
 * 解析规则
 * <p>
 * <ul>
 * <li>java类需提供常用getter\setter方法</li>
 * <li>数据文件第一行必须为字段名，其他表头行数自定义</li>
 * <li>列之间用tab键分隔</li>
 * <li>类字段命名应与数据文件字段名一致，若不一致应使用别名注解{@link PropertyAlias}将类字段映射到数据文件字段</li>
 * <li>字段允许填"null"或者留空</li>
 * <li>数据文件只支持UTF-8编码</li>
 * <li>自动跳过空行</li>
 * </ul>
 * 支持以下特性
 * <ul>
 * <li>IntArray类型。若配置字段为IntArray类型(形如1;2;3，特征是用分号分隔的数字)，则将对应的Java类字段定义为int[]
 * 可实现自动解析，即将IntArray解析为int[]</li>
 * <li>FloatArray类型。若配置字段为FloatArray类型(形如1.1;2.2;3.3，特征是用分号分隔的数字)，
 * 则将对应的Java类字段定义为float[] 可实现自动解析，即将FloatArray解析为float[]</li>
 * <li>枚举类型。支持Enum#valueOf(枚举常量名)</li>
 * </ul>
 * 以上特殊类型空值填"null"字符串
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
     * @param path 类路径文件名
     * @param type 对应的bean类型
     * @param skipLines 表头需要跳过的行数
     * @return
     * @throws RuntimeException
     */
    public static <T> List<T> parse(String path, Class<T> type, int skipLines) throws RuntimeException {
        try {
            List<Map<String, String>> maps = TxtParser.parse(path, skipLines);
            return BeanProcessor.of(type).populate(maps, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 将txt文件解析为k-v集合
    public static class TxtParser
    {
        // 文件最大200M
        static int MAX_FILE_SIZE = 1024 * 1024 * 200;
        // 带BOM的UTF-8格式文件头部3字节
        static byte[] BOM_HEADER = { -17, -69, -65 };

        /**
         * 将数据文件解析为list，list元素为map，map的key为字段名，value为相应值 <br>
         * 文件支持UTF8格式 <br>
         * 解析规则：文件第一行必须为字段名定义
         * 
         * @param path 文件相对类路径
         * @param skipLines 要跳过的表头行数
         * @return
         * @throws IOException
         * @throws URISyntaxException
         */
        public static List<Map<String, String>> parse(String file, int skipLines) throws IOException, URISyntaxException {
            List<String> allLines = getAllLines(file);
            String[] fields = allLines.get(0).split("\t");
            List<String> content = allLines.subList(skipLines, allLines.size());

            return content.stream().map(line -> lineToMap(fields, line)).collect(Collectors.toList());
        }

        // 文件 -> 行
        static List<String> getAllLines(String file) throws URISyntaxException, IOException {
            URL url = DataParser.class.getClassLoader().getResource(file);
            Path path = Paths.get(url.toURI());
            if (Files.size(path) > MAX_FILE_SIZE) {
                throw new RuntimeException("File \"" + file + "\" is too large");
            }

            try (Stream<String> lineStream = Files.lines(path)) {
                return lineStream.map(line -> eraseUTF8BOMHeader(line)).collect(Collectors.toList());
            }
        }

        // 擦除UTF8 BOM头
        static String eraseUTF8BOMHeader(String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= 3) {
                if (bytes[0] == BOM_HEADER[0] && bytes[1] == BOM_HEADER[1] && bytes[2] == BOM_HEADER[2]) {
                    return new String(Arrays.copyOfRange(bytes, 3, bytes.length));
                }
            }
            return value;
        }

        // 将字符串转换为k-v映射
        static Map<String, String> lineToMap(String[] keys, String line) {
            String[] values = line.split("\t", -1);

            if (values.length != keys.length) {
                String msg = "key与value个数不一致, key:" + Arrays.toString(keys) + ", value:" + Arrays.toString(values);
                throw new IllegalStateException(msg);
            }

            Map<String, String> ret = new LinkedHashMap<>();
            for (int i = 0; i < keys.length; i++) {
                ret.put(keys[i], values[i]);
            }
            return ret;
        }
    }

    /**
     * Bean处理器，使用反射将Map#字段名, 字段值#映射为一个java 类
     * 
     * @author lhl
     *
     *         2016年4月29日 下午5:14:08
     */
    public static class BeanProcessor
    {
        private static final int PROPERTY_NOT_FOUND = -1;
        private static final String EMPTY_VALUE1 = "null";
        private static final String EMPTY_VALUE2 = "";
        private static int[] EMPTY_INT_ARRAY = new int[0];
        private static float[] EMPTY_FLOAT_ARRAY = new float[0];

        // key=属性别名，value=属性真名
        private Map<String, String> keyToPropertyOverrides;
        private Class<?> protoType;

        private BeanProcessor(Class<?> protoType) {
            this.protoType = Objects.requireNonNull(protoType);
            this.keyToPropertyOverrides = findOverrideProperties(this.protoType);
        }

        /**
         * 查找属性别名
         * 
         * @param type
         * @return key=配置文件字段名，value=相应类字段名
         */
        private static <T> Map<String, String> findOverrideProperties(Class<T> type) {
            Map<String, String> ret = new HashMap<>();
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                PropertyAlias anno = field.getAnnotation(PropertyAlias.class);
                if (anno != null) {
                    ret.put(anno.value().toLowerCase(), field.getName());
                }
            }
            return ret;
        }

        /**
         * 生成BeanProcessor实例
         * 
         * @param protoType 目标类型
         * @return
         */
        public static BeanProcessor of(Class<?> protoType) {
            return new BeanProcessor(protoType);
        }

        /**
         * k-v数据转换成对象 批量
         * 
         * @param maps
         * @param type
         * @return
         * @throws RuntimeException
         */
        public <T> List<T> populate(List<Map<String, String>> maps, Class<T> type) throws RuntimeException {
            return maps.stream().map(x -> populate(x, type)).collect(Collectors.toList());
        }

        /**
         * k-v数据转换成对象
         * 
         * @param properties
         * @param type
         * @return
         * @throws RuntimeException
         */
        public <T> T populate(Map<String, String> properties, Class<T> type) {
            try {
                PropertyDescriptor[] props = propertyDescriptors(type);
                String[] keys = properties.keySet().toArray(new String[properties.size()]);
                int[] keyToProperty = mapKeysToProperties(keys, props);
                String[] vals = properties.values().toArray(new String[properties.size()]);
                return createBean(vals, type, props, keyToProperty);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
                String propertyName = keyToPropertyOverrides.get(colName.toLowerCase());
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
        private <T> T createBean(String[] values, Class<T> type, PropertyDescriptor[] props, int[] keyToProperty) throws Exception {
            T bean = type.newInstance();

            for (int i = 0; i < keyToProperty.length; i++) {
                if (keyToProperty[i] == PROPERTY_NOT_FOUND)
                    continue;

                PropertyDescriptor prop = props[keyToProperty[i]];
                populateField(bean, prop, values[i]);
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
        private <T> void populateField(T bean, PropertyDescriptor prop, String strVal) {
            Class<?> propType = prop.getPropertyType();
            Object targetVal = null;
            if (propType != null) {
                if (isEmpty(strVal) && propType.isPrimitive()) {
                    return; // 基础类型字段且被留空则不赋值(使用默认值)
                }

                targetVal = parseValueByType(strVal, propType);
            }

            try {
                callSetter(bean, prop, targetVal);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                String message = "无法将值[" + strVal + "]赋值到类[" + bean.getClass() + "]实例的[" + prop.getName() + "]字段，字段类型[]" + propType + "，请检查类型是否匹配";
                throw new IllegalStateException(message, e);
            }
        }

        /**
         * 解析String类型的值为具体属性类型
         * 
         * @param val
         * @param propType
         * @return
         */
        private Object parseValueByType(String val, Class<?> propType) {
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
            } else if (propType.equals(int[].class)) {
                return toIntArray(val);
            } else if (propType.equals(float[].class)) {
                return toFloatArray(val);
            } else {
                return val;
            }
        }

        /**
         * 空值，允许填"null"或者留空以表示字段使用默认值
         * 
         * @param value
         * @return
         */
        private boolean isEmpty(String value) {
            return value == null || EMPTY_VALUE1.equals(value) || EMPTY_VALUE2.equals(value);
        }

        /**
         * 特定配置类型，格式:a;b;c;d，其中a\b\c\d必须为int类型。该方法将特定格式的字符串解析为int数组
         * 
         * @param val
         * @return
         */
        private int[] toIntArray(String val) {
            if (isEmpty(val)) {
                return EMPTY_INT_ARRAY;
            }

            String[] tmp = val.split(";");
            int[] ret = new int[tmp.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = Integer.parseInt(tmp[i]);
            }
            return ret;
        }

        /**
         * 特定配置类型，格式:a;b;c;d，其中a\b\c\d必须为float类型。该方法将特定格式的字符串解析为float数组
         * 
         * @param val
         * @return
         */
        private float[] toFloatArray(String val) {
            if (isEmpty(val)) {
                return EMPTY_FLOAT_ARRAY;
            }

            String[] tmp = val.split(";");
            float[] ret = new float[tmp.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = Float.parseFloat(tmp[i]);
            }
            return ret;
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
        private void callSetter(Object target, PropertyDescriptor prop, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Method setter = prop.getWriteMethod();
            if (setter == null) {
                throw new RuntimeException("找不到类" + target.getClass() + "字段" + prop.getName() + "的set方法，请检查类定义是否符合Bean规范");
            }

            Class<?>[] params = setter.getParameterTypes();
            if (value instanceof String && params[0].isEnum()) {
                value = Enum.valueOf(params[0].asSubclass(Enum.class), (String) value);
            }

            if (isCompatibleType(value, params[0])) {
                setter.invoke(target, new Object[] { value });
            } else {
                throw new IllegalArgumentException("Cannot set " + prop.getName() + ": incompatible types, cannot convert " + value.getClass().getName() + " to " + params[0].getName());
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
        /**
         * 大小写无关
         * 
         * @return
         */
        String value();
    }
}
