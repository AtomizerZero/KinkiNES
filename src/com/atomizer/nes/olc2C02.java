package com.atomizer.nes;

import com.atomizer.Main;
import com.atomizer.PatternTable0;
import com.atomizer.nes.Status;

//ppu
public class olc2C02 {

	private static int[][] tblName = new int[2][1024];
	private static int[][] tblPattern = new int[2][4096];
	private static int[] tblPalette = new int[32];

	private static int palScreen = 0;

	public static boolean frame_complete = false;
	private static int scanline = 0;
	private static int scanDisplay = 0;
	private static int cycle = 0;
	private static Control control = new Control();
	private static Mask mask = new Mask();
	private static Status status = new Status();

	private static LoopyRegister vram_addr = new LoopyRegister();
	private static LoopyRegister tram_addr = new LoopyRegister();

	private static int address_latch = 0x00;
	private static int ppu_data_buffer = 0x00;
	private static int bg_pixel = 0x00;
	private static int bg_palette = 0x00;
	private static int bitMux = 0;
	private static int p0Pixel = 0;
	private static int p1Pixel = 0;
	private static int bgPal0 = 0;
	private static int bgPal1 = 0;

	private static int pixelX = 0;
	private static int pixelY = 0;

	private static int fine_x = 0x00;
	private static int bg_next_tile_id = 0x00;
	private static int bg_next_tile_attrib = 0x00;
	private static int bg_next_tile_lsb = 0x00;
	private static int bg_next_tile_msb = 0x00;
	private static int bg_shifter_pattern_lo = 0x0000;
	private static int bg_shifter_pattern_hi = 0x0000;
	private static int bg_shifter_attrib_lo = 0x0000;
	private static int bg_shifter_attrib_hi = 0x0000;

	private static int nTileY = 0;
	private static int nTileX = 0;
	private static int noffSet = (nTileY * 256) + (nTileX * 16);
	private static int row = 0;
	private static int col = 0;
	private static int lsb = 0;
	private static int msb = 0;

	private static int fg_pixel = 0x00;
	public static int fg_palette = 0x00;
	public static int fg_priority = 0x00;

	public boolean nmi = false;

	private static sObjectAttributeEntry[] OAM = new sObjectAttributeEntry[64];
	private int oam_addr = 0x00;
	private static sObjectAttributeEntry[] sprite_scanline = new sObjectAttributeEntry[8];
	private int sprite_count;
	private int[] sprite_shifter_pattern_lo = new int[8];
	private int[] sprite_shifter_pattern_hi = new int[8];
	public boolean sprite_zero_hit_possible = false;
	public boolean sprite_zero_being_rendered = false;
	public static int pOAM = 0;

/* @formatter:off */	

