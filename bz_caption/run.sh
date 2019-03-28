#!/bin/bash

function build() {
    JAVA_SOURCE_DIR=./src/main/java
    if [[ ! -d ${OUT_DIR} ]]; then
        echo "create out directory."
        mkdir -p "${OUT_DIR}"
    fi

    javac -encoding utf-8 -d ${OUT_DIR} \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/Caption.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/FatalParsingException.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/FormatLRC.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/FormatSRT.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/Main.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/SubtitleInterface.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/TimedTextObject.java \
        ${JAVA_SOURCE_DIR}/com/binzosoft/lib/caption/TimeUtil.java
}

function main() {
    export CLASSPATH="${OUT_DIR}"
    java com.binzosoft.lib.caption.Main $*
}


cd "$(dirname "$0")"
OUT_DIR="./out/"
FILE_INPUT=
FORMAT=
while [[ $# -gt 0 ]];do
    case "$1" in
        -h|--help|\?)
            echo "run.sh [-b|--build]  编译Java文件"
            echo "run.sh [-i|--input ./xxx.lrc] [-f|--format srt] lrc转srt"
            exit 0
            ;;
        -b|--build)
            build
            exit $?
            ;;
        -f|--format)
            shift;FORMAT="${1}";shift
            ;;
        -i|--input)
            shift;FILE_INPUT="${1}";shift
            ;;
        *)
            shift
            ;;
    esac
done
if [[ -z ${FILE_INPUT} ]]; then
    echo "输入文件未指定"
    exit 1
fi
if [[ -z ${FORMAT} ]]; then
    echo "输出格式未指定"
    exit 1
fi
main "${FILE_INPUT}" "${FORMAT}"