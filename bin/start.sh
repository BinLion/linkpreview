#!/bin/sh
export LANG=en_US.UTF-8
JARFILE=${pom.build.finalName}.jar
JARCMD="-jar $JARFILE";
CMD="java -XX:PermSize=64M \
    -XX:MaxPermSize=128M \
    -XX:+HeapDumpOnOutOfMemoryError \
    ";
TMP=`dirname $0`;
BINPATH=`cd $TMP && pwd`;
if [ "$1" == "-jar" ];then
    CMD="$CMD $JARCMD";
    shift;
else
    #use Mainclass and classpath to run snapfun.
    PRINT_CMD=0;
    if [ "$1" == "_CMD" ]; then
        PRINT_CMD=1;
        shift;
    fi
    case "$1" in
    "def")
        APP="${applicationFQCN}";
    ;;
    "")
        APP="${applicationFQCN}";
    ;;
    *)
        APP="$1";
    ;;
    esac
    shift;
    if [ "$1" == "debug" ]; then
        CMD="$CMD -Xrunjdwp:transport=dt_socket,address=8015,server=y,suspend=n ";
        shift;
    fi
    for i in `cd $BINPATH/.. && ls lib/*.jar`;do CPP=$CPP:$i;done;
    CMD="$CMD -cp .:$JARFILE:lib/$CPP "$APP" $@"
fi

if [[ "$1" == "_CMD" || $PRINT_CMD -ne 0 ]]; then
    echo $CMD;
    exit 0;
fi
pid=$(ps uxf|grep $JARFILE|grep -v grep|awk '{print $2}')
if [ -z $pid ]; then
    cd `dirname $0`
    cd ..
    nohup $CMD 2>&1 >/dev/null &
    #nohup $CMD 2>&1 &
    echo "${pom.build.finalName} started"
else
    echo "${pom.build.finalName} is already running"
fi