	public static int getPalScreen(int i) {
		
		switch (i) {
	case 0 : palScreen = 0xFF545454; break;case 1 : palScreen = 0xFF001E74; break;
	case 2 : palScreen = 0xFF081090; break;case 3 : palScreen = 0xFF300088; break;
	case 4 : palScreen = 0xFF440064; break;case 5 : palScreen = 0xFF5C0030; break;
	case 6 : palScreen = 0xFF540400; break;case 7 : palScreen = 0xFF3C1800; break;
	case 8 : palScreen = 0xFF202A00; break;case 9 : palScreen = 0xFF083A00; break;
	case 10: palScreen = 0xFF004000; break;case 11: palScreen = 0xFF003C00; break;
	case 12: palScreen = 0xFF00323C; break;case 13: palScreen = 0xFF000000; break;
	case 14: palScreen = 0xFF000000; break;case 15: palScreen = 0xFF000000; break;	           
	
	case 16: palScreen = 0xFF989698; break;case 17: palScreen = 0xFF084CC4; break;
	case 18: palScreen = 0xFF3032EC; break;case 19: palScreen = 0xFF5C1EE4; break;
	case 20: palScreen = 0xFF8814B0; break;case 21: palScreen = 0xFFA01464; break;
	case 22: palScreen = 0xFF982220; break;case 23: palScreen = 0xFF783C00; break;
	case 24: palScreen = 0xFF545A00; break;case 25: palScreen = 0xFF287200; break;
	case 26: palScreen = 0xFF087C00; break;case 27: palScreen = 0xFF007628; break;
	case 28: palScreen = 0xFF006678; break;case 29: palScreen = 0xFF000000; break;
	case 30: palScreen = 0xFF000000; break;case 31: palScreen = 0xFF000000; break;
		                          
	case 32: palScreen = 0xFFECEEEC; break;case 33: palScreen = 0xFF4C9AEC; break;
	case 34: palScreen = 0xFF787CEC; break;case 35: palScreen = 0xFFB062EC; break;
	case 36: palScreen = 0xFFE454EC; break;case 37: palScreen = 0xFFEC58B4; break;
	case 38: palScreen = 0xFFEC6A64; break;case 39: palScreen = 0xFFD48820; break;
	case 40: palScreen = 0xFFA0AA00; break;case 41: palScreen = 0xFF74C400; break;
	case 42: palScreen = 0xFF4CD020; break;case 43: palScreen = 0xFF38CC6C; break;
	case 44: palScreen = 0xFF38B4CC; break;case 45: palScreen = 0xFF3C3C3C; break;
	case 46: palScreen = 0xFF000000; break;case 47: palScreen = 0xFF000000; break;
		                          
	case 48: palScreen = 0xFFECEEEC; break;case 49: palScreen = 0xFFA8CCEC; break;
	case 50: palScreen = 0xFFBCBCEC; break;case 51: palScreen = 0xFFD4B2EC; break;
	case 52: palScreen = 0xFFECAEEC; break;case 53: palScreen = 0xFFECAED4; break;
	case 54: palScreen = 0xFFECB4B0; break;case 55: palScreen = 0xFFE4C490; break;
	case 56: palScreen = 0xFFCCD278; break;case 57: palScreen = 0xFFB4DE78; break;
	case 58: palScreen = 0xFFA8E290; break;case 59: palScreen = 0xFF98E2B4; break;
	case 60: palScreen = 0xFFA0D6E4; break;case 61: palScreen = 0xFFA0A2A0; break;
	case 62: palScreen = 0xFF000000; break;case 63: palScreen = 0xFF000000; break;
		}
		return palScreen;		
	}
	
/* @formatter:on */
	public static void getPatternTable0(int i, int palette) {

		for (nTileY = 0; nTileY < 16; nTileY++) {
			for (nTileX = 0; nTileX < 16; nTileX++) {
				noffSet = (nTileY * 256) + (nTileX * 16);
				// System.out.println("nof: " + noffSet);
				for (row = 0; row < 8; row++) {
					lsb = i * 0x1000 + noffSet + row + 0x0000;
					msb = i * 0x1000 + noffSet + row + 0x0008;
					int tile_lsb = ppuRead(lsb, false) & 0xFFFF;
					int tile_msb = ppuRead(msb, false) & 0xFFFF;

					// System.out.println("lsb: " + tile_lsb);
					// System.out.println("msb: " + tile_msb);
					for (col = 0; col < 8; col++) {
						int pixel = ((tile_lsb & 0x01) << 1) | ((tile_msb & 0x01));

						// System.out.println("p1: " + pixel);
						tile_lsb >>= 1;
						tile_msb >>= 1;

						// System.out.println("p2: " + pixel);
//						palette = palette;
//						pixel = pixel;

						pixelX = nTileX * 8 + (7 - col);
						pixelY = nTileY * 8 + row;

						PatternTable0.image3.setRGB(pixelX, pixelY, GetColourFromPaletteRam(palette, pixel));

					}
				}
			}

		}
	}

	public static void getPatternTable1(int i, int palette) {

		for (nTileY = 0; nTileY < 16; nTileY++) {
			for (nTileX = 0; nTileX < 16; nTileX++) {
				noffSet = (nTileY * 256) + (nTileX * 16);
				// System.out.println("nof: " + noffSet);
				for (row = 0; row < 8; row++) {
					lsb = i * 0x1000 + noffSet + row + 0x0000;
					msb = i * 0x1000 + noffSet + row + 0x0008;
					int tile_lsb = ppuRead(lsb, false) & 0xFFFF;
					int tile_msb = ppuRead(msb, false) & 0xFFFF;

					// System.out.println("lsb: " + tile_lsb);
					// System.out.println("msb: " + tile_msb);
					for (col = 0; col < 8; col++) {
						int pixel = (tile_lsb & 0x01) + ((tile_msb & 0x01) << 1);

						// System.out.println("p1: " + pixel);
						tile_lsb >>= 1;
						tile_msb >>= 1;

						// System.out.println("p2: " + pixel);
//						palette = palette;
//						pixel = pixel;

						pixelX = nTileX * 8 + (7 - col);
						pixelY = nTileY * 8 + row;

						PatternTable0.image4.setRGB(pixelX, pixelY, GetColourFromPaletteRam(palette, pixel));
					}
				}
			}

		}
	}

