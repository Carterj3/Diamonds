package com.diamonds;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import android.util.Log;

public class SocketServerReplyThread extends Thread {

	private Socket sock;
	private OnCommunication comListener;
	private int id;

	SocketServerReplyThread(OnCommunication comListener, Socket socket, int id) {
		sock = socket;
		this.comListener = comListener;
		this.id = id;
		
		comListener.onConnection(this, id);
	}

	@Override
	public void run() {

		try {
			while (true) {
				int availBytes = sock.getInputStream().available();
				if (availBytes > 0) {
					final byte[] buffer = new byte[availBytes];
					sock.getInputStream().read(buffer);

					comListener.onRecv(new String(buffer), id);

				} else {
					Thread.sleep(10);
				}
			}
		} catch (Exception e) {
			Log.d(MainActivity.tag, "SocketReply died :" + e.getMessage());
		}

	}

	public void send(String msg) {
		OutputStream outputStream;
		try {
			outputStream = sock.getOutputStream();
			PrintStream printStream = new PrintStream(outputStream);
			printStream.print(msg);
		} catch (IOException e) {
			Log.d(MainActivity.tag, "PrintStream error e: " + e.getMessage());
		}

	}

}