package com.atomizer.nes;

import com.atomizer.Main;

public class olc6502 {

	// 6502 registers
	public static int a = 0x00; // Accumulator Register
	public static int x = 0x00; // X Register
	public static int y = 0x00; // Y Register
	public static int stkp = 0x00; // Stack Pointer (points to location on bus)
	public static int pc = 0x0000; // Program Counter
	public static int status; // Status Register

	static boolean breaking = false;

	// FLAGS
	public static int C = 1 << 0; // Carry Bit
	public static int Z = 1 << 1; // Zero
	public static int I = 1 << 2; // Disable Interrupts
	public static int D = 1 << 3; // Decimal Mode (unused in this implementation)
	public static int B = 1 << 4; // Break
	public static int U = 1 << 5; // Unused
	public static int V = 1 << 6; // Overflow
	public static int N = 1 << 7; // Negative

	public static int addCycles1;
	public static int addCycles2;

	public static void setFlag(int f, boolean v) {
		if (v) {
			status |= f;

		} else {
			status &= ~f;
		}
	}

	public static int getFlag(int f) {
		return ((status & f) > 0) ? 1 : 0;
	}

	// Internal helpers
	public static int fetched = 0x00; // Represents the working input value to the ALU
	public static int temp = 0x0000; // A convenience variable used everywhere
	public static int addr_abs = 0x0000; // All used memory addresses end up in here
	public static int addr_rel = 0x00; // Represents absolute address following a branch
	public static int opcode = 0x00; // Is the instruction byte
	public static int cycles = 0; // Counts how many cycles the instruction has remaining
	public static int clock_count = 0; // A global accumulation of the number of clocks

	boolean complete = false;
	public static boolean access = false;

	public static int complete() {
		return getCycles();
	}

	public synchronized static void reset() {

		addr_abs = 0xFFFC;
		int lo = read(addr_abs + 0) & 0xFF;
		int hi = read(addr_abs + 1) & 0xFF;
		pc = (hi << 8) | lo;
		//pc = 0xc000;
		a = 0x00;
		x = 0x00;
		y = 0x00;
		stkp = 0xFD;
		status = 0x00 | U;
		addr_rel = 0x0000;
		addr_abs = 0x0000;
		fetched = 0x00;
		setCycles(7);

	}

	public static void debug() {
		System.out.println("a: " + a);
		System.out.println("x: " + x);
		System.out.println("y: " + y);
		System.out.println("PC: " + pc);
		System.out.println("stkp: " + String.format(Main.format2, stkp));
		System.out.println("status: " + String.format(Main.format, status));
		System.out.println("addr_rel: " + String.format(Main.format2, addr_rel));
		System.out.println("addr_abs: " + String.format(Main.format2, addr_abs));
		System.out.println("fetched: " + String.format(Main.format2, fetched));
		System.out.println("cycles: " + getCycles());

	}

	public synchronized static void irq() {
		if (getFlag(I) == 0) {
			write(0x0100 + stkp, ((pc >> 8) & 0x00FF));
			stkp = (stkp - 1) & 0xFF;
			write(0x0100 + stkp, (pc & 0x00FF));
			stkp = (stkp - 1) & 0xFF;

			setFlag(B, false);
			setFlag(U, true);
			setFlag(I, true);

			write(0x0100 + stkp, status);
			stkp = (stkp - 1) & 0xFF;
			addr_abs = 0xFFFE;
			int lo = read(addr_abs + 0);
			int hi = read(addr_abs + 1);
			pc = (hi << 8) | lo;
			setCycles(7);
		}

	}

	public synchronized static void nmi() {
		write(0x0100 + stkp, ((pc >> 8) & 0x00FF));
		stkp = (stkp - 1) & 0xFF;
		write(0x0100 + stkp, (pc & 0x00FF));
		stkp = (stkp - 1) & 0xFF;

		setFlag(B, false);
		setFlag(U, true);
		setFlag(I, true);
		write(0x0100 + stkp, status);
		stkp = (stkp - 1) & 0xFF;

		addr_abs = 0xFFFA;
		int lo = read(addr_abs + 0);
		int hi = read(addr_abs + 1);
		pc = (hi << 8) | lo;

		setCycles(8);

	}

	public synchronized static void clock() {
		if (cycles == 0) {
			opcode = read(pc) & 0xFF;
			setFlag(U, true);
			pc = (pc + 1) & 0xFFFF;
			cycles = lookupCycles(opcode);
		drawCodeOut();
			lookupAddr(opcode);
			int additional_cycle1 = getAddCycles();
			lookupOper(opcode);
			int additional_cycle2 = getAddCycles();

			cycles = (cycles + (additional_cycle1 & additional_cycle2));

			setFlag(U, true);
		}
		clock_count = clock_count + 1;
		cycles = cycles - 1;

	}

	public static int read(int addr) {
		return Bus.cpuRead(addr, false);
	}