	public static int GetColourFromPaletteRam(int palette, int pixel) {

		return getPalScreen(ppuRead(((0x3F00 + (palette << 2) + pixel)), false) & 0x3F);
	}

	/* @formatter:off */	
	public int cpuRead(int addr, boolean bReadOnly) {
		int data = 0x00;
		if (bReadOnly) 
		{
			switch (addr) 
			{
		case 0x0000: // Control
			data = control.getReg();
			break;
		case 0x0001: // Mask
			data = mask.getReg();
			break;
		case 0x0002: // Status
			data = status.getReg();
			break;
		case 0x0003: // OAM Address
			break;
		case 0x0004: // OAM Data
			break;
		case 0x0005: // Scroll
			break;
		case 0x0006: // PPU Address
			break;
		case 0x0007: // PPU Data
			break;
			}	
		}
		else
		{
			switch (addr) 
			{
		case 0x0000: // Control
			break;
		case 0x0001: // Mask
			break;
		case 0x0002: // Status
			data = (status.getReg() & 0xE0) | (ppu_data_buffer & 0x1F);
			status.verticalBlank = 0;
			address_latch = 0;
			break;
		case 0x0003: // OAM Address
		
			break;
		case 0x0004: // OAM Data
			data = getOamData(oam_addr);
			break;
		case 0x0005: // Scroll
			break;
		case 0x0006: // PPU Address
			break;
		case 0x0007: // PPU Data
			data =ppu_data_buffer;
			ppu_data_buffer = ppuRead(vram_addr.getReg(), false); 
			if (vram_addr.getReg() >= 0x3F00) {
				data =ppu_data_buffer;
			}
			if (control.incrementMode == 1) {
				vram_addr.coarse_y = (vram_addr.coarse_y + 1); // & 0x1F;
			} else {
				vram_addr.coarse_x = (vram_addr.coarse_x + 1); // & 0x1F;
			}	
			break;
			}
		}
		return data;		
	}
	
	
	public void cpuWrite(int addr, int data) {
		switch (addr) 
		{
		case 0x0000: // Control
			control.setReg(data);
			tram_addr.nametable_x = control.nametableX;
			tram_addr.nametable_y = control.nametableY;
			break;
		case 0x0001: // Mask
			mask.setReg(data);

			break;
		case 0x0002: // Status
			break;
		case 0x0003: // OAM Address
			oam_addr = data;
			break;
		case 0x0004: // OAM Data
			setOamData(oam_addr, data);
			break;
		case 0x0005: // Scroll
			if (address_latch == 0) {
				fine_x = data & 0x07;
				tram_addr.coarse_x = data >> 3;
				address_latch = 1;
			} else {
				tram_addr.fine_y = data & 0x07;
				tram_addr.coarse_y = data >> 3;
				address_latch = 0;
			}
			break;
		case 0x0006: // PPU Address
			if(address_latch == 0) {
				tram_addr.setReg((((data) & 0x3F) << 8) | (tram_addr.getReg() & 0x00FF));
				address_latch = 1;
			}else {
				tram_addr.setReg(((tram_addr.getReg() & 0xFF00) | data));
				vram_addr.setReg(tram_addr.getReg());
				address_latch = 0;
			}
			break;
		case 0x0007: // PPU Data
			ppuWrite(vram_addr.getReg(), data);
			if (control.incrementMode > 0) {
				vram_addr.coarse_y = (vram_addr.coarse_y + 1); // & 0x1F;
			} else {
				vram_addr.coarse_x = (vram_addr.coarse_x + 1); // & 0x1F;
			}
			break;
		}
	}
	
