package com.binzosoft.lib.util.text;

/**
 * UTF-8文件的Unicode签名BOM(Byte Order Mark)。
 *
 * BOM(Byte Order Mark)，是UTF编码方案里用于标识编码的标准标记，在UTF-16里本来是FF FE，变成UTF-8就成了EF BB BF。
 * 这个标记是可选的，因为UTF8字节没有顺序，所以它可以被用来检测一个字节流是否是UTF-8编码的。
 * 微软做这种检测，但有些软件不做这种检测，而把它当作正常字符处理。
 *
 * 微软在自己的UTF-8格式的文本文件之前加上了EF BB BF三个字节, windows上面的notepad等程序就是根据这三个字节来确定一个文本文件是ASCII的还是UTF-8的,
 * 然而这个只是微软暗自作的标记, 其它平台上并没有对UTF-8文本文件做个这样的标记。
 */

class CharsetRecog_UTF8 extends CharsetRecognizer {
    CharsetRecog_UTF8() {
    }

    String getName() {
        return "UTF-8";
    }

    CharsetMatch match(CharsetDetector det) {
        boolean hasBOM = false;
        int numValid = 0;
        int numInvalid = 0;
        byte[] input = det.fRawInput;
        if (det.fRawLength >= 3 && (input[0] & 255) == 239 && (input[1] & 255) == 187 && (input[2] & 255) == 191) {
            hasBOM = true;
        }

        label94:
        for (int i = 0; i < det.fRawLength; ++i) {
            int b = input[i];
            if ((b & 128) != 0) {
                int trailBytes;
                if ((b & 224) == 192) {
                    trailBytes = 1;
                } else if ((b & 240) == 224) {
                    trailBytes = 2;
                } else {
                    if ((b & 248) != 240) {
                        ++numInvalid;
                        continue;
                    }

                    trailBytes = 3;
                }

                do {
                    ++i;
                    if (i >= det.fRawLength) {
                        continue label94;
                    }

                    b = input[i];
                    if ((b & 192) != 128) {
                        ++numInvalid;
                        continue label94;
                    }

                    --trailBytes;
                } while (trailBytes != 0);

                ++numValid;
            }
        }

        int confidence = 0;
        if (hasBOM && numInvalid == 0) {
            confidence = 100;
        } else if (hasBOM && numValid > numInvalid * 10) {
            confidence = 80;
        } else if (numValid > 3 && numInvalid == 0) {
            confidence = 100;
        } else if (numValid > 0 && numInvalid == 0) {
            confidence = 80;
        } else if (numValid == 0 && numInvalid == 0) {
            confidence = 15;
        } else if (numValid > numInvalid * 10) {
            confidence = 25;
        }

        return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
    }
}
