#!/bin/bash

cd ../java

if [ -z "${CLASSPATH}" ]; then
    # 如果 CLASSPATH 环境变量为空，添加当前目录，否则无法找到对应类。
    export CLASSPATH=.
fi

java com.binzosoft.lib.encrypt.Decrypt "$@"
