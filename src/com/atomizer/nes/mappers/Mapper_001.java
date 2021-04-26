package com.atomizer.nes.mappers;

import com.atomizer.nes.Cartridge;
import com.atomizer.nes.Mapper;

public class Mapper_001 extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;

	static int vRAMStatic[] = new int[32 * 1024];

	static int nCHRBankSelect4Lo = 0x00;
	static int nCHRBankSelect4Hi = 0x00;
	static int nCHRBankSelect8 = 0x00;

	static int nPRGBankSelect16Lo = 0x00;
	static int nPRGBankSelect16Hi = 0x00;
	static int nPRGBankSelect32 = 0x00;

	static int nLoadRegister = 0x00;
	static int nLoadRegisterCount = 0x00;
	static int nControlRegister = 0x00;

	public Mapper_001(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
		Mapper.Mirror(1);
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x6000 && addr <= 0x7FFF) {
			Mapper.mapped_address = 0xFFFFFFFF;
			Cartridge.cart_data = vRAMStatic[addr & 0x1FFF];
			return true;
		}
		if (addr >= 0x8000) {
			if ((nControlRegister & (0b01000)) >= 1) {
				if (addr >= 0x8000 && addr <= 0xBFFF) {
					Mapper.mapped_address = ((nPRGBankSelect16Lo * 0x4000 + (addr & 0x3FFF)));
					return true;
				}
				if (addr >= 0xC000 && addr <= 0xFFFF) {
					Mapper.mapped_address = (nPRGBankSelect16Hi * 0x4000 + (addr & 0x3FFF));
					return true;
				}
			} else {
				Mapper.mapped_address = nPRGBankSelect32 * 0x8000 + (addr & 0x7FFF);
				return true;
			}
		}
		return false;

	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		if (addr >= 0x6000 && addr <= 0x7FFF) {
			// Write is to static ram on cartridge
			Mapper.mapped_address = 0xFFFFFFFF;

			// Write data to RAM
			vRAMStatic[addr & 0x1FFF] = data;

			// Signal mapper has handled request
			return true;
		}

		if (addr >= 0x8000) {
			if ((data & 0x80) > 0) {
				// MSB is set, so reset serial loading
				nLoadRegister = 0x00;
				nLoadRegisterCount = 0;
				nControlRegister = nControlRegister | 0x0C;
			} else {
				// Load data in serially into load register
				// It arrives LSB first, so implant this at
				// bit 5. After 5 writes, the register is ready
				nLoadRegister >>= 1;
				nLoadRegister |= (data & 0x01) << 4;
				nLoadRegisterCount++;

				if (nLoadRegisterCount == 5) {
					// Get Mapper Target Register, by examining
					// bits 13 & 14 of the address
					int nTargetRegister = (addr >> 13) & 0x03;

					if (nTargetRegister == 0) // 0x8000 - 0x9FFF
					{
						// Set Control Register
						nControlRegister = nLoadRegister & 0x1F;

						switch (nControlRegister & 0x03) {
						case 0:
							Mapper.Mirror(3);
							break;
						case 1:
							Mapper.Mirror(4);
							break;
						case 2:
							Mapper.Mirror(2);
							break;
						case 3:
							Mapper.Mirror(1);
							break;
						}
					} else if (nTargetRegister == 1) // 0xA000 - 0xBFFF
					{
						// Set CHR Bank Lo
						if ((nControlRegister & 0b10000) > 0) {
							// 4K CHR Bank at PPU 0x0000
							nCHRBankSelect4Lo = nLoadRegister & 0x1F;
						} else {
							// 8K CHR Bank at PPU 0x0000
							nCHRBankSelect8 = nLoadRegister & 0x1E;
						}
					} else if (nTargetRegister == 2) // 0xC000 - 0xDFFF
					{
						// Set CHR Bank Hi
						if ((nControlRegister & 0b10000) > 0) {
							// 4K CHR Bank at PPU 0x1000
							nCHRBankSelect4Hi = nLoadRegister & 0x1F;
						}
					} else if (nTargetRegister == 3) // 0xE000 - 0xFFFF
					{
						// Configure PRG Banks
						int nPRGMode = (nControlRegister >> 2) & 0x03;

						if (nPRGMode == 0 || nPRGMode == 1) {
							// Set 32K PRG Bank at CPU 0x8000
							nPRGBankSelect32 = (nLoadRegister & 0x0E) >> 1;
						} else if (nPRGMode == 2) {
							// Fix 16KB PRG Bank at CPU 0x8000 to First Bank
							nPRGBankSelect16Lo = 0;
							// Set 16KB PRG Bank at CPU 0xC000
							nPRGBankSelect16Hi = nLoadRegister & 0x0F;
						} else if (nPRGMode == 3) {
							// Set 16KB PRG Bank at CPU 0x8000
							nPRGBankSelect16Lo = nLoadRegister & 0x0F;
							// Fix 16KB PRG Bank at CPU 0xC000 to Last Bank
							nPRGBankSelect16Hi = nPRGBanks - 1;
						}
					}

					// 5 bits were written, and decoded, so
					// reset load register
					nLoadRegister = 0x00;
					nLoadRegisterCount = 0;
				}

			}

		}

		// Mapper has handled write, but do not update ROMs
		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr < 0x2000) {
			if (nCHRBanks == 0) {
				mapped_addr = addr & 0xFFFF;
				return true;
			} else {
				if ((nControlRegister & 0b10000) >= 1) {
					// 4K CHR Bank Mode
					if (addr >= 0x0000 && addr <= 0x0FFF) {
						Mapper.mapped_address = nCHRBankSelect4Lo * 0x1000 + (addr & 0x0FFF);
						return true;
					}

					if (addr >= 0x1000 && addr <= 0x1FFF) {
						Mapper.mapped_address = nCHRBankSelect4Hi * 0x1000 + (addr & 0x0FFF);
						return true;
					}
				} else {
					// 8K CHR Bank Mode
					Mapper.mapped_address = nCHRBankSelect8 * 0x2000 + (addr & 0x1FFF);
					return true;
				}
			}
		}

		return false;
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {

		if (addr < 0x2000) {
			if (nCHRBanks == 0) {
				Mapper.mapped_address = addr;
				return true;
			}

			return true;
		} else {
			return false;
		}
	}

	public void reset() {
		nControlRegister = 0x1C;
		nLoadRegister = 0x00;
		nLoadRegisterCount = 0x00;

		nCHRBankSelect4Lo = 0;
		nCHRBankSelect4Hi = 0;
		nCHRBankSelect8 = 0;

		nPRGBankSelect32 = 0;
		nPRGBankSelect16Lo = 0;
		nPRGBankSelect16Hi = nPRGBanks - 1;
	}

}
