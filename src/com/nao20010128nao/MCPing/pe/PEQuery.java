package com.nao20010128nao.MCPing.pe;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.nao20010128nao.MCPing.Utils;

/**
 * A class that handles Minecraft Query protocol requests
 *
 * @author Ryan McCann
 */
public class PEQuery {
	final static byte HANDSHAKE = 9;
	final static byte STAT = 0;

	String serverAddress = "localhost";
	int queryPort = 25565; // the default minecraft query port

	int localPort = 25566; // the local port we're connected to the server on

	private DatagramSocket socket = null; // prevent socket already bound
											// exception
	private int token;

	public PEQuery(String address, int port) {
		serverAddress = address;
		queryPort = port;
	}

	// used to get a session token
	private void handshake() throws IOException {
		Request req = new Request();
		req.type = HANDSHAKE;
		req.sessionID = generateSessionID();

		int val = 11 - req.toBytes().length; // should be 11 bytes total
		byte[] input = Utils.padArrayEnd(req.toBytes(), val);
		byte[] result = sendUDP(input);

		token = Integer.parseInt(new String(result).trim());
	}

	/**
	 * Use this to get basic status information from the server.
	 *
	 * @return a <code>QueryResponse</code> object
	 */
	public BasicStat basicStat() throws IOException {
		handshake(); // get the session token first

		Request req = new Request(); // create a request
		req.type = STAT;
		req.sessionID = generateSessionID();
		req.setPayload(token);
		byte[] send = req.toBytes();

		byte[] result = sendUDP(send);

		BasicStat res = new BasicStat(result);
		return res;
	}

	/**
	 * Use this to get more information, including players, from the server.
	 *
	 * @return a <code>QueryResponse</code> object
	 */
	public FullStat fullStat() throws IOException {
		// basicStat() calls handshake()
		// QueryResponse basicResp = this.basicStat();
		// int numPlayers = basicResp.onlinePlayers; //TODO use to determine max
		// length of full stat

		handshake();

		Request req = new Request();
		req.type = STAT;
		req.sessionID = generateSessionID();
		req.setPayload(token);
		req.payload = Utils.padArrayEnd(req.payload, 4);

		byte[] send = req.toBytes();

		byte[] result = sendUDP(send);

		/*
		 * note: buffer size = base + #players(online) * 16(max username length)
		 */

		FullStat res = new FullStat(result);
		return res;
	}

	private byte[] sendUDP(byte[] input) throws IOException {
		while (socket == null)
			try {
				socket = new DatagramSocket(localPort); // create the socket
			} catch (BindException e) {
				++localPort; // increment if port is already in use
			}

		// create a packet from the input data and send it on the socket
		InetAddress address = InetAddress.getByName(serverAddress);
		DatagramPacket packet1 = new DatagramPacket(input, input.length,
				address, queryPort);
		socket.send(packet1);

		// receive a response in a new packet
		byte[] out = new byte[1024 * 100]; // TODO guess at max size
		DatagramPacket packet = new DatagramPacket(out, out.length);
		socket.setSoTimeout(5000); // one half second timeout
		socket.receive(packet);

		return packet.getData();
	}

	private int generateSessionID() {
		/*
		 * Can be anything, so we'll just use 1 for now. Apparently it can be
		 * omitted altogether. TODO: increment each time, or use a random int
		 */
		return 1;
	}

	@Override
	public void finalize() {
		socket.close();
	}
}
