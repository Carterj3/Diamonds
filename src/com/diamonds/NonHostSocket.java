package com.diamonds;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.util.Log;

public class NonHostSocket extends Thread {

	OnCommunication comListener;
	Socket sock;
	String ip;

	public NonHostSocket(OnCommunication comListener, String mIp) {
		this.comListener = comListener;
		this.ip = mIp;

	}

	@Override
	public void run() {
		try {
			sock = new Socket();
			sock.connect(new InetSocketAddress(ip, CONSTANTS.SOCKET_Port));
			Log.d(MainActivity.tag, "NonHostSocket connected");
			send(CONSTANTS.SOCKET_GetUsernames);
			Log.d(MainActivity.tag, "NonHostSocket asked names");
		} catch (Exception e) {
			Log.d(MainActivity.tag,
					"NonHostSocket Creation Error m: " + e.getMessage());
		}
		try {
			int tries = 10;
			while (true) {

				try {
					int availBytes = sock.getInputStream().available();
					if (availBytes > 0) {
						final byte[] buffer = new byte[availBytes];
						sock.getInputStream().read(buffer);

						comListener.onRecv(new String(buffer), 0);

					} else {
						Thread.sleep(10);
					}
				} catch (Exception e) {
					if (tries == 0) {
						throw e;
					}
					tries--;
					Log.d(MainActivity.tag,
							"NonHostSocket err e:" + e.getMessage());
				}

			}
		} catch (Exception e) {
			Log.d(MainActivity.tag, "A nonHostSocket Died m:" + e.getMessage());
		}
	}

	public void send(String msg) {
		OutputStream outputStream;
		try {
			outputStream = sock.getOutputStream();
			PrintStream printStream = new PrintStream(outputStream);
			printStream.print(msg);
		} catch (IOException e) {
			Log.d(MainActivity.tag, "NonHostSocket send e:" + e.getMessage());
		}

	}

}
