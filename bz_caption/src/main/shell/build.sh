#!/bin/bash

JAVA_SOURCE_DIR=../java
OUT_DIR=./out/
if [[ ! -d ${OUT_DIR} ]]; then
    echo "create out directory."
    mkdir "${OUT_DIR}"
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
