

import com.atomizer.nes.Cartridge;
import com.atomizer.nes.Mapper;

public class Mapper_004_back extends Mapper {

	static int nPRGBanks = 0;
	static int nCHRBanks = 0;
	public static int[] BankRegister = new int[8];
	public static int[] CHRMappings = new int[8];
	public static int[] PRGMappings = new int[4];
	public static int bank = 0;
	public static int rest = 0;
	public static int register = 0;

	public Mapper_004_back(int prgBanks, int chrBanks) {
		super(prgBanks, chrBanks);
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}

	public boolean cpuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x8000) {
			bank = (addr - 0x8000) / 0x2000;
			rest = (addr - 0x8000) % 0x2000;
			Mapper.mapped_address = rest+ (PRGMappings[bank] * 0x2000)&0xFFFF;
			return true;
		}
		return false;
	}

	public boolean cpuMapWrite(int addr, int mapped_addr, int data) {
		if (addr >= 0x8000 && addr <= 0x9FFF) {
			if ((addr & 0x1) != 0) {
				register = data &0xFF;
				return true;
			} else {
				BankRegister[register & 0x07] = data &0xFF;
				return true;
			}
		}
		//doBankSwitch();
		if (addr >= 0xA000 && addr <= 0xBFFF) {
			if ((data & 0x01) == 0x00) {
				Cartridge.mirror = 0x00;
				return true;
			}
		}

		return false;
	}

	public boolean ppuMapRead(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr < 0x2000) {
			bank = addr / 0x0400;
			rest = addr % 0x0400;
			Mapper.mapped_address = (CHRMappings[bank]&0xFF * 0x0400) + rest;
			return true;
		}
		return false;
	}

	public boolean ppuMapWrite(int addr, int mapped_addr) {
		if (addr >= 0x0000 && addr < 0x2000) {
		Mapper.mapped_address = addr;
		return true;
		}
		return false;
	}

	public void reset() {
		/*
		int n = Cartridge.vPRGMemory.length & 0xFF / 0x2000;
		PRGMappings[0] = 0 &0xFF;
		PRGMappings[1] = 1 &0xFF;
		PRGMappings[2] = n - 2 &0xFF;
		PRGMappings[3] = n - 1 &0xFF;*/
	}

	
	
	
	/*public void doBankSwitch() {
		int chrMode = (register >> 7) & 0x01;
		int prgMode = (register >> 6) & 0x01;

		if (chrMode == 0x00) {
			CHRMappings[0] = BankRegister[0] & 0xfe &0xFF;
			CHRMappings[1] = BankRegister[0] | 0x01 &0xFF;
			CHRMappings[2] = BankRegister[1] & 0xfe &0xFF;
			CHRMappings[3] = BankRegister[1] | 0x01 &0xFF;
			CHRMappings[4] = BankRegister[2] &0xFF;
			CHRMappings[5] = BankRegister[3] &0xFF;
			CHRMappings[6] = BankRegister[4] &0xFF;
			CHRMappings[7] = BankRegister[5] &0xFF;
		} else {
			CHRMappings[0] = BankRegister[2] &0xFF;
			CHRMappings[1] = BankRegister[3] &0xFF;
			CHRMappings[2] = BankRegister[4] &0xFF;
			CHRMappings[3] = BankRegister[5] &0xFF;
			CHRMappings[4] = BankRegister[0] & 0xfe &0xFF;
			CHRMappings[5] = BankRegister[0] | 0x01 &0xFF;
			CHRMappings[6] = BankRegister[1] & 0xfe &0xFF;
			CHRMappings[7] = BankRegister[1] | 0x01 &0xFF;
		}

		int n = Cartridge.vPRGMemory.length &0xFF / 0x2000;

		if (prgMode == 0x00) {
			PRGMappings[0] = BankRegister[6] &0xFF;
			PRGMappings[1] = BankRegister[7] &0xFF;
			PRGMappings[2] = n - 2 &0xFF;
			PRGMappings[3] = n - 1 &0xFF;
		} else {
			PRGMappings[0] = n - 2 &0xFF;
			PRGMappings[1] = BankRegister[7] &0xFF;
			PRGMappings[2] = BankRegister[6] &0xFF;
			PRGMappings[3] = n - 1 &0xFF;
		}

	}
*/
}
