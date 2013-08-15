package com.mike724.motoloader;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Security {

    private static String iv = "ma0pel18dnwlg510";

    public static String decrypt(String code, String SecretKey) throws Exception {
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
        SecretKeySpec keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        if (code == null || code.length() == 0)
            throw new Exception("Empty string");

        byte[] decrypted = null;

        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            decrypted = cipher.doFinal(Base64.decodeBase64(code));
        } catch (Exception e) {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return new String(decrypted);
    }
}