package com.atomizer.nes.mappers;

import com.atomizer.nes.Mapper;

public class _mapper_template extends Mapper {
	
	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	
	public _mapper_template(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}
	
	public boolean cpuMapRead(int addr, int mapped_addr) {
		return false;
	}
	public boolean cpuMapWrite(int addr, int mapped_addr) {
		return false;
	}
	public boolean ppuMapRead(int addr, int mapped_addr) {
		return false;
	}
	public boolean ppuMapWrite(int addr, int mapped_addr) {
		return false;
	}
	public void reset() {
	}
	

}
