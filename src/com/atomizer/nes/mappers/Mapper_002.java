package com.atomizer.nes.mappers;

import com.atomizer.nes.Mapper;

public class Mapper_002 extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	private static int nPRGBankSelectLo = 0;
	private static int nPRGBankSelectHi = 0;

	public Mapper_002(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x8000 && addr <= 0xBFFF) {
			Mapper.mapped_address = ((nPRGBankSelectLo * 0x4000) + (addr & 0x3FFF));
			return true;
		}
		if (addr >= 0xC000 && addr <= 0xFFFF) {
			Mapper.mapped_address = ((nPRGBankSelectHi * 0x4000) + (addr & 0x3FFF));
			return true;
		}
		return false;
	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			nPRGBankSelectLo = data & 0x0F;
		}
		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr < 0x2000) {
			Mapper.mapped_address = addr;
			return true;
		} else {
			return false;
		}
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		if (addr < 0x2000) {
			if (nCHRBanks == 1) {
				Mapper.mapped_address = addr;
				return true;
			}
		}
		return false;
	}

	public void reset() {
		nPRGBankSelectLo = 0;
		nPRGBankSelectHi = nPRGBanks - 1;
	}

}
