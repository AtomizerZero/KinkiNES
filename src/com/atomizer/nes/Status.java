package com.atomizer.nes;

public class Status {

	public int verticalBlank;
	public int spriteZeroHit;
	public int spriteOverflow;
	public int unused;		// 5 bit

	public int getReg() {
		int reg = 0x00;

		reg += (verticalBlank << 7);
		reg += (spriteZeroHit << 6);
		reg += (spriteOverflow << 5);
		reg += (unused << 0);
		return reg;
	}
	
	public void setReg(int data) {
		verticalBlank = (data & 0x80) > 0? 1: 0;
		spriteZeroHit = (data & 0x40) > 0? 1: 0; 
		spriteOverflow = (data & 0x20) > 0? 1: 0;       
		unused = (data & 0x1F);
	}
}
