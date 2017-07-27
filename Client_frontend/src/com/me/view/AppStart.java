package com.me.view;

import javax.swing.UIManager;

public class AppStart {

	public static void main(String[] args) throws Exception {
		setStyle();
		Login lg = new Login();
		// VoterRegistration vr = new VoterRegistration();
	}

	private static void setStyle() {

		try {
			org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
			UIManager.put("RootPane.setupButtonVisible", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
