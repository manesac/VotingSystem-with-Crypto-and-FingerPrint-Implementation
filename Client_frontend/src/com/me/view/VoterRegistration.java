package com.me.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.me.controller.Connection;
import com.me.util.FingerPrint;

public class VoterRegistration extends JFrame {
	private JLabel lblPin;
	private JLabel lblPleaseLoginTo;
	private JLabel lblCscVotingSystem;
	private JPanel panel_2;
	private JTextField NagritaID;
	// private File pic;
	private BufferedImage pic;
	private int[] minute;

	public void setMinute(int[] minute) {
		this.minute = minute;
	}

	public VoterRegistration() {
		inita();
	}

	private void inita() {

		FScann.setListner(new Listner() {

			@Override
			public void update(int[] m) {
				setMinute(m);
			}

		});

		setTitle("Registration System");
		setSize(504, 357);
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(null);

		JLabel lblUsername = new JLabel("Nagrita_ID");
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsername.setBounds(80, 96, 67, 14);
		getContentPane().add(lblUsername);

		lblPin = new JLabel("Fingerprint");
		lblPin.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPin.setBounds(80, 129, 67, 14);
		getContentPane().add(lblPin);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(204, 255, 255));
		panel.setBounds(0, 255, 450, 23);
		getContentPane().add(panel);
		panel.setLayout(null);

		lblCscVotingSystem = new JLabel("Voting System\u00A9\u00AE\u2122");
		lblCscVotingSystem.setBounds(110, 0, 205, 23);
		panel.add(lblCscVotingSystem);
		lblCscVotingSystem.setHorizontalAlignment(SwingConstants.CENTER);

		JButton btnLogin = new JButton("Register");
		btnLogin.addActionListener(new CustomListner());
		btnLogin.setBounds(160, 154, 120, 23);
		getContentPane().add(btnLogin);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(153, 204, 255));
		panel_1.setBounds(0, 0, 450, 39);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);

		lblPleaseLoginTo = new JLabel("Voter Registration");
		lblPleaseLoginTo.setBackground(new Color(153, 204, 255));
		lblPleaseLoginTo.setBounds(10, 11, 176, 22);
		panel_1.add(lblPleaseLoginTo);
		lblPleaseLoginTo.setFont(new Font("Tahoma", Font.PLAIN, 18));

		JLabel label = new JLabel("YYYY Election");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label.setBackground(new Color(153, 204, 255));
		label.setBounds(300, 11, 140, 22);
		panel_1.add(label);

		panel_2 = new JPanel();
		panel_2.setBorder(null);
		panel_2.setBackground(Color.WHITE);
		panel_2.setBounds(160, 72, 118, 71);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);

		NagritaID = new JTextField();
		NagritaID.setToolTipText("Please enter your issued ID number");
		NagritaID.setText("");
		NagritaID.setColumns(10);
		NagritaID.setBounds(0, 21, 118, 20);
		panel_2.add(NagritaID);

		JButton btnNewButton = new JButton("Search");
		btnNewButton.addActionListener(new CustomListner());
		btnNewButton.setBounds(0, 48, 118, 23);
		panel_2.add(btnNewButton);

		JButton btnLogIn = new JButton("Log In");
		btnLogIn.addActionListener(new CustomListner());
		btnLogIn.setBounds(344, 214, 89, 23);
		getContentPane().add(btnLogIn);

		JPanel panel_3 = new JPanel();
		panel_3.setBounds(0, 0, 450, 278);
		getContentPane().add(panel_3);

		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private class CustomListner implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "Register":
				voterRegister();
				break;
			case "Log In":
				new Login().setVisible(true);
				dispose();
				break;
			case "Search":
				onBtBrowsePressed();
				break;
			}
		}
	}

	public void voterRegister() {
		String id = NagritaID.getText();
		// int[] minute = (new FingerPrint(pic)).execute();
		String minutes = String.valueOf(minute[0]) + "," + String.valueOf(minute[1]);
		System.out.println(minutes);
		String[] info = { id, minutes };

		Connection con = new Connection();
		String[] r_info = con.register(info);
		if (r_info != null) {
			NagritaID.setText("");
			JOptionPane.showMessageDialog(null,
					"Save this information\n\nId => " + r_info[0] + "\nPin => " + r_info[1]);
			new Login();
			dispose();
		} else {
			NagritaID.setText("");
			JOptionPane.showMessageDialog(null, "Failed to register");
		}
	}

	private void onBtBrowsePressed() {
		new FScann();
	}
}
