import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Ipv4Client {

	public static void main(String[] args) {
		Ipv4Client ipv4 = new Ipv4Client();
		try {
			Socket socket = new Socket("codebank.xyz", 38003);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Header fields
			short versionHlenAndTos = 0x4500;
			short length;
			short ident = 0;
			short flagsAndOffset = 0x4000; // no fragmentation
			byte ttl = 50;
			byte protocol = 6; // TCP
			short checksum;// header only
			int sourceAddr = 0; // IP address of my choice
			byte[] destAddr = socket.getInetAddress().getAddress();
			byte[] data = null;
			// Ignoring Options/Pad

			int dataSize = 2;
			int version = 4;
			int hlen = 5;
			for (int i = 0; i < 12; ++i) {
				System.out.println("Data Length:" + dataSize);
				data = ipv4.fillData(dataSize);

				length = (short) ((version * hlen) + dataSize); // header+data
				dataSize *= 2;

				// Checksum
				checksum = 0;
				ByteBuffer bb = ByteBuffer.allocate(20 + dataSize);
				bb.putShort(versionHlenAndTos);
				bb.putShort(length);
				bb.putShort(ident);
				bb.putShort(flagsAndOffset);
				bb.put(ttl);
				bb.put(protocol);
				bb.putShort(checksum);
				bb.putInt(sourceAddr);
				bb.put(destAddr);
				bb.put(data);

				checksum = ipv4.checksum(bb.array());

				// Create Packet
				bb.clear();
				bb.putShort(versionHlenAndTos);
				bb.putShort(length);
				bb.putShort(ident);
				bb.putShort(flagsAndOffset);
				bb.put(ttl);
				bb.put(protocol);
				bb.putShort(checksum);
				bb.putInt(sourceAddr);
				bb.put(destAddr);
				bb.put(data);

				socket.getOutputStream().write(bb.array());
				System.out.println(br.readLine() + "\n");
			}

			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public byte[] fillData(int dataSize) {

		byte[] data = new byte[dataSize];

		for (int i = 0; i < dataSize; i++) {
			data[i] = 0;
		}

		return data;
	}

	public short checksum(byte[] b) {
		int sum = 0;
		int length = b.length;
		int i = 0;

		while (length > 1) {
			int s = ((b[i++] << 8) & 0xFF00) | (b[i++] & 0x00FF);
			sum += s;
			if ((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
			length -= 2;
		}

		return (short) ~(sum & 0xFFFF);
	}

}
