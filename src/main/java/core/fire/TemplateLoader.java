/**
 * 
 */
package core.fire;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.fire.util.ClassUtil;
import core.fire.util.Util;

/**
 * 数值加载器。该加载器会从指定包下面加载所有符合条件的{@link Template}子类并调用这个类的{@code load}方法。
 * 
 * @author lihuoliang
 *
 */
public enum TemplateLoader implements Template, Component {
    INSTANCE;

    private Map<Class<? extends Template>, Template> templates = new HashMap<>();
    // 模板类扫描包
    private String templateScanPackage;

    /**
     * 设置数值扫描包，多个包以英文逗号分隔
     * 
     * @param scanPackages
     */
    public void setCoreServer(CoreServer coreServer) {
        this.templateScanPackage = coreServer.getTemplateScanPath();
    }

    @Override
    public void load() {
        loadTemplateClass();
        invokeLoadMethod();
    }

    /**
     * 调用Template的load方法
     */
    private void invokeLoadMethod() {
        templates.values().forEach(template -> template.load());
    }

    /**
     * 加载所有符合条件的Template子类
     */
    private void loadTemplateClass() {
        String scanPackages = templateScanPackage;
        if (Util.isNullOrEmpty(scanPackages)) {
            throw new IllegalStateException("ScanPackages must be specified before load templates");
        }

        String[] packages = Util.split(scanPackages, ",");
        for (String pack : packages) {
            try {
                List<Class<?>> t = ClassUtil.getClasses(pack);
                Map<Class<? extends Template>, Template> tmp = new HashMap<>();
                t.stream().filter(cls -> isValidTemplate(cls)).map(prototype -> newInstance(prototype)).forEach(instance -> tmp.put(instance.getClass(), instance));
                this.templates = tmp;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <ol>
     * <li>是Template子类</li>
     * <li>不是TemplateLoader类</li>
     * <li>不是抽象类</li>
     * <li>不是Template接口本身</li>
     * </ol>
     * 符合上述条件的类即是有效的数值模版类
     * 
     * @param prototype
     * @return
     */
    private boolean isValidTemplate(Class<?> prototype) {
        return Template.class.isAssignableFrom(prototype) && prototype != this.getClass() && !Modifier.isAbstract(prototype.getModifiers()) && !prototype.isInterface();
    }

    private Template newInstance(Class<?> prototype) {
        try {
            return (Template) prototype.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Template> T get(Class<T> key) {
        return (T) templates.get(key);
    }

    /**
     * 重新加载，运行期间调用该方法只能对已扫描的类执行load调用
     */
    public void reload() {
        invokeLoadMethod();
    }

    @Override
    public void start() throws Exception {
        load();
    }

    @Override
    public void stop() throws Exception {
    }
}
