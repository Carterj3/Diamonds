package com.diamonds;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import android.util.Log;

public class NonHostSocket extends Thread {

	public static NonHostSocket globalSocket = null;

	OnCommunication comListener;
	Socket sock;
	String ip;
	String name;

	public NonHostSocket(OnCommunication comListener, String mIp, String name) {
		this.comListener = comListener;
		this.ip = mIp;
		this.name = name;

		globalSocket = this;
	}

	@Override
	public void run() {

		try {
			sock = new Socket();
			sock.connect(new InetSocketAddress(ip, CONSTANTS.SOCKET_Port));
			Log.d(MainActivity.tag, "NonHostSocket connected");

			send(CONSTANTS.SOCKET_SendUsername + this.name);
		} catch (IOException e1) {
			Log.e(MainActivity.tag, "NonHostSocket Creation Error m [" + name
					+ "]", e1);
			closeSocket();
		}

		try {
			int tries = 10;
			while (true) {

				try {
					byte[] lengthBuffer = new byte[4];
					sock.getInputStream().read(lengthBuffer);
					int length = convertFromBytes(lengthBuffer);

					Log.d(MainActivity.tag,
							"NonHostSocket ["
									+ this.name
									+ "] reading:"
									+ length
									+ "|"
									+ new String(lengthBuffer, Charset
											.forName("UTF-8")));
					if (length == 0) {

						closeSocket();
					}

					if (length > 1000) {
						sock.getInputStream().skip(0);
						Log.d(MainActivity.tag, "NonHostSocket  [" + this.name
								+ "] received too much data: " + length);

					} else if (length > 0) {
						byte[] buffer = new byte[length];
						sock.getInputStream().read(buffer);

						comListener
								.onRecv(new String(buffer, Charset
										.forName("UTF-8")), 0);

					} else {
						Thread.sleep(10);
					}

					if (!sock.isConnected()) {
						closeSocket();
					}

				} catch (IOException | InterruptedException e) {
					if (tries == 0) {
						throw e;
					}
					tries--;
					Log.e(MainActivity.tag, "NonHostSocket err [" + name + "]",
							e);
				}

			}
		} catch (IOException | InterruptedException e) {
			Log.e(MainActivity.tag, "A nonHostSocket Died [" + name + "]", e);
		}
	}

	public void send(String msg) {
		OutputStream outputStream;
		try {
			outputStream = sock.getOutputStream();

			byte[] message = msg.getBytes(Charset.forName("UTF-8"));

			outputStream.write(convertToBytes(message.length));
			outputStream.write(message);

			Log.d(MainActivity.tag, "NonHostSocket [" + name
					+ "] sent a message of length [" + message.length + "]");

		} catch (IOException e) {
			Log.e(MainActivity.tag, "NonHostSocket send [" + name + "]", e);
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

	public void closeSocket() {
		comListener.onDisconnect(null);
		try {
			sock.close();
		} catch (IOException e1) {
			// Log.e(MainActivity.tag, "NonHostSocket close [" + name + "]",
			// e1);
		}
	}

}
