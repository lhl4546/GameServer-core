## core项目旨在提供一种简便易用的方式创建服务器。

## 服务器核心: `CoreServer`
创建一个服务器只需继承`CoreServer`。目前支持创建TCP服务器、HTTP服务器，分别对应`createTcpServer`、`createHttpServer`这2个方法，这2个方法分别接受`TcpServerConfiguration`、`HttpServerConfiguration`这2个接口作为参数，用户需要实现对应的方法以完成服务器功能。CoreServer实现了`Component`接口的start和stop方法，且这2个方法被设定为final，但是提供了beforeStart、afterStart、beforeStop、afterStop这4个回调方法给用户覆盖以添加自定义功能。其中CoreServer本身的start方法和stop方法仅控制tcp服务器、http服务器的启动和停止。用户可以根据自身需要覆盖上述4个回调方法。

## 提供功能
### 集合
1. 免拆箱、装箱的整型IntHashMap
2. 并发ConcurrentHashSet
3. 免拆箱、装箱的整型IntHashSet

### 数据库访问
数据库访问模块提供了一个简单的基于Java反射的ORM实现，并提供同步、异步数据访问接口，其中异步数据访问接口利用Callback参数实现回调。

使用数据访问模块流程如下：
1 定义数据实体类，并使用@Table、@PrimaryKey、@SecondKey、@TableField等注解标注表名、主键、索引、数据字段。其中只支持索引注解，如需使用更多基于索引的查询请自行实现。
2 定义Dao对象，继承BaseDao。
3 定义Service对象(可视情况忽略该步骤)

所有异步数据访问都需要提供Callback参数，若不需要回调可传入Callback.NOOP这个空回调，禁止传null值作为回调

### 事件总线
一个基于Guava EventBus实现的事件总线，提供事件处理器注册、抛出事件等功能。事件总线可以解耦模块，提供模块间通信功能。

### 执行器
基本线程模型。包含如下功能：
1 TCP请求处理器接口、TCP请求拦截器接口
2 一个基于Google Protobuf的TCP请求参数拦截器(已默认注册)
3 一个串行化任务执行的序列
4 TCP请求分发器，负责将TCP请求分发给指定的处理器并提交到指定线程进行处理

### 网络
#### HTTP
HTTP模块提供了一个基于Netty的简易HTTP服务器实现。使用步骤如下：
1 继承CoreServer并重写HTTP相关的方法
2 以CoreServer为参数实例化HttpServer对象

指定路径下面使用了HttpRequestHandler注解标注的HTTP处理器会被自动扫描并注册。

#### TCP
TCP模块提供了几个基于Netty的TCP服务器实现。使用步骤如下：
1 继承CoreServer并重写TCP相关方法
2 以CoreServer为参数实例化TcpServer对象

指定路径下面使用了TcpRequestHandler注解标注的TCP处理器会被自动扫描并注册，拦截器也一样。
TCP模块还提供了一个基于Netty的TCP客户端实现。

### Util
常用工具类：
* ApacheHttpUtil：基于Apache httpclient的http请求工具，支持get、post
* ClassUtil：提供基于包的类扫描功能
* HttpUtil：基于HttpUrlConnection的http请求工具，支持get、post，大量http请求建议使用ApacheHttpUtil
* RandomUtil：基于ThreadLocalRandom的线程安全随机数生成工具
* SensitiveFilter：关键字过滤工具，需要指定类路径下面的某个关键字文件作为参数初始化SensitiveFilter实例
* TimeUtil：能计算现在到指定时间的延时
* Util：提供一些常用功能，比如字符串分割、线程池关停、列表分页

### 回调接口
回调接口分为同步回调接口和异步回调接口，这里的同步和异步相对逻辑执行线程而言，同步回调接口执行在逻辑执行线程，而异步回调接口执行在自定义线程。考虑如下场景：用户在自身线程A中向线程B提交了一个任务，并传入了回调接口作为参数，且要求任务执行完毕后回调仍回到线程A执行(逻辑需要)，此时就需要使用异步回调接口，而同步回调接口则会在线程B中执行。

### 周期活动类型枚举
周期活动类型枚举提供了游戏中常用的活动类型计算公式，比如每日循环活动、每月循环活动。枚举提供如下功能：
1 计算距离下一次活动开启延时
2 判断活动当前是否开启
若需要更复杂的循环类型比如每天2点、每周一0点之类的计算则需要借助Quartz之类的支持Cron表达式的框架。

### DataParser
DataParser是个txt解析器，用于解析策划配置的游戏TXT格式数值文件为Java Bean。

### Timer
一个基于ScheduledExecutorService的定时器实现，支持每日循环、每周循环、一次性、自定义循环等定时任务
