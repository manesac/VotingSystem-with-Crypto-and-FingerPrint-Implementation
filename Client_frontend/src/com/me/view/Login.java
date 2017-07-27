package com.me.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONObject;

import com.me.controller.Connection;
import com.me.util.FingerPrint;
import com.sun.javafx.scene.paint.GradientUtils.Point;
import com.sun.javafx.tk.Toolkit;

public class Login extends JFrame {
	private JLabel lblPin;
	private JLabel lblPleaseLoginTo;
	private JLabel lblCscVotingSystem;
	private JPanel panel_2;
	private JTextField username;
	private JPasswordField ppin;
	private JButton btnLogin;
	// File pic;
	BufferedImage pic;
	private Connection connector;
	private JLabel lblLogIn;
	private JButton btnRegister;
	private JPanel panel_3;

	public Login() {
		initializeLogin();
	}

	private void initializeLogin() {
		setTitle("Voting System");
		setSize(504, 357);
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(null);

		JLabel lblUsername = new JLabel("UserID");
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsername.setBounds(81, 76, 67, 14);
		getContentPane().add(lblUsername);

		lblPin = new JLabel("Pin");
		lblPin.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPin.setBounds(102, 101, 46, 14);
		getContentPane().add(lblPin);

		btnLogin = new JButton("Login");
		btnLogin.setBounds(158, 156, 118, 23);
		getContentPane().add(btnLogin);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(153, 204, 255));
		panel_1.setBounds(0, 0, 450, 39);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);

		lblPleaseLoginTo = new JLabel("YYYY Election");
		lblPleaseLoginTo.setBounds(300, 11, 140, 22);
		panel_1.add(lblPleaseLoginTo, BorderLayout.WEST);
		lblPleaseLoginTo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPleaseLoginTo.setFont(new Font("Tahoma", Font.PLAIN, 18));

		lblLogIn = new JLabel("Voter LogIn");
		lblLogIn.setBounds(10, 11, 176, 22);
		panel_1.add(lblLogIn);
		lblLogIn.setFont(new Font("Tahoma", Font.PLAIN, 18));

		panel_2 = new JPanel();
		panel_2.setBorder(null);
		panel_2.setBackground(Color.WHITE);
		panel_2.setBounds(158, 76, 118, 71);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);

		username = new JTextField();
		username.setToolTipText("Please enter your provided username.");
		username.setText("");
		username.setColumns(10);
		username.setBounds(0, 0, 118, 20);
		panel_2.add(username);

		ppin = new JPasswordField();
		ppin.setToolTipText("Please enter your provided pin number.");
		ppin.setBounds(0, 23, 118, 20);
		panel_2.add(ppin);

		JButton btnNewButton = new JButton("Search");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onBtBrowsePressed();
			}
		});
		btnNewButton.setBounds(0, 48, 118, 23);
		panel_2.add(btnNewButton);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(204, 255, 255));
		panel.setBounds(0, 255, 450, 23);
		getContentPane().add(panel);
		panel.setLayout(null);

		lblCscVotingSystem = new JLabel("Voting System\u00A9\u00AE\u2122");
		lblCscVotingSystem.setBounds(110, 0, 205, 23);
		panel.add(lblCscVotingSystem);
		lblCscVotingSystem.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel lblFingerprint = new JLabel("FingerPrint");
		lblFingerprint.setBounds(88, 126, 60, 14);
		getContentPane().add(lblFingerprint);

		btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new VoterRegistration();
				dispose();
			}
		});
		btnRegister.setBounds(345, 214, 89, 23);
		getContentPane().add(btnRegister);

		panel_3 = new JPanel();
		panel_3.setBounds(0, 0, 450, 278);
		getContentPane().add(panel_3);

		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		actionLogin();
		username.setText("");
		ppin.setText("");
		username.requestFocus();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionLogin() {
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String id = username.getText();
				String pin = new String(ppin.getPassword());
				String minutes = "";
				try {
					int[] minute = (new FingerPrint(pic)).execute();
					minutes = String.valueOf(minute[0]) + "," + String.valueOf(minute[1]);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Enter fingerprint");
				}
				// System.out.println(Integer.parseInt(minutes.split(",")[0]));

				String[] login_info = { id, minutes, pin };

				// start connection
				connector = new Connection();
				String check = connector.start(login_info);

				if (check.equals("sucess")) {

					JSONObject ballot = connector.getBallot();
					new VotingBallot(connector, ballot);
					dispose();
				} else if (check.equals("online"))
					JOptionPane.showMessageDialog(null, "Can't login \n User already login");
				else 
					JOptionPane.showMessageDialog(null, "Wrong Username / Pin / fingerprint");
			}

		});
	}

	private void onBtBrowsePressed() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("./data/fingerprint"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, GIF & PNG Images", "jpg", "gif", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				pic = ImageIO.read(chooser.getSelectedFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
