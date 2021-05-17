package user_interface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import appli.Application;

public class UI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private JPanel panel;
	private JPanel analisisPanel;
	private int actualImage;
	private JLabel image, fileName, results;
	private JButton previous, next, analyse;
	private int tries;
	private int errors;
	public UI() {
		this.panel = new JPanel(new BorderLayout());
		this.actualImage = 1;
		this.tries = 0;
		this.errors = 0;
		
		this.fileName = new JLabel(actualImage+".jpg");
		this.fileName.setHorizontalAlignment(JLabel.CENTER);
		
		this.previous = new JButton("<");
		this.previous.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) { 
					previous();
				}
			});
		
		this.next = new JButton(">");
		this.next.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) { 
				next();
			}
		});
		
		BufferedImage image;
		try {
			image = ImageIO.read(new File("src/img/"+actualImage+".jpg"));
			image = resizeImageUntilItFits(image);
			this.image = new JLabel(new ImageIcon(image));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		this.analisisPanel = new JPanel();
		this.analyse = new JButton("Analyser l'image");
		this.analyse.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) { 
				analyse();
			}
		});
		this.results = new JLabel("En attente d'une analyse");
		
		this.analisisPanel.add(this.analyse);
		this.analisisPanel.add(this.results);
		
		this.panel.add(this.fileName, BorderLayout.NORTH);
		this.panel.add(this.previous, BorderLayout.WEST);
		this.panel.add(this.next, BorderLayout.EAST);
		this.panel.add(this.image, BorderLayout.CENTER);
		this.panel.add(this.analisisPanel, BorderLayout.SOUTH);
		
		
		
		this.add(panel);
		this.setSize(600,600);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void analyse() {
		int result = -1;
		int expected = -1;
		
		// récupérer les infos et les mettre dans result et expected
		try {
			result = Application.getLiquidQuantity("src/img/"+actualImage+".jpg");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(failure(expected, result)) {
			this.errors++;
		}
		this.tries++;
		this.results.setText("Résultat: "+result+"% Attendu: "+expected+"% "+this.successRate());
	}
	
	public void previous() {
		if(actualImage==1) {
			actualImage=73;
		}
		actualImage--;
		try {
			this.image.setIcon(new ImageIcon(resizeImageUntilItFits(ImageIO.read(new File("src/img/"+actualImage+".jpg")))));
			this.fileName.setText(actualImage+".jpg");
			this.results.setText("En attente d'une analyse. "+this.successRate());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void next() {
		if(actualImage==72) {
			actualImage=0;
		}
		actualImage++;
		try {
			this.image.setIcon(new ImageIcon(resizeImageUntilItFits(ImageIO.read(new File("src/img/"+actualImage+".jpg")))));
			this.fileName.setText(actualImage+".jpg");
			this.results.setText("En attente d'une analyse "+this.successRate());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String successRate() {
		if(this.tries == 0) {
			return "Taux de réussite: -%";
		}
		else {
			double num=this.errors;
			double denum=this.tries;
			int result = (int) (num/denum*100);
			return "Taux de réussite (avec 5% de marge): "+result+"%";
		}
	}
	
	private boolean failure(int expected, int result) {
		int marge = 5;
		if(expected-5<result || expected+5>result) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public BufferedImage resizeImageUntilItFits(BufferedImage image) {
		while(image.getWidth()>550 || image.getHeight()>550) {
			int newWidth = image.getWidth()/2;
			int newHeight = image.getHeight()/2;
			Image tmp = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
			image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = image.createGraphics();
			g2d.drawImage(tmp, 0, 0, null);
			g2d.dispose();
		}
		return image;
	}
}