	public static int ppuRead(int addr, boolean bReadOnly) {
		int data = 0x00;
		addr &= 0x3FFF;

		if (Cartridge.ppuRead(addr, data)){	} 
		else if (addr >= 0x0000 && addr <= 0x1FFF){
			data = tblPattern[(addr &0x1000) >> 12][addr &0x0FFF];
			Cartridge.cart_data = data;
		} 
		else if (addr >= 0x2000 && addr <= 0x3EFF) 
		{
			addr &= 0x0FFF;

			if (Cartridge.mirror == 1) { // VERTICAL

				if (addr >= 0x0000 && addr <= 0x03FF)
					data =(tblName[0][addr & 0x03FF]);
				if (addr >= 0x0400 && addr <= 0x07FF)
					data =(tblName[1][addr & 0x03FF]);
				if (addr >= 0x0800 && addr <= 0x0BFF)
					data =(tblName[0][addr & 0x03FF]);
				if (addr >= 0x0C00 && addr <= 0x0FFF)
					data =(tblName[1][addr & 0x03FF]);

			} else if (Cartridge.mirror == 0) { // HORIZONTAL

				if (addr >= 0x0000 && addr <= 0x03FF)
					data =(tblName[0][addr & 0x03FF]);
				if (addr >= 0x0400 && addr <= 0x07FF)
					data =(tblName[0][addr & 0x03FF]);
				if (addr >= 0x0800 && addr <= 0x0BFF)
					data =(tblName[1][addr & 0x03FF]);
				if (addr >= 0x0C00 && addr <= 0x0FFF)
					data =(tblName[1][addr & 0x03FF]);

			}
			Cartridge.cart_data = data;


		} 
		else if (addr >= 0x3F00 && addr <= 0x3FFF) 
		{
			addr &= 0x001F;
			if (addr == 0x0010) {addr = (0x0000);}
			if (addr == 0x0014) {addr = (0x0004);}
			if (addr == 0x0018) {addr = (0x0008);}
			if (addr == 0x001C) {addr = (0x000C);}

			data =(tblPalette[addr]& (mask.grayscale > 0 ? 0x30 : 0x3F));

		Cartridge.cart_data = data;

		}
		
		return 	Cartridge.cart_data ;

	}

	public static void ppuWrite(int addr, int data) {
		
		addr &= 0x3FFF;
		if (Cartridge.ppuWrite(addr, data) == true) 
		{
			
		} 
		else if (addr >= 0x0000 && addr <= 0x1FFF) 
		{
			tblPattern[(addr & 0x1000) >> 12][addr & 0x0FFF] = data;

		} 
		else if (addr >= 0x2000 && addr <= 0x3EFF) 
		{
			addr &= 0x0FFF;

			if (Cartridge.mirror == 1) {
				if (addr >= 0x0000 && addr <= 0x03FF)
					tblName[0][addr & 0x03FF] = data;
				if (addr >= 0x0400 && addr <= 0x07FF)
					tblName[1][addr & 0x03FF] = data;
				if (addr >= 0x0800 && addr <= 0x0BFF)
					tblName[0][addr & 0x03FF] = data;
				if (addr >= 0x0C00 && addr <= 0x0FFF)
					tblName[1][addr & 0x03FF] = data;
			} else if (Cartridge.mirror == 0) {
				if (addr >= 0x0000 && addr <= 0x03FF)
					tblName[0][addr & 0x03FF] = data;
				if (addr >= 0x0400 && addr <= 0x07FF)
					tblName[0][addr & 0x03FF] = data;
				if (addr >= 0x0800 && addr <= 0x0BFF)
					tblName[1][addr & 0x03FF] = data;
				if (addr >= 0x0C00 && addr <= 0x0FFF)
					tblName[1][addr & 0x03FF] = data;
			}
		} 
		else if (addr >= 0x3F00 && addr <= 0x3FFF) 
		{
			addr &= 0x001F;
			if (addr == 0x0010) {addr = 0x0000;}
			if (addr == 0x0014) {addr = 0x0004;}
			if (addr == 0x0018) {addr = 0x0008;}
			if (addr == 0x001C) {addr = 0x000C;}
			tblPalette[addr] = data;
		}

	}
	
