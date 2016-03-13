package com.nao20010128nao.MCPing.pe;

//https://gist.github.com/nao20010128nao/dc8621eb609fdb0d2dd4

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.nao20010128nao.MCPing.ServerPingResult;

public class UnconnectedPing {
	public static final byte UCP_PID = 0x01;
	public static final long MAGIC_1ST = 0x00_ff_ff_00_fe_fe_fe_feL;
	public static final long MAGIC_2ND = 0xfd_fd_fd_fd_12_34_56_78L;

	public static UnconnectedPingResult doPing(String ip, int port)
			throws IOException {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
			ds.setSoTimeout(5000);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(25);
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(UCP_PID);
			dos.writeLong(System.currentTimeMillis());
			dos.writeLong(MAGIC_1ST);
			dos.writeLong(MAGIC_2ND);
			dos.flush();
			DatagramPacket dp = new DatagramPacket(baos.toByteArray(),
					baos.size(), InetAddress.getByName(ip), port);
			ds.send(dp);

			DatagramPacket recDp = new DatagramPacket(new byte[1000], 1000);
			ds.receive(recDp);
			byte[] recvBuf = new byte[recDp.getLength()];
			System.arraycopy(recDp.getData(), 0, recvBuf, 0, recvBuf.length);

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					recvBuf));
			if (dis.readByte() != 0x1c) {
				throw new IOException("Server replied with invalid data");
			}
			dis.readLong();// Ping ID
			dis.readLong();// Server ID
			dis.readLong();// MAGIC
			dis.readLong();// MAGIC
			String s = dis.readUTF();
			return new UnconnectedPingResult(s);
		} catch (IOException e) {
			throw e;
		} finally {
			if (ds != null)
				ds.close();
		}
	}

	public static class UnconnectedPingResult implements ServerPingResult {
		String[] serverInfos;
		String raw;

		public UnconnectedPingResult(String s) {
			serverInfos = (raw = s).split("\\;");
		}

		public String getServerName() {
			return serverInfos[1];
		}

		public String getRaw() {
			return raw;
		}
	}
}