	public static void write(int addr, int data) {

		Bus.cpuWrite(addr & 0xFFFF, data & 0xFF);
	}
/* @formatter:off */
	public synchronized static int lookupOper(int op) {
		switch (op) {
		case 0x00:BRK();break;case 0x01:ORA();break;case 0x02:XXX();break;case 0x03:XXX();break;case 0x04:NOP();break;
		case 0x05:ORA();break;case 0x06:ASL();break;case 0x07:XXX();break;case 0x08:PHP();break;case 0x09:ORA();break;
		case 0x0A:ASL();break;case 0x0B:XXX();break;case 0x0C:NOP();break;case 0x0D:ORA();break;case 0x0E:ASL();break;
		case 0x0F:XXX();break;case 0x10:BPL();break;case 0x11:ORA();break;case 0x12:XXX();break;case 0x13:XXX();break;
		case 0x14:NOP();break;case 0x15:ORA();break;case 0x16:ASL();break;case 0x17:XXX();break;case 0x18:CLC();break;
		case 0x19:ORA();break;case 0x1A:NOP();break;case 0x1B:XXX();break;case 0x1C:NOP();break;case 0x1D:ORA();break;
		case 0x1E:ASL();break;case 0x1F:XXX();break;case 0x20:JSR();break;case 0x21:AND();break;case 0x22:XXX();break;
		case 0x23:XXX();break;case 0x24:BIT();break;case 0x25:AND();break;case 0x26:ROL();break;case 0x27:XXX();break;
		case 0x28:PLP();break;case 0x29:AND();break;case 0x2A:ROL();break;case 0x2B:XXX();break;case 0x2C:BIT();break;
		case 0x2D:AND();break;case 0x2E:ROL();break;case 0x2F:XXX();break;case 0x30:BMI();break;case 0x31:AND();break;
		case 0x32:XXX();break;case 0x33:XXX();break;case 0x34:NOP();break;case 0x35:AND();break;case 0x36:ROL();break;
		case 0x37:XXX();break;case 0x38:SEC();break;case 0x39:AND();break;case 0x3A:NOP();break;case 0x3B:XXX();break;
		case 0x3C:NOP();break;case 0x3D:AND();break;case 0x3E:ROL();break;case 0x3F:XXX();break;case 0x40:RTI();break;
		case 0x41:EOR();break;case 0x42:XXX();break;case 0x43:XXX();break;case 0x44:NOP();break;case 0x45:EOR();break;
		case 0x46:LSR();break;case 0x47:XXX();break;case 0x48:PHA();break;case 0x49:EOR();break;case 0x4A:LSR();break;
		case 0x4B:XXX();break;case 0x4C:JMP();break;case 0x4D:EOR();break;case 0x4E:LSR();break;case 0x4F:XXX();break;
		case 0x50:BVC();break;case 0x51:EOR();break;case 0x52:XXX();break;case 0x53:XXX();break;case 0x54:NOP();break;
		case 0x55:EOR();break;case 0x56:LSR();break;case 0x57:XXX();break;case 0x58:CLI();break;case 0x59:EOR();break;
		case 0x5A:NOP();break;case 0x5B:XXX();break;case 0x5C:NOP();break;case 0x5D:EOR();break;case 0x5E:LSR();break;
		case 0x5F:XXX();break;case 0x60:RTS();break;case 0x61:ADC();break;case 0x62:XXX();break;case 0x63:XXX();break;
		case 0x64:NOP();break;case 0x65:ADC();break;case 0x66:ROR();break;case 0x67:XXX();break;case 0x68:PLA();break;
		case 0x69:ADC();break;case 0x6A:ROR();break;case 0x6B:XXX();break;case 0x6C:JMP();break;case 0x6D:ADC();break;
		case 0x6E:ROR();break;case 0x6F:XXX();break;case 0x70:BVS();break;case 0x71:ADC();break;case 0x72:XXX();break;
		case 0x73:XXX();break;case 0x74:NOP();break;case 0x75:ADC();break;case 0x76:ROR();break;case 0x77:XXX();break;
		case 0x78:SEI();break;case 0x79:ADC();break;case 0x7A:NOP();break;case 0x7B:XXX();break;case 0x7C:NOP();break;
		case 0x7D:ADC();break;case 0x7E:ROR();break;case 0x7F:XXX();break;case 0x80:NOP();break;case 0x81:STA();break;
		case 0x82:NOP();break;case 0x83:XXX();break;case 0x84:STY();break;case 0x85:STA();break;case 0x86:STX();break;
		case 0x87:XXX();break;case 0x88:DEY();break;case 0x89:NOP();break;case 0x8A:TXA();break;case 0x8B:XXX();break;
		case 0x8C:STY();break;case 0x8D:STA();break;case 0x8E:STX();break;case 0x8F:XXX();break;case 0x90:BCC();break;
		case 0x91:STA();break;case 0x92:XXX();break;case 0x93:XXX();break;case 0x94:STY();break;case 0x95:STA();break;
		case 0x96:STX();break;case 0x97:XXX();break;case 0x98:TYA();break;case 0x99:STA();break;case 0x9A:TXS();break;
		case 0x9B:XXX();break;case 0x9C:NOP();break;case 0x9D:STA();break;case 0x9E:XXX();break;case 0x9F:XXX();break;
		case 0xA0:LDY();break;case 0xA1:LDA();break;case 0xA2:LDX();break;case 0xA3:XXX();break;case 0xA4:LDY();break;
		case 0xA5:LDA();break;case 0xA6:LDX();break;case 0xA7:XXX();break;case 0xA8:TAY();break;case 0xA9:LDA();break;
		case 0xAA:TAX();break;case 0xAB:XXX();break;case 0xAC:LDY();break;case 0xAD:LDA();break;case 0xAE:LDX();break;
		case 0xAF:XXX();break;case 0xB0:BCS();break;case 0xB1:LDA();break;case 0xB2:XXX();break;case 0xB3:XXX();break;
		case 0xB4:LDY();break;case 0xB5:LDA();break;case 0xB6:LDX();break;case 0xB7:XXX();break;case 0xB8:CLV();break;
		case 0xB9:LDA();break;case 0xBA:TSX();break;case 0xBB:XXX();break;case 0xBC:LDY();break;case 0xBD:LDA();break;
		case 0xBE:LDX();break;case 0xBF:XXX();break;case 0xC0:CPY();break;case 0xC1:CMP();break;case 0xC2:NOP();break;
		case 0xC3:XXX();break;case 0xC4:CPY();break;case 0xC5:CMP();break;case 0xC6:DEC();break;case 0xC7:XXX();break;
		case 0xC8:INY();break;case 0xC9:CMP();break;case 0xCA:DEX();break;case 0xCB:XXX();break;case 0xCC:CPY();break;
		case 0xCD:CMP();break;case 0xCE:DEC();break;case 0xCF:XXX();break;case 0xD0:BNE();break;case 0xD1:CMP();break;
		case 0xD2:XXX();break;case 0xD3:XXX();break;case 0xD4:NOP();break;case 0xD5:CMP();break;case 0xD6:DEC();break;
		case 0xD7:XXX();break;case 0xD8:CLD();break;case 0xD9:CMP();break;case 0xDA:NOP();break;case 0xDB:XXX();break;
		case 0xDC:NOP();break;case 0xDD:CMP();break;case 0xDE:DEC();break;case 0xDF:XXX();break;case 0xE0:CPX();break;
		case 0xE1:SBC();break;case 0xE2:NOP();break;case 0xE3:XXX();break;case 0xE4:CPX();break;case 0xE5:SBC();break;
		case 0xE6:INC();break;case 0xE7:XXX();break;case 0xE8:INX();break;case 0xE9:SBC();break;case 0xEA:NOP();break;
		case 0xEB:SBC();break;case 0xEC:CPX();break;case 0xED:SBC();break;case 0xEE:INC();break;case 0xEF:XXX();break;
		case 0xF0:BEQ();break;case 0xF1:SBC();break;case 0xF2:XXX();break;case 0xF3:XXX();break;case 0xF4:NOP();break;
		case 0xF5:SBC();break;case 0xF6:INC();break;case 0xF7:XXX();break;case 0xF8:SED();break;case 0xF9:SBC();break;
		case 0xFA:NOP();break;case 0xFB:XXX();break;case 0xFC:NOP();break;case 0xFD:SBC();break;case 0xFE:INC();break;
		case 0xFF:XXX();break;default:System.out.printf("Oper undefined opcode: %02X\n", pc & 0xFF);break;}return op;}

