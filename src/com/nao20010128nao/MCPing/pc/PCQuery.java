package com.nao20010128nao.MCPing.pc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nao20010128nao.MCPing.Utils;

public class PCQuery {
	private Gson gson = new Gson();
	private String host;
	private int port;

	public PCQuery(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/* handshake->Request->statJson->ping */
	private void writeHandshake(DataOutputStream out, String host, int port)
			throws IOException {
		ByteArrayOutputStream handshake_bytes = new ByteArrayOutputStream();
		DataOutputStream handshake = new DataOutputStream(handshake_bytes);

		handshake.writeByte(Utils.PACKET_HANDSHAKE);
		Utils.writeVarInt(handshake, Utils.PROTOCOL_VERSION);
		Utils.writeVarInt(handshake, host.length());
		handshake.writeBytes(host);
		handshake.writeShort(port);
		Utils.writeVarInt(handshake, Utils.STATUS_HANDSHAKE);

		Utils.writeVarInt(out, handshake_bytes.size());
		out.write(handshake_bytes.toByteArray());

	}

	private void writeRequest(DataOutputStream out) throws IOException {
		out.writeByte(0x01); // Size of packet
		out.writeByte(Utils.PACKET_STATUSREQUEST);
	}

	private String getStatJson(DataInputStream in) throws IOException {
		Utils.readVarInt(in); // Size
		int id = Utils.readVarInt(in);

		Utils.io(id == -1, "Server prematurely ended stream.");
		Utils.io(id != Utils.PACKET_STATUSREQUEST,
				"Server returned invalid packet.");

		int length = Utils.readVarInt(in);
		Utils.io(length == -1, "Server prematurely ended stream.");
		Utils.io(length == 0, "Server returned unexpected value.");

		byte[] data = new byte[length];
		in.readFully(data);
		String json = new String(data, StandardCharsets.UTF_8);
		return json;
	}

	private void doPing(DataOutputStream out, DataInputStream in)
			throws IOException {

		out.writeByte(0x09);
		out.writeByte(Utils.PACKET_PING);
		out.writeLong(System.currentTimeMillis());

		Utils.readVarInt(in); // Size
		int id = Utils.readVarInt(in);
		Utils.io(id == -1, "Server prematurely ended stream.");
		Utils.io(id != Utils.PACKET_PING, "Server returned invalid packet.");
	}

	// ///////
	public PCQueryResult fetchReply() throws IOException {
		Socket sock = null;
		try {
			sock = new Socket(host, port);
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			writeHandshake(dos, host, port);
			writeRequest(dos);
			String s = getStatJson(dis);
			try {
				return gson.fromJson(s, Reply.class);
			} catch (JsonSyntaxException e) {
				return gson.fromJson(s, Reply19.class);
			}
		} finally {
			if (sock != null)
				sock.close();
		}
	}

	public void doPingOnce() throws IOException {
		Socket sock = null;
		try {
			sock = new Socket(host, port);
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			doPing(dos, dis);
		} finally {
			if (sock != null)
				sock.close();
		}
	}
}
