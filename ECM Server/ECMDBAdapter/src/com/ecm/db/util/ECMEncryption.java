package com.ecm.db.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ECMEncryption {

	public static final String AES = "AES";
	
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
    
    public String getEncryptionKey() throws Exception {
    	 KeyGenerator keyGen = KeyGenerator.getInstance(ECMEncryption.AES);
         keyGen.init(128);
         SecretKey sk = keyGen.generateKey();
         String key = byteArrayToHexString(sk.getEncoded());
         return key;
    }

    public String getEncryptedString(String key, String inputString) throws Exception {
    	byte[] bytekey = hexStringToByteArray(key);
        SecretKeySpec sks = new SecretKeySpec(bytekey, ECMEncryption.AES);
        Cipher cipher = Cipher.getInstance(ECMEncryption.AES);
        cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
        byte[] encrypted = cipher.doFinal(inputString.getBytes());
        String encString = byteArrayToHexString(encrypted);
        return encString;
    }
    
    public String getDecryptedString(String key, String inputString) throws Exception {
    	byte[] bytekey = hexStringToByteArray(key);
        SecretKeySpec sks = new SecretKeySpec(bytekey, ECMEncryption.AES);
        Cipher cipher = Cipher.getInstance(ECMEncryption.AES);
        cipher.init(Cipher.DECRYPT_MODE, sks);
        byte[] decrypted = cipher.doFinal(hexStringToByteArray(inputString));
        String decString = new String(decrypted);
        return decString;
    }
}
