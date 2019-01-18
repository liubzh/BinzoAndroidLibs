#!/bin/bash

# 环境变量CLASSPATH 设置为class文件所在目录，否则无法找到对应类。
export CLASSPATH=./out/

java com.binzosoft.lib.caption.Main "$@"