	public synchronized static int lookupAddr(int op) {
		switch (op) {
		case 0x00:IMM();break;case 0x01:IZX();break;case 0x02:IMP();break;case 0x03:IMP();break;case 0x04:IMP();break;
		case 0x05:ZP0();break;case 0x06:ZP0();break;case 0x07:IMP();break;case 0x08:IMP();break;case 0x09:IMM();break;
		case 0x0A:IMP();break;case 0x0B:IMP();break;case 0x0C:IMP();break;case 0x0D:ABS();break;case 0x0E:ABS();break;
		case 0x0F:IMP();break;case 0x10:REL();break;case 0x11:IZY();break;case 0x12:IMP();break;case 0x13:IMP();break;
		case 0x14:IMP();break;case 0x15:ZPX();break;case 0x16:ZPX();break;case 0x17:IMP();break;case 0x18:IMP();break;
		case 0x19:ABY();break;case 0x1A:IMP();break;case 0x1B:IMP();break;case 0x1C:IMP();break;case 0x1D:ABX();break;
		case 0x1E:ABX();break;case 0x1F:IMP();break;case 0x20:ABS();break;case 0x21:IZX();break;case 0x22:IMP();break;
		case 0x23:IMP();break;case 0x24:ZP0();break;case 0x25:ZP0();break;case 0x26:ZP0();break;case 0x27:IMP();break;
		case 0x28:IMP();break;case 0x29:IMM();break;case 0x2A:IMP();break;case 0x2B:IMP();break;case 0x2C:ABS();break;
		case 0x2D:ABS();break;case 0x2E:ABS();break;case 0x2F:IMP();break;case 0x30:REL();break;case 0x31:IZY();break;
		case 0x32:IMP();break;case 0x33:IMP();break;case 0x34:IMP();break;case 0x35:ZPX();break;case 0x36:ZPX();break;
		case 0x37:IMP();break;case 0x38:IMP();break;case 0x39:ABY();break;case 0x3A:IMP();break;case 0x3B:IMP();break;
		case 0x3C:IMP();break;case 0x3D:ABX();break;case 0x3E:ABX();break;case 0x3F:IMP();break;case 0x40:IMP();break;
		case 0x41:IZX();break;case 0x42:IMP();break;case 0x43:IMP();break;case 0x44:IMP();break;case 0x45:ZP0();break;
		case 0x46:ZP0();break;case 0x47:IMP();break;case 0x48:IMP();break;case 0x49:IMM();break;case 0x4A:IMP();break;
		case 0x4B:IMP();break;case 0x4C:ABS();break;case 0x4D:ABS();break;case 0x4E:ABS();break;case 0x4F:IMP();break;
		case 0x50:REL();break;case 0x51:IZY();break;case 0x52:IMP();break;case 0x53:IMP();break;case 0x54:IMP();break;
		case 0x55:ZPX();break;case 0x56:ZPX();break;case 0x57:IMP();break;case 0x58:IMP();break;case 0x59:ABY();break;
		case 0x5A:IMP();break;case 0x5B:IMP();break;case 0x5C:IMP();break;case 0x5D:ABX();break;case 0x5E:ABX();break;
		case 0x5F:IMP();break;case 0x60:IMP();break;case 0x61:IZX();break;case 0x62:IMP();break;case 0x63:IMP();break;
		case 0x64:IMP();break;case 0x65:ZP0();break;case 0x66:ZP0();break;case 0x67:IMP();break;case 0x68:IMP();break;
		case 0x69:IMM();break;case 0x6A:IMP();break;case 0x6B:IMP();break;case 0x6C:IND();break;case 0x6D:ABS();break;
		case 0x6E:ABS();break;case 0x6F:IMP();break;case 0x70:REL();break;case 0x71:IZY();break;case 0x72:IMP();break;
		case 0x73:IMP();break;case 0x74:IMP();break;case 0x75:ZPX();break;case 0x76:ZPX();break;case 0x77:IMP();break;
		case 0x78:IMP();break;case 0x79:ABY();break;case 0x7A:IMP();break;case 0x7B:IMP();break;case 0x7C:IMP();break;
		case 0x7D:ABX();break;case 0x7E:ABX();break;case 0x7F:IMP();break;case 0x80:IMP();break;case 0x81:IZX();break;
		case 0x82:IMP();break;case 0x83:IMP();break;case 0x84:ZP0();break;case 0x85:ZP0();break;case 0x86:ZP0();break;
		case 0x87:IMP();break;case 0x88:IMP();break;case 0x89:IMP();break;case 0x8A:IMP();break;case 0x8B:IMP();break;
		case 0x8C:ABS();break;case 0x8D:ABS();break;case 0x8E:ABS();break;case 0x8F:IMP();break;case 0x90:REL();break;
		case 0x91:IZY();break;case 0x92:IMP();break;case 0x93:IMP();break;case 0x94:ZPX();break;case 0x95:ZPX();break;
		case 0x96:ZPY();break;case 0x97:IMP();break;case 0x98:IMP();break;case 0x99:ABY();break;case 0x9A:IMP();break;
		case 0x9B:IMP();break;case 0x9C:IMP();break;case 0x9D:ABX();break;case 0x9E:IMP();break;case 0x9F:IMP();break;
		case 0xA0:IMM();break;case 0xA1:IZX();break;case 0xA2:IMM();break;case 0xA3:IMP();break;case 0xA4:ZP0();break;
		case 0xA5:ZP0();break;case 0xA6:ZP0();break;case 0xA7:IMP();break;case 0xA8:IMP();break;case 0xA9:IMM();break;
		case 0xAA:IMP();break;case 0xAB:IMP();break;case 0xAC:ABS();break;case 0xAD:ABS();break;case 0xAE:ABS();break;
		case 0xAF:IMP();break;case 0xB0:REL();break;case 0xB1:IZY();break;case 0xB2:IMP();break;case 0xB3:IMP();break;
		case 0xB4:ZPX();break;case 0xB5:ZPX();break;case 0xB6:ZPY();break;case 0xB7:IMP();break;case 0xB8:IMP();break;
		case 0xB9:ABY();break;case 0xBA:IMP();break;case 0xBB:IMP();break;case 0xBC:ABX();break;case 0xBD:ABX();break;
		case 0xBE:ABY();break;case 0xBF:IMP();break;case 0xC0:IMM();break;case 0xC1:IZX();break;case 0xC2:IMP();break;
		case 0xC3:IMP();break;case 0xC4:ZP0();break;case 0xC5:ZP0();break;case 0xC6:ZP0();break;case 0xC7:IMP();break;
		case 0xC8:IMP();break;case 0xC9:IMM();break;case 0xCA:IMP();break;case 0xCB:IMP();break;case 0xCC:ABS();break;
		case 0xCD:ABS();break;case 0xCE:ABS();break;case 0xCF:IMP();break;case 0xD0:REL();break;case 0xD1:IZY();break;
		case 0xD2:IMP();break;case 0xD3:IMP();break;case 0xD4:IMP();break;case 0xD5:ZPX();break;case 0xD6:ZPX();break;
		case 0xD7:IMP();break;case 0xD8:IMP();break;case 0xD9:ABY();break;case 0xDA:IMP();break;case 0xDB:IMP();break;
		case 0xDC:IMP();break;case 0xDD:ABX();break;case 0xDE:ABX();break;case 0xDF:IMP();break;case 0xE0:IMM();break;
		case 0xE1:IZX();break;case 0xE2:IMP();break;case 0xE3:IMP();break;case 0xE4:ZP0();break;case 0xE5:ZP0();break;
		case 0xE6:ZP0();break;case 0xE7:IMP();break;case 0xE8:IMP();break;case 0xE9:IMM();break;case 0xEA:IMP();break;
		case 0xEB:IMP();break;case 0xEC:ABS();break;case 0xED:ABS();break;case 0xEE:ABS();break;case 0xEF:IMP();break;
		case 0xF0:REL();break;case 0xF1:IZY();break;case 0xF2:IMP();break;case 0xF3:IMP();break;case 0xF4:IMP();break;
		case 0xF5:ZPX();break;case 0xF6:ZPX();break;case 0xF7:IMP();break;case 0xF8:IMP();break;case 0xF9:ABY();break;
		case 0xFA:IMP();break;case 0xFB:IMP();break;case 0xFC:IMP();break;case 0xFD:ABX();break;case 0xFE:ABX();break;
		case 0xFF:IMP();break;default:System.out.printf("Addr undefined opcode: %02X\n", pc & 0xFF);break;}return op;}

