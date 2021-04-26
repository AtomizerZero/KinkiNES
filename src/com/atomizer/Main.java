package com.atomizer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.atomizer.nes.Bus;
import com.atomizer.nes.Cartridge;
import com.atomizer.nes.Mapper;
import com.atomizer.nes.olc2C02;
import com.atomizer.nes.olc6502;


@SuppressWarnings("serial")
public class Main extends Canvas implements Runnable, MouseListener {
	public final static String TITLE = "KinkiNes";
	public static String gameName = "";
	static int scale = 3;
	static int width = 256 * scale;
	static int height = 240 * scale; // 4:3 192 * scale
	boolean painting = false;
	static JFrame frame = null;

	public static boolean running = false;
	public static boolean debug = false;
	private Thread thread;
	public static BufferStrategy bs;
	public static BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	public static Graphics2D g;

	public static String format = "%04X";
	public static String format2 = "%X";
	public static boolean bEmulationRun = false;
	static int fResidualTime = 0;
	static int fElapsedTime = 0;

	public static final double ONE_SECOND = java.util.concurrent.TimeUnit.SECONDS.toNanos(1);
	private long[] ticks = new long[1000];
	private int first, last;
	double delta;
	public static int updates = 0;
	public static int frames = 0;
	public static long timer = System.currentTimeMillis();

	private static float fps;
	public static boolean boot = true;
	public static boolean ASCII = false;

	static String s = new String();

	static int nSelectedPalette = 0;
	static int nSelectedPattern = 0;
	static int index = 0;

	static JMenuBar menuBar;
	static JMenu menu;
	static JMenuItem menuOpenItem, menuPauseItem, menuResetItem, menuExitItem;
	static Main monitor;
	public static boolean cancel = false;
	static PatternTable0 table = new PatternTable0();

	public static void main(String[] args) {
		monitor = new Main();
		monitor.setSize(new Dimension(width - 10, height - 16));
		frame = new JFrame(TITLE);
		menuLoader();
		frame.setJMenuBar(menuBar);
		frame.add(monitor);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.requestFocusInWindow();
		frame.getContentPane().setBackground(Color.black);
		monitor.start();

	}

