package com.diamonds;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;

import android.util.Log;

public class SocketServerReplyThread extends Thread {

	private Socket sock;
	OnCommunication comListener;
	private int id;
	private Player player = null;

	SocketServerReplyThread(OnCommunication comListener, Socket socket, int id) {
		sock = socket;
		this.comListener = comListener;
		this.id = id;

		try {
			player = comListener.onConnection(this, id);
		} catch (PlayerNotFoundException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				Log.d(CONSTANTS.TAG,
						"SocketReply died : no empty slots for new player");
			}
		}
	}

	@Override
	public void run() {

		if (player == null) {
			return;
		}

		try {
			while (true) {
				byte[] lengthBuffer = new byte[4];
				sock.getInputStream().read(lengthBuffer);
				int length = convertFromBytes(lengthBuffer);

				Log.d(CONSTANTS.TAG,
						"SocketReply ["
								+ id
								+ "] reading:"
								+ length
								+ "|"
								+ new String(lengthBuffer, Charset
										.forName("UTF-8")));
				if (length == 0) {

					closeSocket();
				}

				if (length > 0) {
					byte[] buffer = new byte[length];
					sock.getInputStream().read(buffer);

					comListener.onRecv(
							new String(buffer, Charset.forName("UTF-8")),
							player.position);

				} else {
					Thread.sleep(10);
				}

				if (!sock.isConnected()) {
					Log.d(CONSTANTS.TAG, "SocketReply notConnected : "
							+ player.name);
					closeSocket();
				}
			}
		} catch (IOException | InterruptedException e) {
			Log.d(CONSTANTS.TAG, "SocketReply died :" + e.getMessage());
			closeSocket();
		}

	}

	public void send(String msg) {
		OutputStream outputStream;
		try {
			outputStream = sock.getOutputStream();

			byte[] message = msg.getBytes(Charset.forName("UTF-8"));

			outputStream.write(convertToBytes(message.length));
			outputStream.write(message);

			Log.d(CONSTANTS.TAG, "SocketReply [" + id
					+ "] sent a message of length [" + message.length + "]");

		} catch (IOException e) {
			Log.d(CONSTANTS.TAG, "PrintStream error e: " + e.getMessage());
		}

	}

	public void closeSocket() {
		comListener.onDisconnect(player);
		try {
			sock.close();
		} catch (IOException e1) {
			// e1.printStackTrace();
		}
	}

	public byte[] convertToBytes(int i) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(i);
		return b.array();
	}

	public int convertFromBytes(byte[] b) {
		return ByteBuffer.wrap(b).getInt();
	}

}