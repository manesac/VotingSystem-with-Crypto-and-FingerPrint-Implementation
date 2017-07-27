package com.me.util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;


public class VSCrypt {

    public  byte[] encrypt(byte[] key, String strToEncrypt) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            final SecretKeySpec secretKey = new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            final String encryptedString = Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes()));
            return encryptedString.getBytes();
        } catch (Exception e) {
            throw new Exception("Error in AES Encryption", e);
        }
    }

    public  byte[] decrypt(byte[] key, String strToDecrypt) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            final SecretKeySpec secretKey = new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
            return decryptedString.getBytes();
        } catch (Exception e) {
            throw new Exception("Error in AES Decryption", e);
        }
    }

//    public byte[] rsaEncrypt( byte[] data ) throws Exception {
//        PublicKey pubkey;
//        Cipher cipher;
//        try {
//            pubkey = readPubKeyFromFile("public.key");
//            cipher = Cipher.getInstance("RSA");
//        } catch ( Exception e ) {
//            throw new IOException( "Cannot read key from public.key file", e );
//        }
//        cipher.init(Cipher.ENCRYPT_MODE, pubkey );
//        byte[] cipherData = cipher.doFinal( data );
//        System.out.println( "Returning CipherText: " + cipherData );
//        return cipherData;
//    }
//
//    private PublicKey readPubKeyFromFile( String keyFileName ) throws IOException {
//        InputStream in = new FileInputStream( keyFileName );
//        ObjectInputStream oin = new ObjectInputStream( new BufferedInputStream( in ));
//        try {
//            BigInteger m = (BigInteger) oin.readObject();
//            BigInteger e = (BigInteger) oin.readObject();
//
//            RSAPublicKeySpec keySpec = new RSAPublicKeySpec( m, e );
//            KeyFactory fact = KeyFactory.getInstance("RSA");
//            PublicKey pubkey = fact.generatePublic( keySpec );
//            System.out.println( "returning pubkey: " + pubkey );
//            return pubkey;
//        } catch ( Exception e ) {
//            throw new RuntimeException( "Spurious serialization error", e );
//        } finally {
//            oin.close();
//        }
//    }
    
    public byte[] rsaEncrypt( byte[] message ) throws Exception {
    	return new RSA().encrypt(message); 
    }

    public  byte[] mySha256(String[] list) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("sha-256");
        digest.reset();
        for(String s: list)
            digest.update(s.getBytes());
        byte[]  k = digest.digest();
        StringBuffer sb = new StringBuffer();

        for(byte b : k) {
            if (b < 16) sb.append("0");
            sb.append(Integer.toHexString(b & 0xff));
        }        
        return sb.toString().getBytes();
    }
}