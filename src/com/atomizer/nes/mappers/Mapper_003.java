package com.atomizer.nes.mappers;

import com.atomizer.nes.Mapper;

public class Mapper_003 extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	private static int nCHRBankSelect = 0;

	public Mapper_003(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			if (nPRGBanks == 1) {
				Mapper.mapped_address = addr & 0x3FFF;
			}
			if (nPRGBanks == 2) {
				Mapper.mapped_address = addr & 0x7FFF;
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			nCHRBankSelect = (data & 0x03);
			Mapper.mapped_address = addr;
		}
		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr < 0x2000) {

			Mapper.mapped_address = (nCHRBankSelect * 0x2000) + addr;
			return true;
		} else {
			return false;
		}
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		return false;
	}

	public void reset() {
		nCHRBankSelect = 0;
	}

}