	public synchronized static int lookupCycles(int op) {
		switch (op) {
		case 0x00:setCycles(7);break;case 0x01:setCycles(6);break;case 0x02:setCycles(2);break;case 0x03:setCycles(8);break;
		case 0x04:setCycles(3);break;case 0x05:setCycles(3);break;case 0x06:setCycles(5);break;case 0x07:setCycles(5);break;
		case 0x08:setCycles(3);break;case 0x09:setCycles(2);break;case 0x0A:setCycles(2);break;case 0x0B:setCycles(2);break;
		case 0x0C:setCycles(4);break;case 0x0D:setCycles(4);break;case 0x0E:setCycles(6);break;case 0x0F:setCycles(6);break;
		case 0x10:setCycles(2);break;case 0x11:setCycles(5);break;case 0x12:setCycles(2);break;case 0x13:setCycles(8);break;
		case 0x14:setCycles(4);break;case 0x15:setCycles(4);break;case 0x16:setCycles(6);break;case 0x17:setCycles(6);break;
		case 0x18:setCycles(2);break;case 0x19:setCycles(4);break;case 0x1A:setCycles(2);break;case 0x1B:setCycles(7);break;
		case 0x1C:setCycles(4);break;case 0x1D:setCycles(4);break;case 0x1E:setCycles(7);break;case 0x1F:setCycles(7);break;
		case 0x20:setCycles(6);break;case 0x21:setCycles(6);break;case 0x22:setCycles(2);break;case 0x23:setCycles(8);break;
		case 0x24:setCycles(3);break;case 0x25:setCycles(3);break;case 0x26:setCycles(5);break;case 0x27:setCycles(5);break;
		case 0x28:setCycles(4);break;case 0x29:setCycles(2);break;case 0x2A:setCycles(2);break;case 0x2B:setCycles(2);break;
		case 0x2C:setCycles(4);break;case 0x2D:setCycles(4);break;case 0x2E:setCycles(6);break;case 0x2F:setCycles(6);break;
		case 0x30:setCycles(2);break;case 0x31:setCycles(5);break;case 0x32:setCycles(2);break;case 0x33:setCycles(8);break;
		case 0x34:setCycles(4);break;case 0x35:setCycles(4);break;case 0x36:setCycles(6);break;case 0x37:setCycles(6);break;
		case 0x38:setCycles(2);break;case 0x39:setCycles(4);break;case 0x3A:setCycles(2);break;case 0x3B:setCycles(7);break;
		case 0x3C:setCycles(4);break;case 0x3D:setCycles(4);break;case 0x3E:setCycles(7);break;case 0x3F:setCycles(7);break;
		case 0x40:setCycles(6);break;case 0x41:setCycles(6);break;case 0x42:setCycles(2);break;case 0x43:setCycles(8);break;
		case 0x44:setCycles(3);break;case 0x45:setCycles(3);break;case 0x46:setCycles(5);break;case 0x47:setCycles(5);break;
		case 0x48:setCycles(3);break;case 0x49:setCycles(2);break;case 0x4A:setCycles(2);break;case 0x4B:setCycles(2);break;
		case 0x4C:setCycles(3);break;case 0x4D:setCycles(4);break;case 0x4E:setCycles(6);break;case 0x4F:setCycles(6);break;
		case 0x50:setCycles(2);break;case 0x51:setCycles(5);break;case 0x52:setCycles(2);break;case 0x53:setCycles(8);break;
		case 0x54:setCycles(4);break;case 0x55:setCycles(4);break;case 0x56:setCycles(6);break;case 0x57:setCycles(6);break;
		case 0x58:setCycles(2);break;case 0x59:setCycles(4);break;case 0x5A:setCycles(2);break;case 0x5B:setCycles(7);break;
		case 0x5C:setCycles(4);break;case 0x5D:setCycles(4);break;case 0x5E:setCycles(7);break;case 0x5F:setCycles(7);break;
		case 0x60:setCycles(6);break;case 0x61:setCycles(6);break;case 0x62:setCycles(2);break;case 0x63:setCycles(8);break;
		case 0x64:setCycles(3);break;case 0x65:setCycles(3);break;case 0x66:setCycles(5);break;case 0x67:setCycles(5);break;
		case 0x68:setCycles(4);break;case 0x69:setCycles(2);break;case 0x6A:setCycles(2);break;case 0x6B:setCycles(2);break;
		case 0x6C:setCycles(5);break;case 0x6D:setCycles(4);break;case 0x6E:setCycles(6);break;case 0x6F:setCycles(6);break;
		case 0x70:setCycles(2);break;case 0x71:setCycles(5);break;case 0x72:setCycles(2);break;case 0x73:setCycles(8);break;
		case 0x74:setCycles(4);break;case 0x75:setCycles(4);break;case 0x76:setCycles(6);break;case 0x77:setCycles(6);break;
		case 0x78:setCycles(2);break;case 0x79:setCycles(4);break;case 0x7A:setCycles(2);break;case 0x7B:setCycles(7);break;
		case 0x7C:setCycles(4);break;case 0x7D:setCycles(4);break;case 0x7E:setCycles(7);break;case 0x7F:setCycles(7);break;
		case 0x80:setCycles(2);break;case 0x81:setCycles(6);break;case 0x82:setCycles(2);break;case 0x83:setCycles(6);break;
		case 0x84:setCycles(3);break;case 0x85:setCycles(3);break;case 0x86:setCycles(3);break;case 0x87:setCycles(3);break;
		case 0x88:setCycles(2);break;case 0x89:setCycles(2);break;case 0x8A:setCycles(2);break;case 0x8B:setCycles(2);break;
		case 0x8C:setCycles(4);break;case 0x8D:setCycles(4);break;case 0x8E:setCycles(4);break;case 0x8F:setCycles(4);break;
		case 0x90:setCycles(2);break;case 0x91:setCycles(6);break;case 0x92:setCycles(2);break;case 0x93:setCycles(6);break;
		case 0x94:setCycles(4);break;case 0x95:setCycles(4);break;case 0x96:setCycles(4);break;case 0x97:setCycles(4);break;
		case 0x98:setCycles(2);break;case 0x99:setCycles(5);break;case 0x9A:setCycles(2);break;case 0x9B:setCycles(5);break;
		case 0x9C:setCycles(5);break;case 0x9D:setCycles(5);break;case 0x9E:setCycles(5);break;case 0x9F:setCycles(5);break;
		case 0xA0:setCycles(2);break;case 0xA1:setCycles(6);break;case 0xA2:setCycles(2);break;case 0xA3:setCycles(6);break;
		case 0xA4:setCycles(3);break;case 0xA5:setCycles(3);break;case 0xA6:setCycles(3);break;case 0xA7:setCycles(3);break;
		case 0xA8:setCycles(2);break;case 0xA9:setCycles(2);break;case 0xAA:setCycles(2);break;case 0xAB:setCycles(2);break;
		case 0xAC:setCycles(4);break;case 0xAD:setCycles(4);break;case 0xAE:setCycles(4);break;case 0xAF:setCycles(4);break;
		case 0xB0:setCycles(2);break;case 0xB1:setCycles(5);break;case 0xB2:setCycles(2);break;case 0xB3:setCycles(5);break;
		case 0xB4:setCycles(4);break;case 0xB5:setCycles(4);break;case 0xB6:setCycles(4);break;case 0xB7:setCycles(4);break;
		case 0xB8:setCycles(2);break;case 0xB9:setCycles(4);break;case 0xBA:setCycles(2);break;case 0xBB:setCycles(4);break;
		case 0xBC:setCycles(4);break;case 0xBD:setCycles(4);break;case 0xBE:setCycles(4);break;case 0xBF:setCycles(4);break;
		case 0xC0:setCycles(2);break;case 0xC1:setCycles(6);break;case 0xC2:setCycles(2);break;case 0xC3:setCycles(8);break;
		case 0xC4:setCycles(3);break;case 0xC5:setCycles(3);break;case 0xC6:setCycles(5);break;case 0xC7:setCycles(5);break;
		case 0xC8:setCycles(2);break;case 0xC9:setCycles(2);break;case 0xCA:setCycles(2);break;case 0xCB:setCycles(2);break;
		case 0xCC:setCycles(4);break;case 0xCD:setCycles(4);break;case 0xCE:setCycles(6);break;case 0xCF:setCycles(6);break;
		case 0xD0:setCycles(2);break;case 0xD1:setCycles(5);break;case 0xD2:setCycles(2);break;case 0xD3:setCycles(8);break;
		case 0xD4:setCycles(4);break;case 0xD5:setCycles(4);break;case 0xD6:setCycles(6);break;case 0xD7:setCycles(6);break;
		case 0xD8:setCycles(2);break;case 0xD9:setCycles(4);break;case 0xDA:setCycles(2);break;case 0xDB:setCycles(7);break;
		case 0xDC:setCycles(4);break;case 0xDD:setCycles(4);break;case 0xDE:setCycles(7);break;case 0xDF:setCycles(7);break;
		case 0xE0:setCycles(2);break;case 0xE1:setCycles(6);break;case 0xE2:setCycles(2);break;case 0xE3:setCycles(8);break;
		case 0xE4:setCycles(3);break;case 0xE5:setCycles(3);break;case 0xE6:setCycles(5);break;case 0xE7:setCycles(5);break;
		case 0xE8:setCycles(2);break;case 0xE9:setCycles(2);break;case 0xEA:setCycles(2);break;case 0xEB:setCycles(2);break;
		case 0xEC:setCycles(4);break;case 0xED:setCycles(4);break;case 0xEE:setCycles(6);break;case 0xEF:setCycles(6);break;
		case 0xF0:setCycles(2);break;case 0xF1:setCycles(5);break;case 0xF2:setCycles(2);break;case 0xF3:setCycles(8);break;
		case 0xF4:setCycles(4);break;case 0xF5:setCycles(4);break;case 0xF6:setCycles(6);break;case 0xF7:setCycles(6);break;
		case 0xF8:setCycles(2);break;case 0xF9:setCycles(4);break;case 0xFA:setCycles(2);break;case 0xFB:setCycles(7);break;
		case 0xFC:setCycles(4);break;case 0xFD:setCycles(4);break;case 0xFE:setCycles(7);break;case 0xFF:setCycles(7);break;
		default:System.out.printf("cycle undefined opcode: %02X\n", pc & 0xFF);break;}return getCycles();}
/* @formatter:on */
	public static int getCycles() {
		return cycles;
	}