	public static void menuLoader() {

		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menuBar.add(menu);
		menuOpenItem = new JMenuItem("Open");
		menuPauseItem = new JMenuItem("Pause");
		menuResetItem = new JMenuItem("Reset");
		menuExitItem = new JMenuItem("Exit");
		menu.add(menuOpenItem);
		menu.add(menuPauseItem);
		menu.add(menuResetItem);
		menu.addSeparator();
		menu.add(menuExitItem);
		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == menuOpenItem) {
					if (!boot) {
						bEmulationRun = false;
						try {
							Cartridge.loadCartridge(Cartridge.chooseFile("./"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (!cancel) {
							olc2C02.reset();
							Cartridge.reset();
							Bus.reset();
							olc6502.reset();
							table.refresh();
							bEmulationRun = true;
							// Bus.insertCartridge(Cartridge.);
							Bus.reset();
						}
						cancel = false;
						
					} else if (boot) {
						try {
							// Cartridge.reset();
							Cartridge.loadCartridge(Cartridge.chooseFile("./"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						boot = false;
						// Bus.insertCartridge(cart);
						Bus.reset();
						bEmulationRun = true;
					}
				} else if (event.getSource() == menuPauseItem) {
					if (!boot) {
						if (bEmulationRun == true) {
							bEmulationRun = false;
							menuPauseItem.setText("Resume");
						} else {
							bEmulationRun = true;
							menuPauseItem.setText("Pause");
						}
					}
				} else if (event.getSource() == menuResetItem) {
					Bus.reset();

				} else if (event.getSource() == menuExitItem) {
					System.exit(0);
				}
			}

		};
		menuOpenItem.addActionListener(menuListener);
		menuPauseItem.addActionListener(menuListener);
		menuResetItem.addActionListener(menuListener);
		menuExitItem.addActionListener(menuListener);
	}

	@Override
	public synchronized void run() {
		long sleep = (long) (ONE_SECOND / 60);
		try {
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			long lastTick = System.nanoTime();
			ticks[last] = System.nanoTime();
			while (ticks[last] - ticks[first] > ONE_SECOND) {
				first = circular(first + 1);
				fps--;
			}
			last = circular(last + 1);
			fps++;
			update();
			render();
			long wake = lastTick + sleep;
			do {
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (System.nanoTime() < wake);
		}
	}

	public synchronized void start() {
		if (running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void init() throws IOException {

		this.addKeyListener(new KeyInput(this));
		this.addMouseListener(monitor);
		requestFocus();
		table.showAsFrame();
		frame.toFront();
	}

	public synchronized void update() {
		frame.setTitle((TITLE + " FPS:" + getFPS() + "            " + gameName));
		onUserUpdateWithAudio();
	}

	public synchronized void render() {
		bs = this.getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}

		g = (Graphics2D) bs.getDrawGraphics();
		g.scale(scale, scale);
		g.drawImage(image2, -1, -1, width, height, this); // 4:3 192 * getScale() - 192
		table.refresh();
		g.dispose();
		bs.show();
	}

	public synchronized void onUserUpdateWithoutAudio() {
		if (bEmulationRun == true) {
			if (fResidualTime > 0) {
				fResidualTime -= getElapsedNanoTime() / ONE_SECOND;
			} else {
				fResidualTime += (1.0f / 100f) - fElapsedTime;
				do {
					Bus.clock();
				} while (!olc2C02.frame_complete);
				olc2C02.frame_complete = false;
			}

		}
		if (bEmulationRun == true) {
			olc2C02.getPatternTable0(0, nSelectedPalette);
			olc2C02.getPatternTable1(1, nSelectedPalette);
		}

	}

	public synchronized void onUserUpdateWithAudio() {
		if (bEmulationRun == true) {
			if (fResidualTime > 0) {
				fResidualTime -= getElapsedNanoTime() / ONE_SECOND /8;
			} else {
				fResidualTime += (1.0f / 100f) - fElapsedTime;
				do {
					Bus.clock();
				} while (!olc2C02.frame_complete);
				olc2C02.frame_complete = false;
			}

		}
		if (bEmulationRun == true) {
			olc2C02.getPatternTable0(0, nSelectedPalette);
			olc2C02.getPatternTable1(1, nSelectedPalette);
		}
	}
	
	

	public synchronized long getElapsedNanoTime() {
		long prev = ticks[circular(last - 2)];
		return (prev == 0) ? 0 : ticks[circular(last - 1)] - prev;
	}

	public static float getFPS() {
		return fps;
	}

	private int circular(int n) {
		return Math.floorMod(n, ticks.length);
	}
	
	public void keyReleased(KeyEvent e) {

			
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_F ? ~0x80 : 0xFF; // A Button
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_D ? ~0x40 : 0xFF; // B Button
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_A ? ~0x20 : 0xFF; // Select
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_S ? ~0x10 : 0xFF; // Start
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_UP ? ~0x08 : 0xFF;
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_DOWN ? ~0x04 : 0xFF;
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_LEFT ? ~0x02 : 0xFF;
		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_RIGHT ? ~0x01 : 0xFF;
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_F ? ~0x80 : 0xFF; // A Button
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_D ? ~0x40 : 0xFF; // B Button
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_A ? ~0x20 : 0xFF; // Select
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_S ? ~0x10 : 0xFF; // Start
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_UP ? ~0x08 : 0xFF;
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_DOWN ? ~0x04 : 0xFF;
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_LEFT ? ~0x02 : 0xFF;
//		Bus.controller[0] &= e.getKeyCode() == KeyEvent.VK_RIGHT ? ~0x01 : 0xFF;

	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_F ? 0x80 : 0x00; // A Button
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_D ? 0x40 : 0x00; // B Button
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_A ? 0x20 : 0x00; // Select
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_S ? 0x10 : 0x00; // Start
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_UP ? 0x08 : 0x00;
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_DOWN ? 0x04 : 0x00;
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_LEFT ? 0x02 : 0x00;
		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_RIGHT ? 0x01 : 0x00;
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_F ? 0x80 : 0x00; // A Button
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_D ? 0x40 : 0x00; // B Button
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_A ? 0x20 : 0x00; // Select
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_S ? 0x10 : 0x00; // Start
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_UP ? 0x08 : 0x00;
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_DOWN ? 0x04 : 0x00;
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_LEFT ? 0x02 : 0x00;
//		Bus.controller[0] |= e.getKeyCode() == KeyEvent.VK_RIGHT ? 0x01 : 0x00;

		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}

		if (key == KeyEvent.VK_P) {
			if (bEmulationRun == true) {
				bEmulationRun = false;
			} else {
				bEmulationRun = true;
			}
		}
		if (key == KeyEvent.VK_L) {
			nSelectedPalette += 1;
			if (nSelectedPalette == 8) {
				nSelectedPalette = 0;
			}
		}
		if (key == KeyEvent.VK_R) {
			Bus.reset();
		}
		if (key == KeyEvent.VK_O) {
			if (debug == true) {
				debug = false;
				System.out.println(debug);
			} else {
				debug = true;
				System.out.println(debug);

			}
		}
		if (key == KeyEvent.VK_E) {
			if (Mapper.SMB_EASY == true) {
				Mapper.SMB_EASY = false;
			} else {
				Mapper.SMB_EASY = true;
			}
		}
		if (key == KeyEvent.VK_I) {
			if (ASCII == true) {
				ASCII = false;
			} else {
				ASCII = true;
			}
		}

	}

	public void mouseClicked(MouseEvent m) {

	}

	

	@Override
	public void mouseEntered(MouseEvent m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent m) {
		int mouse = m.getButton();

		Bus.controller[1] &= mouse == MouseEvent.BUTTON1 ? 0x10 : 0x00;
		System.out.println(mouse + " pressed");

	}

	@Override
	public void mouseReleased(MouseEvent m) {
		int mouse = m.getButton();

		Bus.controller[1] &= mouse == MouseEvent.BUTTON1 ? ~0x10 : 0xFF;
		System.out.println(mouse + " released");
	}

}
