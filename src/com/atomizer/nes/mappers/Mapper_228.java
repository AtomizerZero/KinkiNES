package com.atomizer.nes.mappers;

import com.atomizer.nes.Cartridge;
import com.atomizer.nes.Mapper;

public class Mapper_228 extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	private static int nPRGBankSwitch = 0;
	private static int nCHRBankSwitch = 0;
	int[] ram = new int[4];
	int[] chrmap = new int [8192];
	int[] prgmap = new int [8192];
	boolean prgmode = false;
	int prgchip = 0;

	public Mapper_228(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		
		if (addr >= 0x8000) {

			Mapper.mapped_address = prgmap[((addr & 0x7FFF)) >> 10] + (addr &1023);
			return true;
		}
		else if (addr < 0x6000) {

			Mapper.mapped_address = ram[addr & 3] & 0xF;
			return true;
		}
		Mapper.mapped_address = addr >> 8;
		return false;
	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		if (addr <= 0x5FFF) {

			ram[addr & 3] = data & 0xF;
		}else if (addr >= 0x8000) {
			nCHRBankSwitch = ((addr & 0xf) << 2) + (data & 3);
			prgmode = ((addr & (32)) != 0);
			nPRGBankSwitch = (addr >> 6) & 0x1F;
			prgchip = (addr >> 11) & 3;
			
			for (int i = 0; i < 8; ++i) {
                chrmap[i] = (1024 * (nCHRBankSwitch * 8 + i)) % Cartridge.vCHRMemory.length;
            }
			int off = 0;
            switch (prgchip) {
                case 0:
                    off = 0;
                    break;
                case 1:
                    off = 0x080000;
                    break;
                case 3:
                    off = 0x100000;
                    break;
                default:
                    System.err.println("Who knows.");
            }
            if (prgmode) {
                for (int i = 0; i < 16; ++i) {
                    prgmap[i] = ((1024 * ((16 * nPRGBankSwitch) + i)) + off) % Cartridge.vPRGMemory.length;
                    prgmap[i + 16] = ((1024 * ((16 * nPRGBankSwitch) + i)) + off) % Cartridge.vPRGMemory.length;
                }
            } else {
                for (int i = 0; i < 32; ++i) {
                    prgmap[i] = ((1024 * ((32 * (nPRGBankSwitch >> 1)) + i)) + off) % Cartridge.vPRGMemory.length;
                }
            }
			return true;
		}
		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x1FFF) {
			mapped_addr = addr;
			Mapper.mapped_address = addr;
			return true;
		}
		return false;
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x1FFF) {

			Mapper.mapped_address = addr;
			return true;

		}
		return false;
	}

	public void reset() {
		nPRGBankSwitch = 0;
		nCHRBankSwitch = 0;

	}

}