	public static void setCycles(int cycles) {
		olc6502.cycles = cycles;
	}

	public static int getAddCycles() {
		return addCycles1;
	}

	public static int setAddCycles(int addCycles) {
		return addCycles1 = addCycles;
	}

	static int BRK() {
		pc = (pc + 1) & 0xFFFF;
		setFlag(I, true);
		write(0x0100 + stkp, ((pc >> 8) & 0x00FF));
		stkp = (stkp - 1) & 0xFF;
		write(0x0100 + stkp, (pc & 0x00FF));
		stkp = (stkp - 1) & 0xFF;
		setFlag(B, true);
		write(0x0100 + stkp, status);
		stkp = (stkp - 1) & 0xFF;
		setFlag(B, false);

		pc = read(0xFFFE) | (read(0xFFFF) << 8) &0xFFFF;
		return setAddCycles(0);

	}

	static int ADC() {
		fetch();

		temp = (a + fetched + getFlag(C));

		setFlag(C, temp > 255);
		setFlag(Z, (temp & 0x00FF) == 0);
		setFlag(V, ((~(a ^ fetched) & (a ^ temp)) & 0x0080) > 0);
		setFlag(N, (temp & 0x80) == 128);

		a = temp & 0x00FF;
		return setAddCycles(1);
	}

