package com.me.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;

public class RSA {
									
	// Encrypt message
	public byte[] encrypt(byte[] message) {
		BufferedReader br = null;
		File f = new File("public1.key");
		String tt = null;
		try {
			br = new BufferedReader(new FileReader(f));
			tt = br.readLine();
			System.out.println(tt);
			br.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String zz = new String(org.apache.commons.codec.binary.Base64.decodeBase64(tt.getBytes()));
		return (new BigInteger(message)).modPow(new BigInteger(zz.split(",")[1]), new BigInteger(zz.split(",")[0]))
				.toByteArray();
	}

}