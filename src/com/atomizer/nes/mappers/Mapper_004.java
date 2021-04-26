package com.atomizer.nes.mappers;

import com.atomizer.nes.Cartridge;
import com.atomizer.nes.Mapper;

public class Mapper_004 extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	static int vRAMStatic[] = new int[32 * 1024];
	static int pPRGBank[] = new int[4];
	static int pCHRBank[] = new int[8];
	static int pRegister[] = new int[8];
	static int nTargetRegister = 0x00;
	static int bPRGBankMode = 0;
	static int bCHRInversion = 0;
	
	public Mapper_004(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x6000 && addr <= 0x7FFF) {
			
			Mapper.mapped_address = 0xFFFFFFFF;
			Cartridge.cart_data = vRAMStatic[addr & 0x1FFF];
			return true;
		}

		if (addr >= 0x8000 && addr <= 0x9FFF) {
			Mapper.mapped_address = pPRGBank[0] + (addr & 0x1FFF);
			return true;
		}

		if (addr >= 0xA000 && addr <= 0xBFFF) {
			Mapper.mapped_address = pPRGBank[1] + (addr & 0x1FFF);
			return true;
		}

		if (addr >= 0xC000 && addr <= 0xDFFF) {
			Mapper.mapped_address = pPRGBank[2] + (addr & 0x1FFF);
			return true;
		}

		if (addr >= 0xE000 && addr <= 0xFFFF) {
			Mapper.mapped_address = pPRGBank[3] + (addr & 0x1FFF);
			return true;
		}
		return false;
	}

	public boolean cpuMapWrite(int addr, int mapped_addr) {
		if (addr >= 0x6000 && addr <= 0x7FFF) {
			// Write is to static ram on cartridge
			Mapper.mapped_address = 0xFFFFFFFF;

			// Write data to RAM
			vRAMStatic[addr & 0x1FFF] = Cartridge.cart_data;

			// Signal mapper has handled request
			return true;
		}

		if (addr >= 0x8000 && addr <= 0x9FFF) {
			if ((addr & 0x0001) == 0) {
				nTargetRegister = Cartridge.cart_data & 0x07;
				bPRGBankMode = (Cartridge.cart_data & 0x40);
				bCHRInversion = (Cartridge.cart_data & 0x80);
			} else {
				// Update target register
				pRegister[nTargetRegister] = Cartridge.cart_data;

				// Update Pointer Table
				if (bCHRInversion > 0) {
					pCHRBank[0] = pRegister[2] * 0x0400;
					pCHRBank[1] = pRegister[3] * 0x0400;
					pCHRBank[2] = pRegister[4] * 0x0400;
					pCHRBank[3] = pRegister[5] * 0x0400;
					pCHRBank[4] = (pRegister[0] & 0xFE) * 0x0400;
					pCHRBank[5] = pRegister[0] * 0x0400 + 0x0400;
					pCHRBank[6] = (pRegister[1] & 0xFE) * 0x0400;
					pCHRBank[7] = pRegister[1] * 0x0400 + 0x0400;
				} else {
					pCHRBank[0] = (pRegister[0] & 0xFE) * 0x0400;
					pCHRBank[1] = pRegister[0] * 0x0400 + 0x0400;
					pCHRBank[2] = (pRegister[1] & 0xFE) * 0x0400;
					pCHRBank[3] = pRegister[1] * 0x0400 + 0x0400;
					pCHRBank[4] = pRegister[2] * 0x0400;
					pCHRBank[5] = pRegister[3] * 0x0400;
					pCHRBank[6] = pRegister[4] * 0x0400;
					pCHRBank[7] = pRegister[5] * 0x0400;
				}

				if (bPRGBankMode > 0) {
					pPRGBank[2] = (pRegister[6] & 0x3F) * 0x2000;
					pPRGBank[0] = (nPRGBanks * 2 - 2) * 0x2000;
				} else {
					pPRGBank[0] = (pRegister[6] & 0x3F) * 0x2000;
					pPRGBank[2] = (nPRGBanks * 2 - 2) * 0x2000;
				}

				pPRGBank[1] = (pRegister[7] & 0x3F) * 0x2000;
				pPRGBank[3] = (nPRGBanks * 2 - 1) * 0x2000;

			}

			return false;
		}

		if (addr >= 0xA000 && addr <= 0xBFFF) {
			if ((addr & 0x0001) == 0) {
				// Mirroring
				if ((Cartridge.cart_data & 0x01) > 0)
					Mapper.Mirror(1); // H
				else
					Mapper.Mirror(2); // V
			} else {
				// PRG Ram Protect
				// TODO:
			}
			return false;
		}

		if (addr >= 0xC000 && addr <= 0xDFFF) {
			if ((addr & 0x0001) == 0) {
				nIRQReload = Cartridge.cart_data;
			} else {
				nIRQCounter = 0x0000;
			}
			return false;
		}

		if (addr >= 0xE000 && addr <= 0xFFFF) {
			if ((addr & 0x0001) == 0) {
				Mapper.bIRQEnable = false;
				Mapper.bIRQActive = false;
			} else {
				Mapper.bIRQEnable = true;
			}
			return false;
		}

		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr <= 0x03FF) {
			Mapper.mapped_address = pCHRBank[0] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x0400 && addr <= 0x07FF) {
			Mapper.mapped_address = pCHRBank[1] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x0800 && addr <= 0x0BFF) {
			Mapper.mapped_address = pCHRBank[2] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x0C00 && addr <= 0x0FFF) {
			Mapper.mapped_address = pCHRBank[3] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x1000 && addr <= 0x13FF) {
			Mapper.mapped_address = pCHRBank[4] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x1400 && addr <= 0x17FF) {
			Mapper.mapped_address = pCHRBank[5] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x1800 && addr <= 0x1BFF) {
			Mapper.mapped_address = pCHRBank[6] + (addr & 0x03FF);
			return true;
		}

		if (addr >= 0x1C00 && addr <= 0x1FFF) {
			Mapper.mapped_address = pCHRBank[7] + (addr & 0x03FF);
			return true;
		}
		return false;
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		return false;
	}

	public void reset() {
		nTargetRegister = 0x00;
		bPRGBankMode = 0;
		bCHRInversion = 0;
		Mapper.Mirror(1);

		bIRQActive = false;
		bIRQEnable = false;
		bIRQUpdate = false;
		nIRQCounter = 0x0000;
		nIRQReload = 0x0000;

		for (int i = 0; i < 4; i++)
			pPRGBank[i] = 0;
		for (int i = 0; i < 8; i++) {
			pCHRBank[i] = 0;
			pRegister[i] = 0;
		}

		pPRGBank[0] = 0 * 0x2000;
		pPRGBank[1] = 1 * 0x2000;
		pPRGBank[2] = (nPRGBanks * 2 - 2) * 0x2000;
		pPRGBank[3] = (nPRGBanks * 2 - 1) * 0x2000;
	}

	public boolean irqState() {
		return bIRQActive;
	}

	public void irqClear() {
		bIRQActive = false;

	}

	public void scanline() {
		{
			if (nIRQCounter == 0)
			{		
				nIRQCounter = nIRQReload;
			}
			else
				nIRQCounter--;

			if (nIRQCounter == 0 && bIRQEnable)
			{
				bIRQActive = true;
			}
			
		}
	}

}