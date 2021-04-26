package com.atomizer.nes.mappers;

import com.atomizer.nes.Mapper;

public class Mapper_066 extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	private static int nPRGBankSwitch = 0;
	private static int nCHRBankSwitch = 0;

	public Mapper_066(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			Mapper.mapped_address = ((nPRGBankSwitch * 0x8000) + (addr & 0x7FFF));
			return true;
		}
		return false;
	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		if (addr >= 0x8000 && addr <= 0xFFFF) {
			nPRGBankSwitch = (((data & 0x30) >> 4) % (nPRGBanks / 2));
			nCHRBankSwitch = (data % nCHRBanks);
			if (nPRGBanks > 4) {
				Mapper.mapped_address = (addr + (nPRGBankSwitch * 0x8000) & 0x7FFF);
			} else {
				if (!SMB_EASY) {
					Mapper.mapped_address = ((nPRGBankSwitch + addr) &0xFFFF);
				}
			}
			return true;
		}
		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x1FFF) {
			Mapper.mapped_address = (addr + (nCHRBankSwitch * 0x2000));
			return true;
		}
		return false;
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x1FFF) {
			Mapper.mapped_address = (addr + (nCHRBankSwitch * 0x2000));
			return true;
		}
		return false;
	}

	public void reset() {
		nPRGBankSwitch = 0;
		nCHRBankSwitch = 0;

	}

}