	public void clock() {
		
		if (scanline >= -1 && scanline < 240) {

			if (scanline == 0 && cycle == 0){cycle = 1;
			}
			if (scanline == -1 && cycle == 1)
			{
				status.verticalBlank = 0;
				status.spriteOverflow = 0;
				status.spriteZeroHit = 0;
				
				for(int i = 0; i < 8; i++) {
					sprite_shifter_pattern_lo[i] = 0;
					sprite_shifter_pattern_hi[i] = 0;
				}
			}
			
			if ((cycle >= 2 && cycle < 258) || (cycle >= 321 && cycle < 338)) 
			{
				updateShifters();

				switch ((cycle - 1) % 8) 
				{
				case 0:
					loadBackgroundShifters();
					bg_next_tile_id = ppuRead(0x2000 | (vram_addr.getReg() & 0x0FFF), false);
					break;
				case 2:
					bg_next_tile_attrib = ppuRead(0x23C0 | (vram_addr.nametable_y << 11) 
														 | (vram_addr.nametable_x << 10)
														 | ((vram_addr.coarse_y >> 2) << 3) 
														 | (vram_addr.coarse_x >> 2), false);
					if ((vram_addr.coarse_y & 0x02) > 0) 
					{
						bg_next_tile_attrib >>= 4;
					}
					if ((vram_addr.coarse_x & 0x02) > 0) {
						
						bg_next_tile_attrib >>= 2;
						bg_next_tile_attrib &= 0x03;
						break;
					}
				case 4:
					bg_next_tile_lsb = ppuRead((control.patternBackground << 12) 
							+ ((bg_next_tile_id << 4)) 
							+ vram_addr.fine_y + 0, false);
					break;
				case 6:
					bg_next_tile_msb = ppuRead((control.patternBackground << 12) 
							+ ((bg_next_tile_id << 4)) 
							+ vram_addr.fine_y + 8, false);
					break;
				case 7:
					incrementScrollX();
					break;
				}
			}

			if (cycle == 256) 
			{
				incrementScrollY();
			}
			
			if (cycle == 257) 
			{
				loadBackgroundShifters();
				transferAddressX();
			}

			if (cycle == 338 || cycle == 340)
			{
				bg_next_tile_id = ppuRead(0x2000 | (vram_addr.getReg() & 0x0FFF), false);
			}
			
			if (scanline == -1 && cycle >= 280 && cycle < 305)
			{
				transferAddressY();
			}
			
			if(cycle == 257 && scanline >= 0) {
				clearSpriteScanline();
				
				sprite_count = 0;
				
				for(int i = 0; i < 8; i++) {
					sprite_shifter_pattern_lo[i] = 0;
					sprite_shifter_pattern_hi[i] = 0;
				}
				
				int oam_entry = 0;
				sprite_zero_hit_possible = false;
				
				while(oam_entry < 64 && sprite_count < 9) 
				{
					int diff =  (scanline - OAM[oam_entry].y);// & 0xFFFF;
					
					int sp01 = (control.spriteSize > 0 ? 16 : 8);
					if(diff >= 0 && diff < sp01) {

						if(sprite_count < 8) {
							if(oam_entry == 0) {
								sprite_zero_hit_possible = true;
							}
							
							sprite_scanline[sprite_count].x = OAM[oam_entry].x;
							sprite_scanline[sprite_count].y = OAM[oam_entry].y;
							sprite_scanline[sprite_count].attribute = OAM[oam_entry].attribute;
							sprite_scanline[sprite_count].id = OAM[oam_entry].id;
							sprite_count++;
						}
					}
					
					oam_entry = (oam_entry+1) &0xFF;
				}
				
					status.spriteOverflow = (sprite_count > 8)? 1: 0; 
				
			}
			
			if(cycle == 340) {
				for(int i = 0; i < sprite_count; i++) {
					int sprite_pattern_bits_lo, sprite_pattern_bits_hi;
					int sprite_pattern_addr_lo, sprite_pattern_addr_hi;
					
					if(control.spriteSize == 0) {
						if((sprite_scanline[i].attribute & 0x80) == 0) {
							sprite_pattern_addr_lo = (
									(control.patternSprite << 12)
									| (sprite_scanline[i].id << 4) 
									| (scanline - sprite_scanline[i].y) &0xFF) & 0xFFFF;
							System.out.println("1 " + (sprite_scanline[i].id << 4) + " 2: " + (sprite_scanline[i].id));
						} else {
							sprite_pattern_addr_lo = 
									((control.patternSprite << 12)
									| (sprite_scanline[i].id << 4)
									| (7 - (scanline - sprite_scanline[i].y))&0xFF) & 0xFFFF;
						}
					} else {
						if((sprite_scanline[i].attribute & 0x80) == 0) {
							if(((scanline - sprite_scanline[i].y)&0xFF )< 8) {
								sprite_pattern_addr_lo = 
										(((sprite_scanline[i].id & 0x01)      << 12)  // Which Pattern Table? 0KB or 4KB offset
										| ((sprite_scanline[i].id & 0xFE)      << 4 )  // Which Cell? Tile ID * 16 (16 bytes per tile)
										| ((scanline - sprite_scanline[i].y) & 0x07 )) & 0xFFFF;
							} else {
								sprite_pattern_addr_lo =
										(((sprite_scanline[i].id & 0x01)      << 12)  // Which Pattern Table? 0KB or 4KB offset
										| (((sprite_scanline[i].id & 0xFE) + 1) << 4 )  // Which Cell? Tile ID * 16 (16 bytes per tile)
										| ((scanline - sprite_scanline[i].y) & 0x07 ) & 0xFFFF);
							}
						} else {
							if(((scanline - sprite_scanline[i].y)&0xFF)< 8 ) {
								sprite_pattern_addr_lo = 
										(((sprite_scanline[i].id & 0x01)      << 12)    // Which Pattern Table? 0KB or 4KB offset
										| (((sprite_scanline[i].id & 0xFE) + 1) << 4 )    // Which Cell? Tile ID * 16 (16 bytes per tile)
										| (7 - (scanline - sprite_scanline[i].y) & 0x07)) & 0xFFFF;
							} else {
								sprite_pattern_addr_lo =
										(((sprite_scanline[i].id & 0x01)       << 12)    // Which Pattern Table? 0KB or 4KB offset
										| ((sprite_scanline[i].id & 0xFE)       << 4 )    // Which Cell? Tile ID * 16 (16 bytes per tile)
										| (7 - (scanline - sprite_scanline[i].y) & 0x07)) & 0xFFFF;
							}
						}
					}
					
					sprite_pattern_addr_hi = (sprite_pattern_addr_lo + 8) &0xFFFF;
					
					sprite_pattern_bits_lo = ppuRead(sprite_pattern_addr_lo, false);
					sprite_pattern_bits_hi = ppuRead(sprite_pattern_addr_hi, false);
					
					if((sprite_scanline[i].attribute & 0x40) > 0) {
						sprite_pattern_bits_lo = flipByte(sprite_pattern_bits_lo);
						sprite_pattern_bits_hi = flipByte(sprite_pattern_bits_hi);
					}
					
					sprite_shifter_pattern_lo[i] = sprite_pattern_bits_lo;
					sprite_shifter_pattern_hi[i] = sprite_pattern_bits_hi;
				}
			}
		}
		

		if (scanline == 240) 
		{
			// Do nothing
		}

		if (scanline >= 241 && scanline < 261) 
		{
			if (scanline == 241 && cycle == 1) 
			{
				status.verticalBlank = 1;

				if (control.enableNmi > 0) 
				{
					nmi = true;
				}
			}
		}

		bg_pixel = 0x00;
		bg_palette = 0x00;

		
		if (mask.render_background > 0) {
			bitMux = 0x8000 >> fine_x;
							
			p0Pixel = (bg_shifter_pattern_lo & bitMux) > 0 ? 1 : 0;
			p1Pixel = (bg_shifter_pattern_hi & bitMux) > 0 ? 1 : 0;

			bg_pixel = (p1Pixel << 1) | p0Pixel;

			bgPal0 = (bg_shifter_attrib_lo & bitMux) > 0 ? 1 : 0;
			bgPal1 = (bg_shifter_attrib_hi & bitMux) > 0 ? 1 : 0;

			bg_palette = (bgPal1 << 1) | bgPal0;
			
		} 
		
		fg_pixel = 0x00;
		fg_palette = 0x00;
		fg_priority = 0x00;

		
		if(mask.render_sprites > 0) 
		{
			sprite_zero_being_rendered = false;
			
			for (int i = 0; i < sprite_count; i++) 
			{
				if(sprite_scanline[i].x == 0) 
				{
					int fg_pixel_lo = (sprite_shifter_pattern_lo[i] &0x80) > 0? 1: 0;
					int fg_pixel_hi = (sprite_shifter_pattern_hi[i] &0x80) > 0? 1: 0;
					fg_pixel = (fg_pixel_hi << 1) | fg_pixel_lo;
					
					fg_palette = (sprite_scanline[i].attribute & 0x03) + 0x04;
					fg_priority = (sprite_scanline[i].attribute & 0x20) == 0? 1 : 0;
					
					if(fg_pixel !=0) 
					{
						if(sprite_zero_hit_possible && i == 0) 
						{
							sprite_zero_being_rendered = true;					
						}
						break;
					}
				}
			}
		}
				
		int final_pixel = 0x00;
		int final_palette = 0x00;
		
		if(bg_pixel == 0 && fg_pixel == 0) 
		{
			final_pixel = 0x00;
			final_palette = 0x00;
		}
		else if (bg_pixel == 0 && fg_pixel >0) 
		{
			final_pixel = fg_pixel;
			final_palette = fg_palette;
		}
		else if (bg_pixel > 0 && fg_pixel ==0) 
		{
			final_pixel = bg_pixel;
			final_palette = bg_palette;
		}
		else if (bg_pixel > 0 && fg_pixel > 0) 
		{
			if (fg_priority >0) 
			{
				final_pixel = fg_pixel;
				final_palette = fg_palette;
			}
			else 
			{
				final_pixel = bg_pixel;
				final_palette = bg_palette;
			}
			
			if(sprite_zero_hit_possible && sprite_zero_being_rendered ) 
			{
				if ((mask.render_background & mask.render_sprites)>0) 
				{
					if((~(mask.renderBackgroundLeft | mask.renderSpritesLeft)) !=0) 
					{
						if(cycle >= 9 && cycle < 258) 
						{
							status.spriteZeroHit = 1;
						}
					}
					else 
					{
						if(cycle >=1 && cycle < 258) 
						{
							status.spriteZeroHit = 1;
						}
					}
				}
			}
		}
		
		

		Main.image2.setRGB(cycle, scanDisplay, GetColourFromPaletteRam(final_palette, final_pixel));

		cycle = cycle +1;
		if (cycle >= 341) {
			cycle = 0;
			scanline = scanline + 1;
			scanDisplay = scanline;
			if (scanline >= 261) {
				scanline = -1;
				scanDisplay = 1 + scanline;
				frame_complete = true;
			}
		}		
	}
	


