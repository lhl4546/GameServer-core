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
 * 数值加载器
 * 
 * @author lihuoliang
 *
 */
public enum TemplateLoader implements Template, Component {
    INSTANCE;

    private Map<Class<? extends Template>, Template> templates = new HashMap<>();
    // 数值类扫描包
    private String scanPackages;

    /**
     * 设置数值扫描包，多个包以英文逗号分隔
     * 
     * @param scanPackages
     */
    public void setScanPackages(String scanPackages) {
        this.scanPackages = scanPackages;
    }

    @Override
    public void load() {
        if (Util.isNullOrEmpty(scanPackages)) {
            throw new IllegalStateException("ScanPackages must be specified before load templates");
        }

        String[] packages = Util.split(scanPackages, ",");
        for (String pack : packages) {
            try {
                List<Class<?>> t = ClassUtil.getClasses(pack);
                Map<Class<? extends Template>, Template> tmp = new HashMap<>();
                t.stream().filter(cls -> isValidTemplate(cls)).map(prototype -> newInstance(prototype)).peek(instance -> tmp.put(instance.getClass(), instance)).forEach(template -> template.load());
                this.templates = tmp;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isValidTemplate(Class<?> prototype) {
        return prototype != this.getClass() && !Modifier.isAbstract(prototype.getModifiers()) && !prototype.isInterface();
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

    @Override
    public void start() throws Exception {
        load();
    }

    @Override
    public void stop() throws Exception {
    }
}