	static int SBC() {
		fetch();
		int value = ((fetched) ^ 0x00FF);
		temp = (a + value + getFlag(C));
		setFlag(C, temp > 255);
		setFlag(Z, (temp & 0x00FF) == 0);
		setFlag(V, ((temp ^ a) & (temp ^ value) & 0x0080) > 0);
		setFlag(N, (temp & 0x80) == 128);

		a = (temp & 0x00FF);

		return setAddCycles(1);
	}

	static int ASL() {
		fetch();
		temp = (fetched << 1) & 0xFFFF;
		setFlag(C, (temp & 0xFF00) > 0);
		setFlag(Z, (temp & 0x00FF) == 0x00);
		setFlag(N, (temp & 0x80) != 0);
		if (disAsm.op2Name(opcode) == "IMP") {
			a = temp & 0x00FF;
		} else {
			write(addr_abs, (temp & 0x00FF));
		}
		return setAddCycles(0);
	}

	static int AND() {
		fetch();
		a = (a & fetched) &0xFF;
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x80) != 0);
		return setAddCycles(1);

	}

	static int BCC() {
		if (getFlag(C) == 0) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int BCS() {
		if (getFlag(C) == 1) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int BEQ() {
		if (getFlag(Z) == 1) {
			cycles = (cycles + 1);

			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}

			pc = addr_abs;
		}
		return setAddCycles(0);

	}

	static int BIT() {
		fetch();
		temp = (a & fetched) & 0xFF;
		setFlag(Z, (temp & 0x00FF) == 0x00);
		setFlag(N, (fetched & (1 << 7)) != 0);
		setFlag(V, (fetched & (1 << 6)) != 0);
		return setAddCycles(0);
	}

	static int BMI() {
		if (getFlag(N) == 1) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int BNE() {
		if (getFlag(Z) == 0) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int BPL() {
		if (getFlag(N) == 0) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int BVC() {
		if (getFlag(V) == 0) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int BVS() {
		if (getFlag(V) == 1) {
			cycles = (cycles + 1);
			addr_abs = (pc + addr_rel) & 0xFFFF;
			if ((addr_abs & 0xFF00) != (pc & 0xFF00)) {
				cycles = (cycles + 1);
			}
			pc = addr_abs;
		}
		return setAddCycles(0);
	}

	static int CLC() {
		setFlag(C, false);
		return setAddCycles(0);
	}

	static int CLD() {
		setFlag(D, false);
		return setAddCycles(0);
	}

	static int CLI() {
		setFlag(I, false);
		return setAddCycles(0);
	}

	static int CLV() {
		setFlag(V, false);
		return setAddCycles(0);
	}

	static int CMP() {
		fetch();
		temp = (a - fetched) & 0xFFFF;
		setFlag(C, a >= fetched);
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		return setAddCycles(1);
	}

	static int CPX() {
		fetch();
		temp = (x - fetched) & 0xFFFF;
		setFlag(C, x >= fetched);
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		return setAddCycles(0);
	}

	static int CPY() {
		fetch();
		temp = (y - fetched) & 0xFFFF;
		setFlag(C, y >= fetched);
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		return setAddCycles(0);
	}

	static int DEC() {
		fetch();
		temp = (fetched - 1) & 0xFFFF;
		write(addr_abs, (temp & 0x00FF));
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		return setAddCycles(0);
	}

	static int DEX() {
		x = (x - 1) & 0xFF;
		setFlag(Z, x == 0x00);
		setFlag(N, (x & 0x80) != 0);
		return setAddCycles(0);
	}

	static int DEY() {
		y = (y - 1) & 0xFF;
		setFlag(Z, y == 0x00);
		setFlag(N, (y & 0x0080) != 0);
		return setAddCycles(0);
	}

	static int EOR() {
		fetch();
		a = (a ^ fetched) & 0xFFFF;
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x0080) != 0);
		return setAddCycles(1);
	}

	static int INC() {
		fetch();
		temp = (fetched + 1) & 0xFFFF;
		write(addr_abs, (temp & 0x00FF));
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		return setAddCycles(0);
	}

	static int IND() {
		int ptr_lo = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int ptr_hi = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int ptr = (ptr_hi << 8 | ptr_lo);
		if (ptr_lo == 0x00FF) {
			addr_abs = (read(ptr & 0xFF00) << 8) | read(ptr + 0);
		} else {
			addr_abs = (read(ptr + 1) << 8) | read(ptr + 0);
		}
		return setAddCycles(0);
	}

	static int INX() {
		x = (x + 1) & 0xFF;
		setFlag(Z, x == 0x00);
		setFlag(N, (x & 0x80) != 0);
		return setAddCycles(0);
	}

	static int INY() {
		y = (y + 1) & 0xFF;
		setFlag(Z, y == 0x00);
		setFlag(N, (y & 0x80) != 0);
		return setAddCycles(0);
	}

	static int JMP() {
		pc = addr_abs;
		return setAddCycles(0);
	}

	static int JSR() {
		pc = (pc - 1) & 0xFFFF;
		write(0x0100 + stkp, (pc >> 8) & 0x00FF);
		stkp = (stkp - 1) & 0xFF;
		write((0x0100 + stkp), (pc & 0x00FF));
		stkp = (stkp - 1) & 0xFF;
		pc = addr_abs;
		return setAddCycles(0);
	}

	static int LDA() {
		fetch();
		a = fetched & 0xFF;
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x80) != 0);
		return setAddCycles(1);
	}

	static int LDX() {
		fetch();
		x = fetched & 0xFF;
		setFlag(Z, x == 0x00);
		setFlag(N, (x & 0x80) != 0);
		return setAddCycles(1);
	}

	static int LDY() {
		fetch();
		y = fetched & 0xFF;
		setFlag(Z, y == 0x00);
		setFlag(N, (y & 0x80) != 0);
		return setAddCycles(1);
	}

	static int LSR() {
		fetch();
		setFlag(C, (fetched & 0x0001) != 0);
		temp = (fetched >> 1);
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		if (disAsm.op2Name(opcode) == "IMP") {
			a = temp & 0x00FF;
		} else {
			write(addr_abs, (temp & 0x00FF));
		}
		return setAddCycles(0);
	}

/* @formatter:off */
	static int NOP() {
		switch (opcode) {
		case 0x1C:break;
		case 0x3C:break;
		case 0x5C:break;
		case 0x7C:break;
		case 0xDC:break;
		case 0xFC:return setAddCycles(1);
		}
		return setAddCycles(0);
	}
/* @formatter:on */
	static int ORA() {
		fetch();
		a = (a | fetched) &0xFF;
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x80) != 0);
		return setAddCycles(1);
	}

	static int PHA() {
		write((0x0100 + stkp), a);
		stkp = (stkp - 1) & 0xFF;
		return setAddCycles(0);
	}

	static int PHP() {
		write(0x0100 + stkp, status | B | U);
		setFlag(B, false);
		setFlag(U, false);
		stkp = (stkp - 1) & 0xFF;
		return setAddCycles(0);
	}

	static int PLA() {
		stkp = (stkp + 1) & 0xFF;
		a = read(0x0100 + stkp);
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x80) != 0);
		return setAddCycles(0);
	}

	static int PLP() {
		stkp = (stkp + 1) & 0xFF;
		status = read(0x0100 + stkp);
		setFlag(U, true);
		return setAddCycles(0);
	}

	static int ROL() {
		fetch();
		temp = (fetched << 1) | getFlag(C);
		setFlag(C, (temp & 0xFF00) != 0);
		setFlag(Z, (temp & 0x00FF) == 0x0000);
		setFlag(N, (temp & 0x0080) != 0);
		if (disAsm.op2Name(opcode) == "IMP") {
			a = temp & 0x00FF;
		} else {
			write(addr_abs, (temp & 0x00FF));
		}
		return setAddCycles(0);
	}

	static int ROR() {
		fetch();
		temp = (getFlag(C) << 7) | (fetched >> 1);
		setFlag(C, (fetched & 0x01) != 0);
		setFlag(Z, (temp & 0x00FF) == 0x00);
		setFlag(N, (temp & 0x0080) != 0);
		if (disAsm.op2Name(opcode) == "IMP") {
			a = temp & 0x00FF;
		} else {
			write(addr_abs, (temp & 0x00FF));
		}
		return setAddCycles(0);
	}

	static int RTI() {
		stkp = (stkp + 1) & 0xFF;
		status = read(0x0100 + stkp);
		status &= ~B & 0xFF;
		status &= ~U & 0xFF;
		stkp = (stkp + 1) & 0xFF;
		pc = read(0x0100 + stkp)&0xFFFF;
		stkp = (stkp + 1) & 0xFF;
		pc |= read(0x0100 + stkp) << 8 &0xFFFF;
		return setAddCycles(0);
	}

	static int RTS() {
		stkp = (stkp + 1) & 0xFF;
		pc = read(0x0100 + stkp) &0xFFFF;
		stkp = (stkp + 1) & 0xFF;
		pc |= read(0x0100 + stkp) << 8 &0xFFFF;
		pc = (pc + 1) & 0xFFFF;
		return setAddCycles(0);
	}

	static int SEC() {
		setFlag(C, true);
		return setAddCycles(0);
	}

	static int SED() {
		setFlag(D, true);
		return setAddCycles(0);
	}

	static int SEI() {
		setFlag(I, true);
		return setAddCycles(0);
	}

	static int STA() {
		write(addr_abs, a);
		return setAddCycles(0);
	}

	static int STX() {
		write(addr_abs, x);
		return setAddCycles(0);
	}

	static int STY() {
		write(addr_abs, y);
		return setAddCycles(0);
	}

	static int TAX() {
		x = a;
		setFlag(Z, x == 0x00);
		setFlag(N, (x & 0x80) != 0);
		return setAddCycles(0);
	}

	static int TAY() {
		y = a;
		setFlag(Z, y == 0x00);
		setFlag(N, (y & 0x80) != 0);
		return setAddCycles(0);
	}

	static int TSX() {
		x = stkp & 0xFF;
		setFlag(Z, x == 0x00);
		setFlag(N, (x & 0x80) != 0);
		return setAddCycles(0);
	}

	static int TXA() {
		a = x;
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x80) != 0);
		return setAddCycles(0);
	}

	static int TXS() {
		stkp = x;
		return setAddCycles(0);
	}

	static int TYA() {
		a = y;
		setFlag(Z, a == 0x00);
		setFlag(N, (a & 0x80) != 0);
		return setAddCycles(0);
	}

	static int XXX() {
		return setAddCycles(0);
	}

	static int ZPY() {
		addr_abs = (read(pc) + y) &0xFFFF;
		pc = (pc + 1) & 0xFFFF;
		addr_abs &= 0x00FF;
		return setAddCycles(0);
	}

	// op2
	static int ABS() {
		int lo = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int hi = read(pc);
		pc = (pc + 1) & 0xFFFF;
		addr_abs = ((hi << 8) | lo) &0xFFFF;
		return setAddCycles(0);
	}

	static int ABX() {
		int lo = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int hi = read(pc);
		pc = (pc + 1) & 0xFFFF;
		addr_abs = ((hi << 8) | lo) &0xFFFF;
		addr_abs = (addr_abs + x) & 0xFFFF;
		if ((addr_abs & 0xFF00) != (hi << 8)) {
			return setAddCycles(1);
		} else {
			return setAddCycles(0);
		}
	}

	static int ABY() {
		int lo = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int hi = read(pc);
		pc = (pc + 1) & 0xFFFF;
		addr_abs = ((hi << 8) | lo) &0xFFFF;
		addr_abs = (addr_abs + y) & 0xFFFF;
		if ((addr_abs & 0xFF00) != (hi << 8)) {
			return setAddCycles(1);
		} else {
			return setAddCycles(0);
		}
	}

	static int IMM() {
		addr_abs = pc++ & 0xFFFF;
		return setAddCycles(0);
	}

	static int IMP() {
		fetched = a;
		return setAddCycles(0);

	}

	static int IZX() {
		int temp = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int lo = read((temp + x) & 0x00FF);
		int hi = read(((temp + x) + 1) & 0x00FF);
		addr_abs = ((hi << 8) | lo) &0xFFFF;
		return setAddCycles(0);
	}

	static int IZY() {
		int temp = read(pc);
		pc = (pc + 1) & 0xFFFF;
		int lo = read(temp & 0x00FF);
		int hi = read((temp + 1) & 0x00FF);
		addr_abs = ((hi << 8) | lo) &0xFFFF;
		addr_abs = (addr_abs + y) & 0xFFFF;
		if ((addr_abs & 0xFF00) != (hi << 8)) {
			return setAddCycles(1);
		} else {
			return setAddCycles(0);
		}
	}

	static int REL() {
		addr_rel = read(pc);
		pc = (pc + 1) & 0xFFFF;
		if ((addr_rel & 0x80) != 0) {
			addr_rel |= 0xFF00;
		}

		return setAddCycles(0);
	}

	static int ZP0() {
		addr_abs = read(pc) &0xFFFF;
		pc = (pc + 1) & 0xFFFF;
		addr_abs &= 0x00FF;
		return setAddCycles(0);
	}

	static int ZPX() {
		addr_abs = (read(pc) + x) &0xFFFF;
		pc = (pc + 1) & 0xFFFF;
		addr_abs &= 0x00FF;
		return setAddCycles(0);
	}

	public static int fetch() {
		if (disAsm.op2Name(opcode) != "IMP") {
			olc6502.fetched = read(olc6502.addr_abs); // &0xFF
		}
		return fetched;
	}

	public static void drawCodeOut() {

		if (Main.debug) {
		String first = (hex4(pc - 1));
		String op1 = (disAsm.op1Name(opcode));
		String value = "";
		String op2 = " {" + (disAsm.op2Name(opcode)) + "}";

		if (disAsm.op2Name(opcode) == "IMP") {
			value = ((hex2(read(Bus.disAddr))) + " " + (hex2(olc6502.read(olc6502.pc) & 0xFF00)) + "     ");

		} else if (disAsm.op2Name(opcode) == "IMM") {
			value = (hex2(read(Bus.disAddr)) + " " + (hex2(read(Bus.disAddr) & 0xFF00)) + "     ");

		} else if (disAsm.op2Name(olc6502.opcode) == "ZP0") {
			value = (hex2(read(Bus.disAddr)) + "        ");

		} else if (disAsm.op2Name(olc6502.opcode) == "ZPX") {
			value = (hex2(read(Bus.disAddr)) + ",      X");

		} else if (disAsm.op2Name(olc6502.opcode) == "ZPY") {
			value = (hex2(read(Bus.disAddr)) + ", Y  ");

		} else if (disAsm.op2Name(olc6502.opcode) == "IZX") {
			value = (hex2(read(Bus.disAddr)) + ",      X");

		} else if (disAsm.op2Name(olc6502.opcode) == "IZY") {
			value = (hex2(read(Bus.disAddr)) + ",      Y");

		} else if (disAsm.op2Name(olc6502.opcode) == "ABS") {
			String lo = (hex2(read(Bus.disAddr)));
			String hi = (hex4s(read(Bus.disAddr) >> 6));
			value = (lo + " " + hi + "  ");

		} else if (disAsm.op2Name(olc6502.opcode) == "ABX") {
			int lo = (read(Bus.disAddr));
			int hi = (read(Bus.disAddr));
			value = (hex4s(read(hi << 8) | lo) + ",   X");

		} else if (disAsm.op2Name(olc6502.opcode) == "ABY") {
			int lo = (read(Bus.disAddr));
			int hi = (read(Bus.disAddr));
			value = (hex4s(read(hi << 8) | lo) + ", Y  ");

		} else if (disAsm.op2Name(olc6502.opcode) == "IND") {
			int lo = (read(Bus.disAddr));
			int hi = (read(Bus.disAddr));
			value = (hex4s(read(hi << 8) | lo) + "   ");

//		} else if (disAsm.op2Name(olc6502.opcode) == "REL") {
//		value = (hex2(Bus.disAddr)+ " " + hex4s(Bus.disAddr + (Bus.disAddr)) + "  ");
//		}
		} else if (disAsm.op2Name(olc6502.opcode) == "REL") {
			value = (hex2(Bus.disAddr + 4) + " " + (hex2(Bus.disAddr + 239)) + "     ");
		}
		String all = new String(
				first + "  " + value + "     " + op1 + op2 + " A:" + hex2(a) + " " + "X:" + hex2(x) + " " + "Y:"
						+ hex2(y) + " " + "P:" + hex2(status) + " SP:" + hex2(stkp) + "  CYC:" + clock_count + "\n");

		System.out.format("%20s", all);
	}}

	/*
	 * public static void drawCode(int x, int y, int nLines, Graphics2D g) {
	 * 
	 * String first = (hex4(pc)); String op1 = (disAsm.op1Name(opcode)); String
	 * value = ""; String op2 = " {" + (disAsm.op2Name(opcode)) + "}";
	 * 
	 * if (disAsm.op2Name(opcode) == "IMP") { value = ((hex2(read(Bus.disAddr))) +
	 * " " + (hex2(olc6502.read(olc6502.pc) & 0xFF00)) + "    ");
	 * 
	 * } else if (disAsm.op2Name(opcode) == "IMM") { value =
	 * (hex4(read(Bus.disAddr))+ "   ");
	 * 
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "ZP0") { value =
	 * (hex4(addr_abs)+ "   ");
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "ZPX") { value =
	 * (hex4(addr_abs)+ "  ");
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "ZPY") { value =
	 * (hex4(addr_abs));
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "IZX") { value =
	 * (hex4(addr_abs));
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "IZY") { value =
	 * (hex4(addr_abs));
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "ABS") { value =
	 * (hex4(addr_abs)+ " ");
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "ABX") { value =
	 * (hex4(addr_abs));
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "ABY") { value =
	 * (hex4(addr_abs));
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "IND") { value =
	 * (hex4(addr_abs));
	 * 
	 * } else if (disAsm.op2Name(olc6502.opcode) == "REL") { value =
	 * (hex4(Bus.cpuRead(Bus.disAddr, true)) + "   ");
	 * 
	 * }
	 * 
	 * g.drawString(first + value + "   " + op1 + op2 + "      CYC:" + clock_count,
	 * x, y); }
	 */
	static char[] h = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', };

	public static String hex2(int i) {
		i = i & 0xff;
		StringBuffer buff = new StringBuffer();
		buff.append(h[(i / 16) % 16]);
		buff.append(h[i % 16]);
		return buff.toString();
	}

	public static String hex4s(int i) {
		return hex2(i >> 8) + " " + hex2(i);
	}

	public static String hex4(int i) {
		return hex2(i >> 8) + hex2(i);
	}

}