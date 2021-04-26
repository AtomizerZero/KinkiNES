package com.atomizer.nes;

public class Mask {

	public int enhanceBlue;		// MSB
	public int enhanceGreen;
	public int enhanceRed;
	public int render_sprites;
	public int render_background;
	public int renderSpritesLeft;
	public int renderBackgroundLeft;
	public int grayscale;		// LSB

	public int getReg() {
		int reg = 0x00;

		reg += (enhanceBlue << 7);
		reg += (enhanceGreen << 6);
		reg += (enhanceRed << 5);
		reg += (render_sprites << 4);
		reg += (render_background << 3);
		reg += (renderSpritesLeft << 2);
		reg += (renderBackgroundLeft << 1);
		reg += (grayscale << 0);


		return reg;
	}
	
	public void setReg(int data) {
		enhanceBlue = (data & 0x80) > 0? 1: 0;       
		enhanceGreen = (data & 0x40) > 0? 1: 0;       
		enhanceRed = (data & 0x20) > 0? 1: 0;    
		render_sprites = (data & 0x10) > 0? 1: 0;    
		render_background = (data & 0x08) > 0? 1: 0;
		renderSpritesLeft = (data & 0x04) > 0? 1: 0;       
		renderBackgroundLeft = (data & 0x02) > 0? 1: 0; 
		grayscale = (data & 0x01) > 0? 1: 0;
	}
}
