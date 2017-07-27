package com.me.controller;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me.util.VSCrypt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class Connection {
	private String host = "localhost";
	private VSCrypt toolKit;
	private JSONObject ballot;
	private byte[] finalKey;
	private byte[] vid_hash;

	public Connection() {
		toolKit = new VSCrypt();
		StringBuffer instr = new StringBuffer();
		String timestamp;
		System.out.println("SocketClient Initialized");
	}

	public String[] register(String[] info) {
		try {
			Socket clientSocket = new Socket(host, 9999);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			JSONObject jo = new JSONObject();
			jo.put("nagid", info[0]);
			jo.put("minutes", info[1]);
			outToServer.write(jo.toString().getBytes());
			
	        String receivedData = inFromServer.readLine();  // data send from server either nagid and pin or null
	        jo = (JSONObject) new JSONParser().parse(receivedData);
			String [] passToRegister = {jo.get("id").toString(),jo.get("pin").toString()};
			return passToRegister;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean logout() {
		try {
			Socket clientSocket = new Socket(host, 9999);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			JSONObject dataToSend = new JSONObject();
			dataToSend.put("vid_hash", new String(vid_hash));
			dataToSend.put("logout", "zz");
			outToServer.write(dataToSend.toString().getBytes());
			
			String receivedData = inFromServer.readLine();
			if(receivedData.equals("done")){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}	
	
	@SuppressWarnings("unchecked")
	public String start(String[] loginInfo){
//		Socket clientSocket;
		try{
			Socket clientSocket = new Socket(host, 9999);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // Generation of Shared key start //
            VSCrypt toolKit = new VSCrypt();
            String[] s = {loginInfo[0]};
            vid_hash = toolKit.mySha256(s);
            String[] k = {"somethingRandom"}; // generate something random so that it can be used to generate a shared key
            byte[] somethingRandom_Hash = toolKit.mySha256(k); // get the hash of it

            String vid_hashString = new String(vid_hash); 					  // string value of vid hash
            String sometingRandom_string = new String(somethingRandom_Hash); //  string value of the random_hash
            String sendData = vid_hashString+","+sometingRandom_string;     //   comma separate them and server uses each value seperately
            outToServer.write(toolKit.rsaEncrypt(sendData.getBytes()));    //    send it to server using rsa Encryption
            
            String returnedString = inFromServer.readLine();             // value returned from server
            
            if(returnedString.equals("online"))
            	return "online";
            
            System.out.println(new String(sendData.split(",")[1])+"||"+returnedString);
            String[] keyset = {sendData.split(",")[1], returnedString};      // we need the second part of the string we sent,
                                                                            //because that is what the server will use to create a hash
            finalKey = toolKit.mySha256(keyset); // get the final shared key using the same set of the data as the server
            System.out.println(new String(toolKit.mySha256(keyset)));
            System.out.print("the new key is "+new String(finalKey)); // we now have a shared key to use for aes
            if (finalKey.length != 128)
                System.out.print("\n key size is: " + finalKey.length);
            // Genereton of Shared key stop //

            // further data are encryped using aes
            String vid = loginInfo[0];
            String minutes = loginInfo[1];
            String pin = loginInfo[2];
            clientSocket = new Socket(host, 9999); // make a second connection
            outToServer = new DataOutputStream(clientSocket.getOutputStream()); // setup output pipe
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // setup input pipe
            JSONObject dataToSend = new JSONObject(); // make a json to send
            System.out.println("\nThis is my vid hash "+ new String(vid_hash));
            dataToSend.put("vid_hash", new String(vid_hash)); // packet to go out. Do not encrypt
            System.out.println("going to do aes encrypt");

            JSONObject data = new JSONObject();

            JSONObject userInfo = new JSONObject();
            userInfo.put("vid", vid);
            userInfo.put("minutes", minutes);
            userInfo.put("pin", pin);
            data.put("userInfo", userInfo);
            data.put("state", "login");
            byte[] encryptedData = toolKit.encrypt(finalKey, data.toString());
            dataToSend.put("data", new String(encryptedData));
            outToServer.write(dataToSend.toString().getBytes());
            
            returnedString = inFromServer.readLine();
            
            System.out.println(returnedString);
            JSONObject returnedJson = stringToJson(returnedString);
            System.out.println(returnedJson.toJSONString());
            
            String string = new String(returnedJson.get("data").toString());
            String normalized_string = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\u0000", "");
            System.out.println(normalized_string);
            
            byte[] decryptedBytes = toolKit.decrypt(finalKey, normalized_string);
            String decryptedStuff = new String(decryptedBytes);
            ballot = new JSONObject();
            ballot = stringToJson(decryptedStuff.toString());
            System.out.println("key is: "+new String(finalKey)+"\nvid_hash is: "+vid_hash +"\nuser info is "+ballot.toJSONString());
            
			return "sucess";
			
		}catch(Exception e){
			System.out.println("this"+e.getStackTrace());
			return "failed";
		}
	}

	@SuppressWarnings("unchecked")
	public boolean sendBallot(JSONObject finalBallot) {
		try {
			JSONObject ballotToSend = new JSONObject();
			Socket clientSocket = new Socket(host, 9999);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			byte[] encryptedData = toolKit.encrypt(finalKey, finalBallot.toJSONString());

			ballotToSend.put("vid_hash", new String(vid_hash));
			ballotToSend.put("data", new String(encryptedData));
			System.out.println("Ballot being sent: " + ballotToSend.toString());
			byte[] dataToSend = ballotToSend.toString().getBytes();
			System.out.println(dataToSend.length);
			outToServer.write(dataToSend);
			
			String returnedString = inFromServer.readLine();
			System.out.println(returnedString);
			JSONObject returnedJson = stringToJson(returnedString);
			System.out.println(returnedJson.toJSONString());

			String string = new String(returnedJson.get("data").toString());
			// Map<Object, Object> activeUsers = new HashMap<Object, Object>();
			String normalized_string = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\u0000", "");
			System.out.println(normalized_string);
			// JSONObject packetObject = stringToJson(normalized_string);
			// String vid_hash2 = packetObject.get("vid_hash").toString();
			// String userinfo = new
			// String(packetObject.get("data").toString());
			// byte[] key = activeUsers.get(vid_hash2).toString().getBytes();
			byte[] decryptedBytes = toolKit.decrypt(finalKey, normalized_string);
			String decryptedStuff = new String(decryptedBytes);
			ballot = new JSONObject();
			ballot = stringToJson(decryptedStuff.toString());

			System.out.println("key is: " + new String(finalKey) + "\nvid_hash is: " + vid_hash + "\nuser info is "
					+ ballot.toJSONString());

			if (!ballot.get("state").toString().equals("accepted")) {
				return false;
			}

			return true;
		} catch (Exception e) {
			System.out.println("this" + e.getStackTrace());
			return false;
		}
	}

	public static JSONObject stringToJson(String s) {
		JSONObject myNewString = null;
		try {
			myNewString = (JSONObject) new JSONParser().parse(s);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not read json");
		}
		return myNewString;
	}

	public JSONObject getBallot() {
		return ballot;
	}
}
