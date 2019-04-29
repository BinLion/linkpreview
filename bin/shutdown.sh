#!/bin/bash
pid=$(ps uxf|grep ${pom.build.finalName}.jar|grep -v grep|awk '{print $2}')
if [ -z $pid ]; then
    echo "${pom.build.finalName} is not running"
else
    kill $pid
    echo "${pom.build.finalName} is shutting down"
fi
