package com.atomizer.nes;

public class Mapper {

	public static int nPRGBanks = 0;
	public static int nCHRBanks = 0;
	private static int nPRGBankSwitch = 0;
	private static int nCHRBankSwitch = 0;
	static int nCHRBankSelect = 0;

	public static int mapped_address = 0;
	public static int mapperNumber = 0;

	public static boolean SMB_EASY = true;
	public static boolean bIRQActive;
	public static boolean bIRQEnable = false;
	public static boolean bIRQUpdate = false;
	public static int nIRQCounter = 0x0000;
	public static int nIRQReload = 0x0000;

	public static int getMapperNumber() {
		return Cartridge.nMapperID;
	}

	public Mapper(int prgBanks, int chrBanks) {
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		switch (getMapperNumber()) {

		case 1:
			break;
		case -28:
			if (addr >= 0x8000 && addr <= 0xFFFF) {
				Mapper.mapped_address = ((nPRGBankSwitch + (addr - 0x8000)));
				return true;
			}
		}
		return false;
	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		switch (getMapperNumber()) {
		case 1:
			break;

		case -28:
			if (addr >= 0x8000 && addr <= 0xFFFF) {
				nPRGBankSwitch = (((data >> 4) & 0xFF) * 0x80000);
				nCHRBankSwitch = ((data & 0xFF) * 0x8000);
				Mapper.mapped_address = ((nPRGBankSwitch + addr) & 0xFFFFFF);
				return true;
			}
		}
		return false;

	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		switch (getMapperNumber()) {

		case 1:
			break;

		case -28:
			if (addr >= 0x0000 && addr <= 0xC000) {
				Mapper.mapped_address = (mapped_addr + (nCHRBankSwitch * 0x10000));

			}
		}
		return false;
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		switch (getMapperNumber()) {

		case 1:
			break;
		case -28:
			if (addr >= 0x0000 && addr <= 0xC000) {
				Mapper.mapped_address = (mapped_addr + (nCHRBankSwitch * 0x8000));

			}

		}
		return false;
	}

	public void reset() {
		nCHRBankSelect = 0;

		nPRGBankSwitch = 0;
		nCHRBankSwitch = 0;
		nCHRBankSelect = 0;

	}
	
	public static int Mirror(int m) {

		Cartridge.mirror = m;
		return m;
	}
	
	
	
	public boolean irqState() {
		return false;
	}
	public void irqClear() {
		
	}
	public void scanline() {
		
	}
}
