package com.atomizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.atomizer.nes.Cartridge;
import com.atomizer.nes.Cartridge.sHeader;
import com.atomizer.nes.Mapper;
import com.atomizer.nes.olc2C02;

public class PatternTable0 extends JPanel {
	public static BufferedImage image3;
	public static BufferedImage image4;
	public static BufferedImage image5;
	public static BufferedImage image6;
	public static Font font;
	public String name = "";
	public String game1 = "";
	public String game2 = "";
	public String game3 = "";
	public String game4 = "";

	public static Graphics g3;
	int width = 256 * 2 + 256;
	int height = 256 + 38;

	boolean painting = false;

	public PatternTable0() {
		createFont("NES3.ttf");

		image3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image4 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image5 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image6 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		g3 = image3.createGraphics();
		g3 = image4.createGraphics();
		g3 = image5.createGraphics();
		g3 = image6.createGraphics();
	}

	public void refresh() {
		if (!painting) {
			painting = true;
			repaint();
		}
	}

	private void drawString(Graphics g3, String text, int x, int y) {
		for (String line : text.split("\n"))
			g3.drawString(line, x, y += g3.getFontMetrics().getHeight()+1);
	}

	public String Rpad(String str, int len, char c) {
		return (str + String.format("%" + len + "s", "").replace(" ", String.valueOf(c))).substring(0, len);
	}

	@Override
	public void paint(Graphics g3) {
		super.paint(g3);
		int w = frame.getContentPane().getWidth();
		int h = frame.getContentPane().getHeight();
		g3.setFont(font.deriveFont(16f));
		g3.drawImage(image3, w - w, h - h+1, w * 2, h * 2, this);
		g3.drawImage(image4, w / 2 - 130, h - h+1, w * 2, h * 2, this);
		g3.setColor(new Color(204, 166, 166));
		g3.fillRect((Main.nSelectedPalette * 98) - 3, h - 39, 100, 20 * 2);
		g3.drawImage(image5, 8, h - h + 238, ((w * 8) * 8) - (w * 32) - (254 * 39), h * 30, this);
		for (int i = 0; i < 8; i++) {

			drawSwatches((4 * i) + (12 * 4) + i - 48, h - h + 1, i);
		}
		g3.setColor(Color.BLACK);
		g3.fillRect(514 + 4, 0, 260, 265);
		g3.setColor(Color.WHITE);
		if (!Main.boot) {
			
			name = Rpad(Main.gameName,128,' ');
			game1 = name.substring(0,16);
			game2 = name.substring(16,32);
			game3 = name.substring(32,48);

		}
		String s = String.format(game1 + "%n" + game2 + "%n" + game3 + "%n"  
				+ "PRG #:" + Mapper.nPRGBanks + "" + "(" + (Mapper.nPRGBanks * 16) + "Kb)" + "%n" 
				+ "CHR #:" + Mapper.nCHRBanks + "" + "("
				+ (Mapper.nCHRBanks * 8) + "Kb)" + "%n" + "Header1:" + Cartridge.bit(sHeader.mapper1) + "%n"
				+ "Header2:" + Cartridge.bit(sHeader.mapper2) + "%n" + "Mapper :" + Cartridge.nMapperID + "%n"
				+ "Mirror :" + Cartridge.mirror);
		drawString(g3, s, 518, -3);
		painting = false;
		g3.dispose();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	private void drawSwatches(int x, int y, int index) {

		for (int i = 0; i < 4; i++) {
			int c = olc2C02.GetColourFromPaletteRam(index, i);
			Color colour = new Color(c);
			g3.setColor(colour);
			image5.setRGB(x + i, y, c);
		}
	}

	JFrame frame = null;

	public void showAsFrame() {
		if (frame == null) {
			frame = new JFrame("Pattern Table");
			this.setBackground(new Color(73, 73, 73));
			// frame.setUndecorated(true);
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(this);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setResizable(false);
			frame.setLocation(0, height);
			frame.pack();
			frame.setVisible(true);
		}
	}

	void createFont(String fontFile) {

		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFile));
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
