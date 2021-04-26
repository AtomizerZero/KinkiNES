package com.atomizer.nes.mappers;

import com.atomizer.nes.Mapper;

public class Mapper_000 extends Mapper {
	
	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	
	public Mapper_000(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}
	
	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			Mapper.mapped_address = addr & (nPRGBanks > 1 ? 0x7FFF : 0x3FFF);
			return true;
		}
		return false;
	}
	public boolean cpuMapWrite(int addr, int mapped_addr) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			Mapper.mapped_address = addr & (nPRGBanks > 1 ? 0x7FFF : 0x3FFF);
			return true;
		}
		return false;	}
	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x1FFF) {
			mapped_addr = addr;
			Mapper.mapped_address = mapped_addr;
			return true;
		}	
		return false;
	}
	public boolean ppuMapWrite(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x1FFF) {
			if (nCHRBanks == 0) {
				Mapper.mapped_address = addr; // treat as ram
				return true;
			}
		}
		return false;
	}
	public void reset() {
	}
	

}
