package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.PriorityQueue;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HuffmanTask extends Task {
	static int pointer = 0;
	static long headerSize = 0;
	static long bodySize = 0;
	static int binSize = 0;
	static OutputStream outputStream = null;

	@Override
	protected Object call() throws Exception {

		updateProgress(0, 10);
		updateMessage("Loading...");

		if (SampleController.choose == 1)
			Huff(SampleController.fileo);
		else
			DeHuff(SampleController.fileo);

		updateProgress(900, 1000);
		updateMessage("Finished");

		return null;

	}

	public void Huff(File file) throws IOException {
		// code length freq.
		long[][] Table = SampleController.Table;

		// initialize table
		for (int i = 0; i < 256; i++)
			Table[i][2] = 0;

		// calculate freq
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));) {
			int available = inputStream.available();
			byte[] buffer = new byte[8];
			int count = 0, bufferlen = inputStream.read(buffer);
			while (bufferlen != -1) {
				count++;
				if (count == 50000) {

					count = 0;
					updateProgress(((available - inputStream.available()) / 4), available); 
				}

				for (int i = 0; i < bufferlen; i++) {
					if (buffer[i] >= 0)
						Table[buffer[i]][2]++;
					else
						Table[buffer[i] + 256][2]++;
				}
				bufferlen = inputStream.read(buffer);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// make heap
		PriorityQueue<node> heap = new PriorityQueue<node>();
		int numberOfNodes = 0;
		// populate heap
		for (int i = 0; i < Table.length; i++) {
			if (Table[i][2] != 0) {
				heap.add(new node((char) i, Table[i][2], true));
				numberOfNodes++;
			}
		}

		// make Tree.
		node x, y, z;
		for (int i = 1; i < numberOfNodes; i++) {
			x = heap.remove();
			y = heap.remove();
			z = new node(x.num + y.num);
			z.left = x;
			z.right = y;
			heap.add(z);
		}

		node root = heap.remove();

		// count header size
		functions.countHeaderSize(root);

		// get new codes
		functions.getCodes(root, Table, 0, 31);


		String fileExtension = file.getName().substring(file.getName().indexOf('.') + 1);
		byte extensionLength = (byte) fileExtension.length();
		String newName = file.getName().substring(0, file.getName().indexOf('.')) + "header.huf";
		String newName2 = file.getName().substring(0, file.getName().indexOf('.')) + ".huf";
		// System.out.println("Header: " + newName);
		outputStream = new BufferedOutputStream(new FileOutputStream((new File(newName))));
		outputStream.write(extensionLength); // System.out.print("length: (" + extensionLength + " )");
		outputStream.write(fileExtension.getBytes());


		byte[] headerByte = new byte[4];
		headerByte[0] = (byte) (functions.headerSize >> 24);
		headerByte[1] = (byte) (functions.headerSize >> 16);
		headerByte[2] = (byte) (functions.headerSize >> 8);
		headerByte[3] = (byte) (functions.headerSize);
		outputStream.write(headerByte);
		functions.headerSize = 0;


		byte[] buffer = new byte[8];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = 0;

		serializeTree(root, buffer);

		if (pointer == 0) {
			outputStream.write(0);
		} else {
			int usedBytes = (pointer / 8) + 1;
			byte usedBits = (byte) (pointer % 8); // in the last byte.
			for (int i = 0; i < usedBytes; i++) {

				outputStream.write(buffer[i]);
			}
			outputStream.write(usedBits); // saves the number of needed bits from the last byte.
		}
		pointer = 0;

		outputStream.flush();
		outputStream.close();

		// SECOND FILE
		outputStream = new BufferedOutputStream(new FileOutputStream((new File(newName2))));
		long bitsRemaining = 0;
		long codeRemaining = 0;
		
		// Body
		pointer = 0;
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = 0;
		}
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));) {
			int available = inputStream.available();
			byte[] buffer1 = new byte[8];
			byte bitsLeftInByte = 0;
			int count = 0, bufferlen = inputStream.read(buffer1);
			while (bufferlen != -1) {
				count++;
				if (count == 50000) {

					count = 0;
					updateProgress(available / 4 + ((available - inputStream.available()) * (3.0 / 4)), available); 
				}
				for (int i = 0; i < bufferlen; i++) {
					bodySize += Table[i][1];

					bitsRemaining = Table[Byte.toUnsignedInt(buffer1[i])][1];
					codeRemaining = Table[Byte.toUnsignedInt(buffer1[i])][0];
					while (bitsRemaining > 0) {
						bitsLeftInByte = (byte) (8 - pointer % 8);

						if (bitsLeftInByte >= bitsRemaining) {
							buffer[pointer / 8] = (byte) (buffer[pointer / 8]
									| codeRemaining << (bitsLeftInByte - bitsRemaining));
							pointer += bitsRemaining;
							bitsRemaining = 0;
						} else {
							buffer[pointer / 8] = (byte) (buffer[pointer / 8]
									| codeRemaining >> (bitsRemaining - bitsLeftInByte));
							pointer += bitsLeftInByte;
							bitsRemaining -= bitsLeftInByte;
						}
						if (pointer == 64) {
							emptyBuffer(buffer);
							pointer = 0;
						}
					}
				}
				bufferlen = inputStream.read(buffer1);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// To know the end of the body.
		if (pointer == 0) {
			outputStream.write(0);
		} else {
			int usedBytes = (pointer / 8) + 1;
			byte usedBits = (byte) (pointer % 8); // in the last byte.
			for (int i = 0; i < usedBytes; i++) {
				outputStream.write(buffer[i]);
			}
			outputStream.write(usedBits); // saves the number of needed bits from the last byte.
		}
		pointer = 0;

		outputStream.flush();
		outputStream.close();

		System.out.println("huf file size = " + binSize);
		System.out.println("FINISHED");

	}

	public static void serializeTree(node n, byte[] buffer) throws IOException {
		if (n == null)
			return;
		if (n.left == null && n.right == null) {
			buffer[pointer / 8] = (byte) (buffer[pointer / 8] | (1 << (7 - (pointer % 8))));
			pointer++;// System.out.println("b0 " + buffer[0]);
			if (pointer == 64) {
				emptyBuffer(buffer);
				pointer = 0;
			}
			byte bitsLeftInByte = (byte) (8 - pointer % 8);
			buffer[pointer / 8] = (byte) (buffer[pointer / 8] | n.c >> (8 - bitsLeftInByte));
			pointer += bitsLeftInByte;
			if (pointer == 64) {
				emptyBuffer(buffer);
				pointer = 0;
			}
			buffer[pointer / 8] = (byte) (buffer[pointer / 8] | n.c << (bitsLeftInByte));
			pointer += 8 - bitsLeftInByte;
			headerSize += 9;

		} else {
			headerSize++;
			pointer++;
			if (pointer == 64) {
				emptyBuffer(buffer);
				pointer = 0;
			}
			// System.out.print("0");
			serializeTree(n.left, buffer);
			serializeTree(n.right, buffer);

		}
	}

	public static void emptyBuffer(byte[] buffer) throws IOException {
		binSize += 8;

		outputStream.write(buffer);

		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = 0;
		}

	}

	public void DeHuff(File file) throws IOException, InterruptedException {
		String name = file.getName().substring(0, file.getName().indexOf('.')) + "header.huf";

		node center = null;
		node root = new node(false);
		node cur = root;


		InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(name)));

		byte extensionLength = (byte) inputStream.read();

		byte[] fileExtension = new byte[extensionLength];
		inputStream.read(fileExtension);
		String extensionStr = new String(fileExtension);

		byte[] headerSize = new byte[4];
		inputStream.read(headerSize);

		int headerInt = 0;
		headerInt = headerInt | headerSize[0] << 24;
		headerInt = headerInt | headerSize[1] << 16;
		headerInt = headerInt | headerSize[2] << 8;
		headerInt = headerInt | headerSize[3];


		byte[] buffer = new byte[8];
		byte cbyte = 0, cbit = 1, pout = 0; // be careful, ignoring first 0
		boolean firsttime = true, getBits = false;
		byte ch = 0, ptr = 7; // char, and pointer to build the char bit by bit.

		int available = inputStream.available();

		// 1
		while (inputStream.available() > 9) {
			inputStream.read(buffer);
			for (cbyte = 0; cbyte < 8; cbyte++) {
				for (cbit = 0; cbit < 8; cbit++) {
					// if we need a byte
					if (getBits) {
						ch = (byte) (ch | (((buffer[cbyte] >> (7 - cbit)) & 1) << ptr));
						if (ptr == 0) {
							root = functions.constructLeaf(root, (char) ch);
							functions.flag = false;
							getBits = false;
							ptr = 8;
							ch = 0;
						}
						ptr--;
						continue;
					}
					// to ignore first 0
					if (firsttime) {
						cbit++;
						firsttime = false;
					}

					if ((buffer[cbyte] & (1 << (7 - cbit))) > 0) {
						getBits = true;
					} else {
						root = functions.construct(root);
						functions.flag = false;
					}

				}
			}
		}

		// 2
		int size = inputStream.available();
		byte[] bufferLast = new byte[size];
		inputStream.read(bufferLast);
		byte last = bufferLast[size - 1];
		byte seclast = bufferLast[size - 2];

		for (cbyte = 0; cbyte < size - 2; cbyte++) {
			for (cbit = 0; cbit < 8; cbit++) {

				// if we need a byte
				if (getBits) {
					ch = (byte) (ch | (((bufferLast[cbyte] >> (7 - cbit)) & 1) << ptr));
					if (ptr == 0) {
						root = functions.constructLeaf(root, (char) ch);
						functions.flag = false;
						getBits = false;
						ptr = 8;
						ch = 0;
					}
					ptr--;
					continue;
				}
				// to ignore first 0
				if (firsttime) {
					cbit++;
					firsttime = false;
				}

				if ((bufferLast[cbyte] & (1 << (7 - cbit))) > 0) {
					getBits = true;
				} else {
					root = functions.construct(root);
					functions.flag = false;
				}

			}
		}

		// 3
		for (cbit = 0; cbit < last; cbit++) {

			// if we need a byte
			if (getBits) {
				ch = (byte) (ch | (((seclast >> (7 - cbit)) & 1) << ptr));
				if (ptr == 0) {
					root = functions.constructLeaf(root, (char) ch);
					functions.flag = false;
					getBits = false;
					ptr = 8;
					ch = 0;
				}
				ptr--;
				continue;
			}
			// to ignore first 0
			if (firsttime) {
				cbit++;
				firsttime = false;
			}

			if ((seclast & (1 << (7 - cbit))) > 0) {
				getBits = true;
			} else {
				root = functions.construct(root);
				functions.flag = false;
			}
		}

		center = root;


		String name1 = file.getName().substring(0, file.getName().indexOf('.')) + "pp." + extensionStr;
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream((new File(name1))));

		node n = root = center;
		cur = root;

		int boutSize = 0;
		try (InputStream inputStream2 = new BufferedInputStream(new FileInputStream(file));) {
			byte[] bufferIn = new byte[8];
			byte[] bufferOut = new byte[8];
			cbyte = 0;
			cbit = 0;
			pout = 0;

			available = inputStream2.available();
			int count = 0;
			// 1
			while (inputStream2.available() > 9) {
				count++;
				if (count == 50000) {
					count = 0;
					updateProgress(available - inputStream2.available(), available);
				}

				inputStream2.read(bufferIn);
				for (cbyte = 0; cbyte < 8; cbyte++) {
					for (cbit = 0; cbit < 8; cbit++) {
						if ((bufferIn[cbyte] & (1 << (7 - cbit))) > 0) {
							cur = cur.right;
						} else {
							cur = cur.left;
						}
						if (cur.left == null && cur.right == null) {
							bufferOut[pout] = (byte) cur.c;
							cur = root;
							pout++;
							if (pout == 8) {
								outputStream.write(bufferOut);
								pout = 0;
								boutSize += 8;
							}
						}
					}
				}
			}

			// 2
			size = inputStream2.available();
			byte[] bufferLast2 = new byte[size];
			inputStream2.read(bufferLast2);
			last = bufferLast2[size - 1];
			seclast = bufferLast2[size - 2];

			for (cbyte = 0; cbyte < size - 2; cbyte++) {
				for (cbit = 0; cbit < 8; cbit++) {
					if ((bufferLast2[cbyte] & (1 << (7 - cbit))) > 0) {
						cur = cur.right;
					} else {
						cur = cur.left;
					}
					if (cur.left == null && cur.right == null) {
						bufferOut[pout] = (byte) cur.c;
						cur = root;
						pout++;
						if (pout == 8) {
							outputStream.write(bufferOut);
							pout = 0;
						}
					}
				}
			}

			// 3
			for (cbit = 0; cbit < last; cbit++) {
				if ((seclast & (1 << (7 - cbit))) > 0) {
					cur = cur.right;
				} else {
					cur = cur.left;
				}

				if (cur.left == null && cur.right == null) {
					bufferOut[pout] = (byte) cur.c;
					cur = root;
					pout++;
					if (pout == 8) {
						outputStream.write(bufferOut);
						pout = 0;
					}
				}
			}

			byte[] bufferOutLast = new byte[pout];
			for (int i = 0; i < pout; i++) {
				bufferOutLast[i] = bufferOut[i];
			}

			outputStream.write(bufferOutLast);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		outputStream.flush();
		outputStream.close();
		inputStream.close();

		System.out.println("out file size = " + boutSize);
		updateMessage("Finished");

	}
}
