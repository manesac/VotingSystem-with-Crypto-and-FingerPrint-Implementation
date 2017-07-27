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
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.me.util.FingerPrint;

public class FScann extends JFrame {
	private static final String IMG_PATH = "data/fingerprint/101_4.jpg";
//	File[] pic;
//	int i = 0;
	ImageIcon icon = null;
	int [] fp1;
	int [] fp2;

	public void setFp1(int[] fp1) {
		this.fp1 = fp1;
	}

	public void setFp2(int[] fp2) {
		this.fp2 = fp2;
	}

	public FScann() {
		setSize(594+44, 576+40);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);

//		BufferedImage img = null;
//		try {
//			img = ImageIO.read(new File(IMG_PATH));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		ImageIcon icon = new ImageIcon(img);

		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 288, 494);
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel label = new JLabel((Icon) null);
		label.setBounds(0, 50, 288, 384);
		panel.add(label);
		
		JLabel lblEndpoints = new JLabel("EndPoints =");
		lblEndpoints.setBounds(10, 444, 220, 14);
		panel.add(lblEndpoints);

		JLabel lblCrossingpoints = new JLabel("CrossingPoints =");
		lblCrossingpoints.setBounds(10, 469, 231, 14);
		panel.add(lblCrossingpoints);
		
		JButton btnFirst = new JButton("First");
		btnFirst.setBounds(85, 11, 89, 23);
		panel.add(btnFirst);
		btnFirst.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					BufferedImage bf = onBtBrowsePressed();
					icon = new ImageIcon(bf);
					label.setIcon(icon);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							int[] minute = (new FingerPrint(bf)).execute();
							setFp1(minute);
							lblEndpoints.setText("EndPoints = "+minute[1]);
							lblCrossingpoints.setText("CrossingPoints = "+minute[0]);
						}
					}).start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBounds(296, 0, 288, 494);
		getContentPane().add(panel_1);

		JLabel label_1 = new JLabel((Icon) null);
		label_1.setBounds(0, 50, 288, 384);
		panel_1.add(label_1);
		
		JLabel label_2 = new JLabel("EndPoints =");
		label_2.setBounds(10, 444, 220, 14);
		panel_1.add(label_2);

		JLabel label_3 = new JLabel("CrossingPoints =");
		label_3.setBounds(10, 469, 231, 14);
		panel_1.add(label_3);

		JButton button = new JButton("Second");
		button.setBounds(85, 11, 89, 23);
		panel_1.add(button);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					BufferedImage bf = onBtBrowsePressed();
					icon = new ImageIcon(bf);
					label_1.setIcon(icon);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							int[] minute = (new FingerPrint(bf)).execute();
							setFp2(minute);
							label_2.setText("EndPoints = "+minute[1]);
							label_3.setText("CrossingPoints = "+minute[0]);
						}
					}).start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JButton btnProcess = new JButton("Process");
		btnProcess.setBounds(238, 505, 89, 23);
		getContentPane().add(btnProcess);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(0, 0, 584, 537);
		getContentPane().add(panel_2);
		btnProcess.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, (fp1[0]+fp2[0])/2+"\n"+(fp1[1]+fp2[1])/2);
			}
		});
		setVisible(true);
	}

	public static void main(String[] s) {
		setStyle();
		new FScann();
	}

	private BufferedImage onBtBrowsePressed() throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("./data/fingerprint"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, GIF & PNG Images", "jpg", "gif", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) 
			return ImageIO.read(chooser.getSelectedFile());
		return null;
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
