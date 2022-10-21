## 目录结构
解压后生成以下两个子目录

* iotx-haas-tdserver-service，包含各中间件的使用示例代码，代码在src/main/java目录下的com.aliyun.iotx包中。
* iotx-haas-tdserver-start，包含启动类`com.aliyun.iotx.Application`。中间件使用示例的单元测试代码在`src/test/java`目录下的`com.aliyun.iotx`包中。日志配置文件为`src/main/resources`目录下的logback-spring.xml。使用springmvc的代码在`src/main/java`目录下的`com.aliyun.iotx`包中，velocity模板在`/src/main/resources/templates`目录中。

## 使用方式
### 在开发工具中执行
将工程导入eclipse或者idea后，直接执行包含main方法的类`com.aliyun.iotx.Application`。

### 使用fat jar的方式
这也是pandora boot应用发布的方式。首先执行下列命令打包
   
```sh
mvn package
```

如果选择了auto-config，可在命令后加

```sh 
-Dautoconfig.userProperties={fullPath}/bootstrap-start/antx.properties
```

通过-D参数指定antx.properties的位置，否则会进入autoconfig的交互模式

然后进入`iotx-haas-tdserver-start/target`目录，执行fat jar

```sh
java -Dpandora.location=${sar} -jar iotx-haas-tdserver-start-1.0.0-SNAPSHOT.jar
```

其中${sar}为sar包的路径

### 通过mvn命令直接启动
第一次调用前先要执行

```sh
mvn install
```

如果maven工程的Artifact，group id，version等都未变化，只需执行一次即可。

然后直接通过命令执行start子工程

```sh
mvn -pl iotx-haas-tdserver-start pandora-boot:run
```

以上两个命令，如果选择了auto-config，可在命令后加

```sh 
-Dautoconfig.userProperties={fullPath}/bootstrap-start/antx.properties
```

通过-D参数指定antx.properties的位置，否则会进入autoconfig的交互模式properties的位置

## 升级指南

* http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/changelog

## Docker 模板

* APP-META 目录里
* http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/docker

## aone发布
请参考文档 http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/aone-guide

## 相关链接
### Pandora Boot
* gitbook ： http://mw.alibaba-inc.com/products/pandoraboot/_book/
* 钉钉交流群 ： 11701173
* wiki ： http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/home
* FAQ: http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/faq

### 开发者应用中心
* 线上 ： http://start.alibaba-inc.com
* 日常 ： http://start.taobao.net
* 文档 ： http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/application-center

### Logger配置

* logger配置: http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/log-config

### Docker相关链接
* 如果工程有docker模板，目录是 APP-META，docker模板的说明文件是：APP-META/README.md
* docker参考说明：http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/docker

### Security
* wiki ： http://gitlab.alibaba-inc.com/ali_security_public/stc-wiki/wikis/home
* 用法详细说明 ： http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/spring-boot-security
