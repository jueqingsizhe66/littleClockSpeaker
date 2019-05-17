#  Java版闹钟订制

使用方式： 运行main，然后输入框按格式输入(英文逗号分隔)

## Jacob dll

下载[JACOB][1] is a JAVA-COM Bridge that allows you to call COM Automation components from Java. It uses JNI to make native calls to the COM libraries. JACOB runs on x86 and x64 environments supporting 32 bit and 64 bit JVMs 


## Jacob dependency


在上节中下载包中包含对应jar包，放在我的[ java nexus ][2]库中

``` java
    <dependency>
      <groupId>com.jacob</groupId>
      <artifactId>jacob</artifactId>
      <version>1.19</version>
    </dependency>
```
或者上传到本地maven repository中(填好具体的group,artifactid,version等信息)

``` java
mvn install:install-file -Dfile=D:\thrift-0.9.2.jar -DgroupId=org.apache.thrift -DartifactId=libthrift -Dversion=0.9.2 -Dpackaging=jar
```

[1]: https://sourceforge.net/projects/jacob-project/
[2]:https://www.jianshu.com/p/bacfdf261967 
[3]:https://www.cnblogs.com/ncyhl/p/7535564.html 

