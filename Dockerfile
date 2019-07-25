FROM java:8-jdk-alpine
MAINTAINER 1uogang@totok.ai
ENV JVM_OPTS -server -Xmx512m -Xms512m    -XX:MetaspaceSize=64m -verbose:gc -verbose:sizes -XX:+UseG1GC -XX:MaxGCPauseMillis=100
#添加lib
RUN  mkdir -p  jar/lib
ADD  target/lib/*  jar/lib/
ADD  target/linkpreview.jar  jar/lib/app.jar
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT  java -classpath "./jar/lib/*" ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8 com.zayhu.server.linkpreview.rest.Application
