package com.atomizer.nes;

public class Bus {

	public static int[] cpuRam = new int[2048]; 

	static int nSystemClockCounter = 0;

	static Cartridge cart = new Cartridge();
	static olc2C02 ppu = new olc2C02();
	private static int dma_page = 0x00;
	private static int dma_addr = 0x00;
	private static int dma_data = 0x00;
	private static boolean dma_dummy = true;
	private static boolean dma_transfer = false;

	public static int[] controller = new int[2];
	private static int[] controller_state = new int[2];

	public static int disAddr;

	public static void cpuWrite(int addr, int data) {

		if (Cartridge.cpuWrite(addr, data) == true) {

		} else if (addr >= 0x0000 && addr <= 0x1FFF) {
			cpuRam[addr & 0x07FF] = data;
		} else if (addr >= 0x2000 && addr <= 0x3FFF) {
			ppu.cpuWrite(addr & 0x0007, data);
		} else if (addr == 0x4014) {
			dma_page = data;
			dma_addr = 0x00;
			dma_transfer = true;
		} else if (addr >= 0x4016 && addr <= 0x4017) {
			controller_state[addr & 0x0001] = controller[addr & 0x0001];
			//System.out.println(controller_state[addr & 0x0001]);
		}
		disAddr = addr; // for debug

	}

	public static int cpuRead(int addr, boolean bReadOnly) {

		int data = 0x00;

		if (Cartridge.cpuRead(addr, data)) {

		} else if (addr >= 0x0000 && addr <= 0x1FFF) {
			Cartridge.cart_data = cpuRam[addr & 0x07FF];

		} else if (addr >= 0x2000 && addr <= 0x3FFF) {
			Cartridge.cart_data = ppu.cpuRead(addr & 0x0007, bReadOnly);

		} else if (addr >= 0x4016 && addr <= 0x4017) {
			Cartridge.cart_data = (controller_state[addr & 0x0001] & 0x80) > 0 ? 1 : 0;
			controller_state[addr & 0x0001] = (controller_state[addr & 0x0001] << 1) & 0xFF;
			//System.out.println(controller_state[addr & 0x0001]);
		}
		disAddr = addr;
		data = Cartridge.cart_data;
		return data;

	}

	public static void insertCartridge(Cartridge cartridge) {
		Bus.cart = cartridge;

	}

	public static void reset() {
		Cartridge.reset();
		olc6502.reset();
		ppu.reset();
		nSystemClockCounter = 0;
		dma_page = 0x00;
		dma_addr = 0x00;
		dma_data = 0x00;
		dma_dummy = true;
		dma_transfer = false;
	}

	public synchronized static void clock() 
	{
		ppu.clock();
		if (nSystemClockCounter % 3 == 0) 
		{
			if (dma_transfer) 
			{
				if (dma_dummy) 
				{
					if (nSystemClockCounter % 2 == 1) 
					{
						dma_dummy = false;
					}
				} 
				else 
				{
					if (nSystemClockCounter % 2 == 0) 
					{
						dma_data = cpuRead((dma_page << 8) | (dma_addr), false)&0xFF;
					} 
					else 
					{
						olc2C02.setOamData((dma_addr) &0xFF, (dma_data) &0xFF);
						dma_addr = (dma_addr + 1) & 0xFF;
						if (dma_addr == 0x00) 
						{
							dma_transfer = false;
							dma_dummy = true;
						}
					}
				}
			} 
			else 
			{
				olc6502.clock();
				nSystemClockCounter = 0;
			}
		}
		if (ppu.nmi) {
			ppu.nmi = false;
			olc6502.nmi();
		}
		nSystemClockCounter = nSystemClockCounter + 1;

	}

}
