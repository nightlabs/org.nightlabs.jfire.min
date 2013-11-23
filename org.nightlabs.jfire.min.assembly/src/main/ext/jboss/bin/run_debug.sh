#!/bin/bash

cd `dirname $0`

export JAVA_OPTS="$JAVA_OPTS -Xms128m -Xmx1024m -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

./run.sh $*
