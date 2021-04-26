package com.atomizer.nes;

public class LoopyRegister {

	public int unused;		// MSB   // 1 bit
	public int fine_y;                // 3 bit
	public int nametable_y;           // 1 bit
	public int nametable_x;           // 1 bit
	public int coarse_y;              // 5 bit
	public int coarse_x;		// LSB   // 5 bit
	
	public int getReg() {
		int reg = 0x00;

		reg += (unused << 15);	  
		reg += (fine_y << 12);    
		reg += (nametable_y << 11); 
		reg += (nametable_x << 10); 
		reg += (coarse_y << 5);	  
		reg += (coarse_x << 0);    

		return reg;
	}
	
	public void setReg(int data) {
		unused = (data & 0x8000) > 0? 1: 0; 
		fine_y = (data & 0x7000) >> 12; 
		nametable_y = (data & 0x0800) > 0? 1: 0;       
		nametable_x = (data & 0x0400) > 0? 1: 0;
		coarse_y = (data & 0x03E0) >> 5;    
		coarse_x = (data & 0x001F);    
	}
}
