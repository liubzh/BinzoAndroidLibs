package com.binzosoft.lib.encrypt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt {

    private static final String encryptionKey = "0123456789ABCDEF";
    private static final String encryptionIv = "ABCDEF0123456789";

    public static void main(String[] args) {
        encrypt(args);
    }

    public static void encrypt(String[] filePaths) {
        try {
            Cipher cipher = getCipherInstance();

            Key cipherKey = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            IvParameterSpec cipherIV = new IvParameterSpec(encryptionIv.getBytes("UTF-8"));

            cipher.init(Cipher.ENCRYPT_MODE, cipherKey, cipherIV);

            for (String path : filePaths) {
                System.out.println("ENCRYPT:'" + path + "'");
                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                CipherInputStream cipherInputStream = null;
                try {
                    inputStream = new FileInputStream(path);
                    outputStream = new FileOutputStream(path + ".enc");
                    cipherInputStream = new CipherInputStream(inputStream, cipher);
                    copyLarge(cipherInputStream, outputStream, new byte[1024 * 4]);//4KB
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (cipherInputStream != null) {
                            cipherInputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                    }

                }
            }
        } catch (InvalidAlgorithmParameterException | InvalidKeyException var6) {
            throw new RuntimeException(var6);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException var7) {
            throw new RuntimeException(var7);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static Cipher getCipherInstance() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            if (count % (1024 * 1024 * 10) == 0) { //10MB
                System.out.print(".");
            }
        }
        return count;
    }

}
