package com.me.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.me.controller.Connection;


public class VotingBallot extends JFrame implements ActionListener {
	// private JParser jp;
	private int row = 0;
	private ButtonGroup[] propsButtons;
	private JRadioButton[] selectedPres;
	private Connection connector;
	private JSONObject ballot;

	public VotingBallot(Connection con, JSONObject b) {
		connector = con;
		ballot = b;
		initializeBallot();
	}

	public VotingBallot() throws Exception {
		BufferedReader br21;
		br21 = new BufferedReader(new FileReader("cand.txt"));
		String check = null;
		if ((check = br21.readLine()) != null) {
		}
		br21.close();
		JSONObject data = (JSONObject) new JSONParser().parse(check);
		ballot = (JSONObject) data.get("data");
		// ArrayList presidents = (ArrayList)
		// ballot.get("presidential_candidates");
		// for (Object o : presidents) {
		// System.out.println(o.toString());
		// }
		// System.out.println(ballot.get("vid_hash"));
		initializeBallot();
	}

	private void initializeBallot() {
		// jp = new JParser();
		this.setSize(579, 650);
		this.setTitle("Voting Ballot");
		this.getContentPane().setBackground(new Color(255, 255, 255));
		this.getContentPane().setLayout(null);

		JPanel main_frame = new JPanel();
		main_frame.setBackground(new Color(255, 255, 255));
		main_frame.setBounds(0, 0, 525, 650);
		this.getContentPane().add(main_frame);
		main_frame.setLayout(null);

		JPanel msg_panel = new JPanel();
		msg_panel.setForeground(Color.BLACK);
		msg_panel.setBackground(new Color(153, 204, 255));
		msg_panel.setBounds(0, 0, 525, 40);
		main_frame.add(msg_panel);
		msg_panel.setLayout(null);

		JLabel ballot_msg = new JLabel("Press the Submit after selecting any one of the candiate..");
		ballot_msg.setBounds(10, 11, 384, 20);
		ballot_msg.setFont(new Font("Tahoma", Font.PLAIN, 14));
		msg_panel.add(ballot_msg);
		
		JButton btnLogOut = new JButton("Log Out");
		btnLogOut.setBounds(426, 8, 89, 23);
		msg_panel.add(btnLogOut);
		
		btnLogOut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(connector.logout()){
					new Login();
					dispose();
				}
				else
					JOptionPane.showMessageDialog(null, "Failed to logout");
			}
		});

		JPanel submit_panel = new JPanel();
		submit_panel.setBounds(0, 538, 525, 34);
		main_frame.add(submit_panel);
		submit_panel.setLayout(null);

		JButton btnSubmit = new JButton("Submit");
		btnSubmit.setBounds(204, 5, 89, 23);
		btnSubmit.addActionListener(this);
		submit_panel.add(btnSubmit);

		JPanel ballot_panel = new JPanel();
		GridBagLayout gbl_ballot_panel = new GridBagLayout();
		gbl_ballot_panel.rowWeights = new double[] { 0.0 };
		gbl_ballot_panel.columnWeights = new double[] { 0.0 };
		ballot_panel.setLayout(gbl_ballot_panel);
		GridBagConstraints c = new GridBagConstraints();

		// pres panel
		JPanel pres_panel = new JPanel();
		pres_panel.setPreferredSize(new Dimension(505, 25));
		GridBagLayout gbl_pres_panel = new GridBagLayout();
		pres_panel.setLayout(gbl_pres_panel);
		c.gridx = 0;
		c.gridy = row;
		ballot_panel.add(pres_panel, c);

		JLabel pres_msg = new JLabel("Candidates:");
		pres_msg.setFont(new Font("Tahoma", Font.PLAIN, 15));
		pres_msg.setSize(50, 14);
		GridBagConstraints gbc_pres_msg = new GridBagConstraints();
		gbc_pres_msg.gridx = 0;
		gbc_pres_msg.gridy = 0;
		pres_panel.add(pres_msg, gbc_pres_msg);
		row++;

		// presidential candidates
		ArrayList presidents = (ArrayList) ballot.get("presidential_candidates");
		int sizeOfPres = presidents.size();
		ButtonGroup group = new ButtonGroup();
		selectedPres = new JRadioButton[sizeOfPres];

		int i = 0;
		for (Object o : presidents) {
			JPanel ppanel = new JPanel();
			ppanel.setPreferredSize(new Dimension(500, 130));
			JSONObject p = stringToJson(o.toString());

			// JTextArea msg = new JTextArea(p.get("party_affiliation") + ": " +
			// p.get("full_name"));
			// msg.setBounds(260, 14, 263, 14);
			// ppanel.add(msg);

			ppanel.setLayout(null);

			selectedPres[i] = new JRadioButton("");
			selectedPres[i].setActionCommand("Yes");
			selectedPres[i].setSelected(false);
			group.add(selectedPres[i]);
			selectedPres[i].setBounds(28, 47, 27, 21);
			ppanel.add(selectedPres[i]);

			JLabel lblNull = new JLabel(p.get("party_affiliation") + ": " + p.get("full_name"));
			lblNull.setBounds(75, 10, 350, 14);
			lblNull.setFont(new Font("Tahoma", Font.PLAIN, 13));
			ppanel.add(lblNull);

			ImageIcon ic = null;
//			ByteArrayInputStream bais = new ByteArrayInputStream((Base64.decodeBase64(p.get("image").toString())));
			BufferedImage bf = createImageFromBytes(Base64.decodeBase64(p.get("image").toString()));
			
			try {
				ImageIO.write(bf, "jpg", new File("test.jpg"));
				ic = new ImageIcon(bf);
			} catch (Exception e) {
				e.printStackTrace();
			}
			JLabel label = new JLabel("", ic, JLabel.CENTER);
			label.setBounds(77, 35, 70, 70);
			ppanel.add(label);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBounds(158, 29, 323, 100);
			ppanel.add(scrollPane);

			JTextArea textPane = new JTextArea();
			textPane.setEditable(false);
			textPane.setLineWrap(true);
			textPane.setText(p.get("desc").toString());
			textPane.setFont(new Font("Tahoma", Font.PLAIN, 11));
			scrollPane.setViewportView(textPane);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = row;
			ballot_panel.add(ppanel, c);
			row++;
			i++;
		}

		JScrollPane scroller = new JScrollPane(ballot_panel);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setUnitIncrement(10);
		scroller.setSize(525, 497);
		scroller.setLocation(0, 41);
		ballot_panel.setAutoscrolls(true);

		main_frame.add(scroller);

		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				new Connection().logout();
			}
		});
	}
	
	private BufferedImage createImageFromBytes(byte[] imageData) {
	    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
	    try {
	        return ImageIO.read(bais);
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JSONObject finalBallot = new JSONObject();
		JSONObject a = null;
		ArrayList presidents = (ArrayList) ballot.get("presidential_candidates");
		int i = 0;
		for (Object o : presidents) {
			JSONObject pr = stringToJson(o.toString());
			a = new JSONObject();
			if (selectedPres[i].isSelected()) {
				a.put("pick", "true");
				a.put("id", Integer.parseInt(pr.get("id").toString()));
				break;
			}
			i++;
		}

		finalBallot.put("state", "ballot_response");
		finalBallot.put("presidential_candidate", a);
		System.out.println("calling connector" + finalBallot.toJSONString());

		 if(connector.sendBallot(finalBallot) == true){
		 JOptionPane.showMessageDialog(this, "Vote Accepted!");
		 }else{
		 JOptionPane.showMessageDialog(this, "Already Voted! Or problem with voting");
		 }
	}

	public String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();

			if (button.isSelected()) {
				return button.getText();
			}
		}

		return null;
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

	public static void main(String[] args) throws Exception {
		new VotingBallot().setVisible(true);
	}
}
