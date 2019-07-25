#!/usr/bin/env bash
container=linkpreview
tag=20190722
PREFIX=docker.totok.co:8000/server/${container}
source ~/.bash_profile
echo "准备构建....."
echo "删除之前版本..."
docker rmi $(docker images | grep "none" | awk '{print $3}')
docker rmi -f ${PREFIX}:${tag}
echo "开始构建应用程序..."
mvn clean package -DskipTests
echo "开始构建docker镜像"
docker build -t ${PREFIX}:${tag} .
#docker tag ${container}:${tag} ${PREFIX}:${tag}
echo  "${PREFIX}:${tag} 构建成功，开始上传至远程仓库"
docker login --username=admin --password=XWGfnSj9uATR  https://docker.totok.co:8000
docker push ${PREFIX}:${tag}
echo "镜像构建并上传至远程仓库成功"