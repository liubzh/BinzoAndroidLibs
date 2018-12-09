package com.binzosoft.lib.encrypt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt {

    private static final String encryptionKey = "0123456789ABCDEF";
    private static final String encryptionIv = "ABCDEF0123456789";

    public static void main(String[] args) {
        decrypt(args);
    }

    public static void decrypt(String[] filePaths) {
        Cipher cipher;
        try {
            cipher = getCipherInstance();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException var7) {
            throw new RuntimeException(var7);
        }

        Key cipherKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        IvParameterSpec cipherIV = new IvParameterSpec(encryptionIv.getBytes());

        try {
            cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherIV);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException var6) {
            throw new RuntimeException(var6);
        }

        for (String path : filePaths) {
            System.out.println("DECRYPT:'" + path + "'");
            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            CipherInputStream cipherInputStream = null;
            try {
                inputStream = new FileInputStream(path);
                outputStream = new FileOutputStream(path.replace(".enc", ""));
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
