package Server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;

public class RSA {

	private BigInteger p;
	private BigInteger q;
	private BigInteger N;
	private BigInteger phi;
	private BigInteger e;
	private BigInteger d;
	private int bitlength = 1024;
//	private int blocksize = 256; // blocksize in byte

	private Random r;

	public RSA() {
		r = new Random();
		p = BigInteger.probablePrime(bitlength, r);
		q = BigInteger.probablePrime(bitlength, r);
		N = p.multiply(q);

		phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		e = BigInteger.probablePrime(bitlength / 2, r);

		while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(BigInteger.ONE);
		}
		d = e.modInverse(phi);
	}

	public RSA(BigInteger e, BigInteger d, BigInteger N) {
		this.e = e;
		this.d = d;
		this.N = N;
	}

	private static String bytesToString(byte[] encrypted) {
		String test = "";
		for (byte b : encrypted) {
			test += Byte.toString(b);
		}
		return test;
	}

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
			e1.printStackTrace();
		}
		String zz = new String(org.apache.commons.codec.binary.Base64.decodeBase64(tt.getBytes()));
		return (new BigInteger(message)).modPow(new BigInteger(zz.split(",")[1]), new BigInteger(zz.split(",")[0]))
				.toByteArray();
	}

	// Decrypt message
	public byte[] decrypt(byte[] message) {
		BufferedReader br = null;
		File f = new File("private1.key");
		String tt = null;
		try {
			br = new BufferedReader(new FileReader(f));
			tt = br.readLine();
			System.out.println(tt);
			br.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String zz = new String(org.apache.commons.codec.binary.Base64.decodeBase64(tt.getBytes()));
		return (new BigInteger(message)).modPow(new BigInteger(zz.split(",")[1]), new BigInteger(zz.split(",")[0])).toByteArray();
	}
	
	public void generateKey(){
		String s = String.valueOf(N) + "," + String.valueOf(e);
		String ss = String.valueOf(N) + "," + String.valueOf(d);
		byte[] bpub = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
		byte[] bpvt = org.apache.commons.codec.binary.Base64.encodeBase64(ss.getBytes());
		File pub = new File("public1.key");
		File pvt = new File("private1.key");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(pub);
			pw.write(new String(bpub)+"\n");
			pw.close();
			pw = new PrintWriter(pvt);
			pw.write(new String(bpvt)+"\n");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			pw.close();
		}
	}
	

	public static void main(String[] args) throws IOException {
		RSA rsa = new RSA();
		rsa.generateKey();
		String teststring = "test";
		System.out.println("Plain text: "+teststring);
		System.out.println("Encrypting String: " + teststring);
		System.out.println("String in Bytes: " + bytesToString(teststring.getBytes()));

		// encrypt
		byte[] encrypted = rsa.encrypt(teststring.getBytes());
		System.out.println("Encrypted String in Bytes: " + bytesToString(encrypted));

		// decrypt
		byte[] decrypted = rsa.decrypt(encrypted);
		System.out.println("Decrypted String in Bytes: " + bytesToString(decrypted));

		System.out.println("Decrypted String: " + new String(decrypted));

	}

}