	private void incrementScrollX() {
		if ((mask.render_background > 0) || (mask.render_sprites > 0)) 
		{
			if (vram_addr.coarse_x == 31) 
			{
				vram_addr.coarse_x = 0;
				vram_addr.nametable_x = (~vram_addr.nametable_x)&0x01;

			} 
			else 
			{
				vram_addr.coarse_x = (vram_addr.coarse_x + 1); // & 0x1F;
			}
		}
	}

	private void incrementScrollY() {
		if ((mask.render_background > 0) || (mask.render_sprites > 0)) 
		{
			if (vram_addr.fine_y < 7) 
			{
				vram_addr.fine_y = (vram_addr.fine_y + 1);// & 0x7;

			} 
			else 
			{
				vram_addr.fine_y = 0;

				if (vram_addr.coarse_y == 29) 
				{
					vram_addr.coarse_y = 0;
					vram_addr.nametable_y = (~vram_addr.nametable_y)&0x01;
				} 
				else if (vram_addr.coarse_y == 31) 
				{
					vram_addr.coarse_y = 0;
				} 
				else 
				{
					vram_addr.coarse_y = (vram_addr.coarse_y + 1);
				}
			}
		}
	}

	private void transferAddressX() {
		if ((mask.render_background > 0) || (mask.render_sprites > 0)) 
		{
			vram_addr.nametable_x = tram_addr.nametable_x;
			vram_addr.coarse_x = tram_addr.coarse_x;
		}
	}

