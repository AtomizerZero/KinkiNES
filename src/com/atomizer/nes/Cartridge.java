package com.atomizer.nes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.atomizer.Main;
import com.atomizer.nes.Cartridge.sHeader;
import com.atomizer.nes.mappers.Mapper_000;
import com.atomizer.nes.mappers.Mapper_001;
import com.atomizer.nes.mappers.Mapper_002;
import com.atomizer.nes.mappers.Mapper_003;
import com.atomizer.nes.mappers.Mapper_004;
import com.atomizer.nes.mappers.Mapper_066;
import com.atomizer.nes.mappers.Mapper_228;

public class Cartridge {

	static boolean bImageValid = false;
	public static int nMapperID = 0;
	public static int nPRGBanks = 0;
	public static int nCHRBanks = 0;
	public static byte[] vPRGMemory = new byte[0];
	public static byte[] vCHRMemory = new byte[0];
	public static int trainer;
	public static int mirror;
	public static Mapper pMapper = new Mapper(nPRGBanks, nCHRBanks);
	public static int cart_data = 0;
	public static int nFileType = 1;
	public static InputStream ifs;

	public static class sHeader { // struct
		public static byte[] name = new byte[4]; // NES_
		public static int prg_rom_chunks; // prg rom size
		public static int chr_rom_chunks; // chr rom size
		public static int mapper1;
		public static byte mapper2;
		public static int prg_ram_size;
		public static int tv_system1;
		public static int tv_system2;
		public static int[] unused = new int[5];
	};

	private static boolean match(byte[] a, byte[] b, int start, int len) {
		for (int i = start; i < start + len; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	private static void debug(String str) {
		System.out.println(str);
	}

	public static String bit(int mapper1) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < 8; i++)
			buff.append((mapper1 >> (7 - i)) & 1);
		return buff.toString();
	}

	@SuppressWarnings("unused")
	private static String bytesToHex(byte[] hashInBytes) {

		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();

	}

	public static File chooseFile(String path) {
		JFileChooser fileChooser = new JFileChooser(path);
		FileNameExtensionFilter nesFilter = new FileNameExtensionFilter(".NES files (*.nes)", "nes");
		fileChooser.setFileFilter(nesFilter);
		fileChooser.setDialogTitle("Choose NES ROM");
		fileChooser.setApproveButtonText("Open NES ROM");
		int res = fileChooser.showOpenDialog(null);
		if (res == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else if (res == JFileChooser.CANCEL_OPTION) {
			fileChooser.cancelSelection();
			if (!Main.boot) {
				Main.bEmulationRun = true;
				Main.cancel = true;
				fileChooser.cancelSelection();
			}
		}
		return null;
	}

	public static boolean loadCartridge(File file) throws IOException {

		try {
			ifs = new FileInputStream(file);
		} catch (NullPointerException e) {
			System.out.println("Cancel selected " + e);
			return false;
		}
		if (!file.exists()) {
			ifs.close();
			return false;
		} else {
			Main.gameName = file.getName().toString();
			debug(Main.gameName);
			byte[] buff = new byte[16];
			ifs.read(buff);
			if (match(buff, new byte[] { 'N', 'E', 'S', 0x1A }, 0, 4)) {
				nFileType = 1;
			}
			debug(new String(buff));
			for (int i = 0; i < 3; i++) {
				sHeader.name[i] = buff[i];
			}
/* @formatter:off */
			if (nFileType == 0) {	
			}
			if (nFileType == 1) {
				
				nPRGBanks = (int)buff[4]; 			debug("PRG Banks: " + nPRGBanks + " " +"(" +(nPRGBanks *16)+"Kb)");
				nCHRBanks = (int)buff[5]; 			debug("CHR Banks: " + nCHRBanks + " " +"(" +(nCHRBanks *8 )+"Kb)");
				
				sHeader.mapper1 = buff[6];			debug("Header1  : " + bit(sHeader.mapper1));
				sHeader.mapper2 = buff[7];			debug("Header2  : " + bit(sHeader.mapper2));
						
				trainer = (sHeader.mapper1 >> 2)&1;
				if (trainer > 0) {
					System.out.println("trainer present! Get a better rom, dumbass!");
				}
				nMapperID = ((sHeader.mapper2 >> 4) << 4)&0xFF | (sHeader.mapper1 >> 4); debug("Mapper ID: " + nMapperID);
				mirror = sHeader.mapper1 &0x01; debug("Mirror   : " + mirror);
				//mirroring 
				//0 - hardware
				//1 = horizontal
				//2 = vertical
				//3 = onescreen_lo
				//4 = onescreen_hi
				
				vPRGMemory = new byte[1 * nPRGBanks*16384];
				ifs.read(vPRGMemory);

				if (nCHRBanks == 0) {
					nCHRBanks = 1;
				}
				vCHRMemory = new byte[1 * 8192 * nCHRBanks];
				ifs.read(vCHRMemory);
				
			if (nFileType == 2) {
				
			}
		}
		switch (nMapperID) {
//    Mapper    |
		case   0: pMapper = new Mapper_000(nPRGBanks,(nCHRBanks));break;
		case   1: pMapper = new Mapper_001(nPRGBanks,(nCHRBanks));break;
		case   2: pMapper = new Mapper_002(nPRGBanks,(nCHRBanks));break;
		case   3: pMapper = new Mapper_003(nPRGBanks,(nCHRBanks));break;
		case   4: pMapper = new Mapper_004(nPRGBanks,(nCHRBanks));break;
		case  66: pMapper = new Mapper_066(nPRGBanks,(nCHRBanks));break;
		case 228: pMapper = new Mapper_228(nPRGBanks,(nCHRBanks));break;
		default : pMapper = new Mapper(nPRGBanks,(nCHRBanks));break;
		}
/* @formatter:on */
			setbImageValid(true);

			ifs.close();
			return getbImageValid();
		}
	}

	public static boolean getbImageValid() {
		return bImageValid;
	}

	public static void setbImageValid(boolean bImageValid) {
		Cartridge.bImageValid = bImageValid;
	}

	public static boolean cpuRead(int addr, int data) {

		int mapped_addr = 0;

		if (pMapper.cpuMapRead(addr, mapped_addr)) {

			if (Mapper.mapped_address == 0xFFFFFFFF) {
				return true;
			} else {
				data = (vPRGMemory[Mapper.mapped_address] & 0xFF);
				cart_data = data;
			}
			return true;
		} else {
			return false;
		}
	}

	public static boolean cpuWrite(int addr, int data) {
		int mapped_addr = 0;

		if (pMapper.cpuMapWrite(addr, mapped_addr, data)) {
			vPRGMemory[Mapper.mapped_address] = (byte) data;
			return true;
		} else {
			return false;
		}
	}

	public static boolean ppuRead(int addr, int data) {

		int mapped_addr = 0;
		if (pMapper.ppuMapRead(addr, mapped_addr)) {
			data = (vCHRMemory[Mapper.mapped_address] & 0xFF);
			cart_data = data;
			return true;
		} else {
			return false;
		}
	}

	public static boolean ppuWrite(int addr, int data) {

		int mapped_addr = 0;
		if (pMapper.ppuMapWrite(addr, mapped_addr)) {
			vCHRMemory[Mapper.mapped_address] = (byte) data;
			return true;
		} else {
			return false;
		}
	}

	public static void reset() {
		if (pMapper != null) {
			pMapper.reset();
			cart_data = 0;
		}
	}

	public static void close() {
		vPRGMemory = new byte[0];
		vCHRMemory = new byte[0];
	}

}
