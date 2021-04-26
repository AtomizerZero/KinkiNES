package com.atomizer.nes;

public class Control {

	public int enableNmi;   // MSB
	public int slaveMode; // unused
	public int spriteSize;
	public int patternBackground;
	public int patternSprite;
	public int incrementMode;
	public int nametableY;
	public int nametableX;  // LSB
	
	public int getReg() {
		int reg = 0x00;
		
		reg += (enableNmi << 7);
		reg += (slaveMode << 6);
		reg += (spriteSize << 5);
		reg += (patternBackground << 4);
		reg += (patternSprite << 3);
		reg += (incrementMode << 2);
		reg += (nametableY << 1);
		reg += (nametableX << 0);

		return reg;
	}
	
	public void setReg(int data) {
		enableNmi = (data & 0x80) > 0? 1: 0;       
		slaveMode = (data & 0x40) > 0? 1: 0;       
		spriteSize = (data & 0x20) > 0? 1: 0;    
		patternBackground = (data & 0x10) > 0? 1: 0;    
		patternSprite = (data & 0x08) > 0? 1: 0;
		incrementMode = (data & 0x04) > 0? 1: 0;       
		nametableY = (data & 0x02) > 0? 1: 0; 
		nametableX = (data & 0x01) > 0? 1: 0;
	}
}