	private void transferAddressY() {
		if ((mask.render_background > 0) || (mask.render_sprites > 0)) 
		{
			vram_addr.fine_y = tram_addr.fine_y;
			vram_addr.nametable_y = tram_addr.nametable_y;
			vram_addr.coarse_y = tram_addr.coarse_y;
		}
	}

	private void loadBackgroundShifters() {
		bg_shifter_pattern_lo = (bg_shifter_pattern_lo & 0xFF00) | bg_next_tile_lsb;
		bg_shifter_pattern_hi = (bg_shifter_pattern_hi & 0xFF00) | bg_next_tile_msb;

		bg_shifter_attrib_lo = (bg_shifter_attrib_lo & 0xFF00) | ((bg_next_tile_attrib & 0b01) > 0 ? 0xFF : 0x00);
		bg_shifter_attrib_hi = (bg_shifter_attrib_hi & 0xFF00) | ((bg_next_tile_attrib & 0b10) > 0 ? 0xFF : 0x00);
	}

	private void updateShifters() {
		if (mask.render_background > 0) {
			bg_shifter_pattern_lo = (bg_shifter_pattern_lo << 1);
			bg_shifter_pattern_hi = (bg_shifter_pattern_hi << 1);

			bg_shifter_attrib_lo = (bg_shifter_attrib_lo << 1); 
			bg_shifter_attrib_hi = (bg_shifter_attrib_hi << 1);
		}
		if (mask.render_sprites > 0 && cycle >= 1 && cycle < 258) {
			for (int i = 0; i < sprite_count; i++) {
				if(sprite_scanline[i].x > 0) {
					sprite_scanline[i].x = (sprite_scanline[i].x -1);
				} else {
					sprite_shifter_pattern_lo[i] = (sprite_shifter_pattern_lo[i] << 1);
					sprite_shifter_pattern_hi[i] = (sprite_shifter_pattern_hi[i] << 1);
				}
			}
		}
	}
	
/* @formatter:on */

