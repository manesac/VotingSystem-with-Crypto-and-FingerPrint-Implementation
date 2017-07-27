package Server;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class VotingSystem {

	private Map<String, String> activeUsers = null;
	private static VSCrypt cryptoToolKit = null;

	public VotingSystem() {
		activeUsers = new HashMap<String, String>();
		cryptoToolKit = new VSCrypt();
	}

	private synchronized String getKeyForUser(String user) {
		return activeUsers.get(user).toString();
	}

	private synchronized void setKeyForUser(String user, String key) {
		activeUsers.put(user, key);
	}

	public synchronized String router(byte[] packet) throws Exception {
		JSONObject packetObject = null;
		String vid_hash = null;
		String packetAsString = new String(packet);

		if (packetAsString.contains("nagid")) {
			String normalized_string = Normalizer.normalize(packetAsString, Normalizer.Form.NFD).replaceAll("\u0000",
					"");
			return this.addUser(normalized_string);
		} else if(packetAsString.contains("logout")){ 
			String normalized_string = Normalizer.normalize(packetAsString, Normalizer.Form.NFD).replaceAll("\u0000",
					"");
			return this.logOut(normalized_string);
		}else {
			try {
				if (packetAsString.contains("vid_hash")) {
					System.out.println("AES Encrypted");
					System.out.println("Decrypt and get data using Session key");
					String string = new String(packet);
					String normalized_string = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\u0000",
							"");
					System.out.println(normalized_string);
					packetObject = stringToJson(normalized_string);
					vid_hash = packetObject.get("vid_hash").toString();
					String userinfo = new String(packetObject.get("data").toString());
					byte[] key = activeUsers.get(vid_hash).toString().getBytes();
					byte[] decryptedBytes = cryptoToolKit.decrypt(key, userinfo);
					String decryptedStuff = new String(decryptedBytes);
					JSONObject userInfoObject = new JSONObject();
					userInfoObject = stringToJson(decryptedStuff.toString());
					System.out.println("key is: " + new String(key) + "\nvid_hash is: " + vid_hash + "\nuser info is "
							+ userInfoObject.toJSONString());
					packetObject = userInfoObject;

				} else {
					System.out.println("RSA Encrypted >> lets get session key");
					byte[] decryptedByteArray = cryptoToolKit.rsaDecrypt(Arrays.copyOf(packet, 256));
					String incoming = new String(decryptedByteArray);
					String[] incomingArr = incoming.split(",");

					Random rand = new Random();
					Integer random = rand.nextInt();
					String serverRandom = random.toString().getBytes().toString();
					String outGoing = serverRandom;
					System.out.println(incomingArr[1] + "||" + serverRandom);

					String[] keySet = { incomingArr[1], outGoing };
					byte[] myKey = cryptoToolKit.mySha256(keySet);
					String myKeyString = new String(myKey);
					
					if (!activeUsers.containsKey(incomingArr[0])) {
						// Set key for each user Syncronised
						setKeyForUser(incomingArr[0], myKeyString);
						System.out.println("for user " + incomingArr[0] + "session key is " + myKeyString);
						return outGoing;
					}else
						return "online";
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Could not decrypt\n" + packetAsString);
				System.out.println(packetAsString.getBytes().length);
				return null;
			}

			String state;
			state = packetObject.get("state").toString();

			if (state.equals("login")) {
				return this.login(packetObject, vid_hash);
			} else if (state.equals("ballot_response")) {
				return this.ballot_response(packetObject, vid_hash);
			} else if (state.equals("getReuslts")) {
				return null;
			} 
			return null;
		}
	}

	private String logOut(String normalized_string) {
		JSONObject logoutData = new JSONObject();
		logoutData = stringToJson(normalized_string);
		String vidHash = logoutData.get("vid_hash").toString();
		if(activeUsers.containsKey(vidHash)){
			activeUsers.remove(vidHash);
			return "done";
		}
		return null;
	}

	private String addUser(String info) throws Exception {
		JSONObject jo = new JSONObject();
		jo = stringToJson(info);
		System.out.println("Send form client => "+jo.toJSONString());
		// jo.put("nagid", "1234");
		// jo.put("minutes", "34,45");
		JSONObject loginInfo = new JSONObject();
		String[] nag_id = { jo.get("nagid").toString() };
		String minutes = jo.get("minutes").toString();
//		byte[] vid_hash = cryptoToolKit.mySha256(nag_id);
//		String vid = new String(vid_hash);

		Random r = new Random();
		String pin = String.valueOf(r.nextInt(99999));
		String name = intToASCII(Integer.parseInt(nag_id[0]));
		name += String.valueOf(r.nextInt(999));
		String []a = {name};
		String vid = new String(cryptoToolKit.mySha256(a));
		loginInfo.put("id", name);
		loginInfo.put("pin", pin);

		String eid = new String(cryptoToolKit.encrypt(pin.getBytes(), name));
		String eminutes = new String(cryptoToolKit.encrypt(pin.getBytes(), minutes));

		DB_handler db = new DB_handler();
		String sql = "insert into voter_info values('" + vid + "','" + eid + "','" + eminutes + "'," + 0 + ")";
		if (db.setValues(sql)) {
			System.out.println("System Generated => "+loginInfo.toJSONString());
			return loginInfo.toJSONString();
		}
		return null;
	}

	private String intToASCII(int i) {
		String s = "";
		while(i != 0){
			s += ((char)(
							(
									(
											(i%10)+new Random().nextInt(99)
									)%26
							)+96
						));
			i /= 10;
		}
		return s;
	}

	private String login(JSONObject packet, String vid_hash) throws Exception {

		String temp = null;
		String user_hash = vid_hash;
		JSONObject userInfo = (JSONObject) packet.get("userInfo");
		String userProvided_pin = userInfo.get("pin").toString();
		String userProvided_vid = userInfo.get("vid").toString();
		String userProvided_fp = userInfo.get("minutes").toString();
		JSONObject returnPacket = new JSONObject();
		returnPacket.put("vid_hash", user_hash);

		DB_handler db = new DB_handler();
		String sql;
		// sql = "select vid, ssn from votingsystem.voters_of_america where
		// `vid_hash` = \"" + user_hash + "\";";
		sql = "select vid, minutes from voter_info where `vid_hash` = \"" + user_hash + "\";";
		ResultSet result = db.getResult(sql);
		int runs = 0;

		String dbReturn_vid = null;
		String dbReturn_fp = null;

		while (result.next()) {
			runs++;
			dbReturn_vid = new String(cryptoToolKit.decrypt(userProvided_pin.getBytes(), result.getString("vid")));
			dbReturn_fp = new String(cryptoToolKit.decrypt(userProvided_pin.getBytes(), result.getString("minutes")));
		}
		db.cleanup();

		if (runs == 0 || runs > 1) {
			System.out.println("more than one entry found" + runs);
		} else {
			System.out.println("only one entry found in database fot this user");
			boolean vidMatches = userProvided_vid.compareTo(dbReturn_vid) == 0;
			System.out.println(dbReturn_vid);
			System.out.println(String.valueOf(vidMatches));
			boolean ssnMatches = check(userProvided_fp, dbReturn_fp);
			if (vidMatches && ssnMatches) {
				System.out.println("i m in");
				// temp = "check";
				JSONObject ballot = setupBallot();
				ballot.put("state", "ballot");
				try {
					byte[] encryptedData = cryptoToolKit.encrypt(activeUsers.get(vid_hash).getBytes(),
							ballot.toString());
					returnPacket.put("data", new String(encryptedData));
					System.out.println("\nSending the ballots");
					return returnPacket.toString();
				} catch (Exception e) {
					System.out.println("Could not encrypt the string going out client " + e);
					e.printStackTrace();
				}
			} else {
				activeUsers.remove(vid_hash);
				System.out.println("the provided info was wrong");
			}
		}

		return returnPacket.toJSONString();
		// return temp;

	}

	private boolean check(String userProvided_fp, String dbReturn_fp) {
		System.out.println(userProvided_fp + "||" + dbReturn_fp);
		String[] ufp = userProvided_fp.split(",");
		String[] dfp = dbReturn_fp.split(",");
		if ((Math.abs(Integer.parseInt(ufp[0]) - Integer.parseInt(dfp[0])) < 5)
				&& (Math.abs(Integer.parseInt(ufp[1]) - Integer.parseInt(dfp[1])) < 10))
			return true;
		return false;
	}

	private String ballot_response(JSONObject packet, String vid_hash) {

		DB_handler db = new DB_handler();
		int check = 0;
		ResultSet rs = db.getResult("select vote_status from voter_info where vid_hash='" + vid_hash + "'");
		try {
			if(rs.next())
				check = rs.getInt("vote_status");
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}
		if (check == 0) {
			String id = null;
			JSONObject tmp = stringToJson(packet.get("presidential_candidate").toString());
			if (tmp.get("pick").toString().equals("true")) {
				id = tmp.get("id").toString();
				System.out.println(id);
			}

			db = new DB_handler();
			String sql = "insert into candidates_vote (vid_hash,cid) values('" + vid_hash + "'," + Integer.parseInt(id) + ")";
			boolean presidentSet = false;
			try {
				presidentSet = db.setValues(sql);
			} catch (Exception E) {
				presidentSet = false;
			}
			db.cleanup();
			if (presidentSet) {
				// make sure this person can not vote again
				db = new DB_handler();
				sql = "update voter_info set vote_status = 1 where vid_hash = '" + vid_hash + "'";
				db.setValues(sql);
				db.cleanup();
				JSONObject dataToSend = new JSONObject();
				dataToSend.put("vid_hash", vid_hash);
				JSONObject data = new JSONObject();
				data.put("state", "accepted");
				try {
					byte[] dataEncrypted = cryptoToolKit.encrypt(activeUsers.get(vid_hash).toString().getBytes(),
							data.toString());
					dataToSend.put("data", new String(dataEncrypted));
					String returnVal = new String(dataToSend.toJSONString());
					return returnVal;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		JSONObject dataToSend = new JSONObject();
		dataToSend.put("vid_hash", vid_hash);
		dataToSend.put("state", "error");
		dataToSend.put("data", null);
		String returnVal = new String(dataToSend.toJSONString());

		return returnVal;
	}

	private JSONObject setupBallot() {
		JSONObject ballotObject = new JSONObject();
		ArrayList presidents = new ArrayList();

		DB_handler db = new DB_handler();
		String sql = "select * from candidates;";
		ResultSet presidentsTable = db.getResult(sql);

		try {
			while (presidentsTable.next()) {
				JSONObject tmp = new JSONObject();
				tmp.put("id", presidentsTable.getInt("cid"));
				tmp.put("full_name", presidentsTable.getString("cname"));
				tmp.put("party_affiliation", presidentsTable.getString("cparty"));
				try {
					File file = new File(presidentsTable.getString("img_loc"));
					FileInputStream imageInFile = new FileInputStream(file);
					byte imageData[] = new byte[(int) file.length()];
					imageInFile.read(imageData);
					String imageDataString = Base64.encodeBase64URLSafeString(imageData);
					tmp.put("image", imageDataString);
				} catch (Exception e) {
				}
				tmp.put("desc",presidentsTable.getString("description"));
				// tmp.put("party_image", presidentsTable.getString("img_loc"));
				presidents.add(tmp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.cleanup();

		ballotObject.put("presidential_candidates", presidents);

		return ballotObject;
	}

	private JSONObject stringToJson(String s) {
		JSONObject myNewString = null;
		try {
			myNewString = (JSONObject) new JSONParser().parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("Could not read json");
		}
		return myNewString;
	}

}
