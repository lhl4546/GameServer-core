package core.fire;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * core配置类，提供core所需Spring配置。具体工程必须import该类以完成Spring配置
 * 
 * @author lihuoliang
 *
 */
@Configuration
@ComponentScan("core.fire")
public class LauncherConfig
{
}