	public static void reset() {
		fine_x = 0x00;
		address_latch = 0x00;
		ppu_data_buffer = 0x00;
		scanline = 0;
		cycle = 0;
		bg_next_tile_id = 0x00;
		bg_next_tile_attrib = 0x00;
		bg_next_tile_lsb = 0x00;
		bg_next_tile_msb = 0x00;
		bg_shifter_pattern_lo = 0x0000;
		bg_shifter_pattern_hi = 0x0000;
		bg_shifter_attrib_lo = 0x0000;
		bg_shifter_attrib_hi = 0x0000;
		status.setReg(0x00);
		mask.setReg(0x00);
		control.setReg(0x00);
		vram_addr.setReg(0x0000);
		tram_addr.setReg(0x0000);

		for (int s = 0; s < 8; s++) {
			sprite_scanline[s] = new sObjectAttributeEntry();
		}

		for (int o = 0; o < 64; o++) {
			OAM[o] = new sObjectAttributeEntry();
		}

	}

	private void clearSpriteScanline() {
		for (sObjectAttributeEntry entry : sprite_scanline) {
			entry.attribute = 0xFF;
			entry.id = 0xFF;
			entry.x = 0xFF;
			entry.y = 0xFF;
		}
	}

	private int flipByte(int b) {
		b = (b & 0xF0) >> 4 | (b & 0x0F) << 4;
		b = (b & 0xCC) >> 2 | (b & 0x33) << 2;
		b = (b & 0xAA) >> 1 | (b & 0x55) << 1;
		return b;
	}

	public static void setOamData(int addr, int data) {
		int index = (addr / 4) &0xFFFF ;

		switch (addr % 4) {
		case 0:
			OAM[index].y = data;
			break;
		case 1:
			OAM[index].id = data;
			break;
		case 2:
			OAM[index].attribute = data;
			break;
		case 3:
			OAM[index].x = data;
			break;
		default:
		}
	}

	public int getOamData(int addr) {
		int index = (addr / 4) &0xFFFF ;
		int data = 0;

		switch (addr % 4) {
		case 0:
			data = OAM[index].y;
			break;
		case 1:
			data = OAM[index].id;
			break;
		case 2:
			data = OAM[index].attribute;
			break;
		case 3:
			data = OAM[index].x;
			break;
		default:
		}
		return data;
	}

}
