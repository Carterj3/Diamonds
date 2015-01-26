package com.diamonds;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
			int availBytes = sock.getInputStream().available();
			if (availBytes > 0) {
				final byte[] buffer = new byte[availBytes];
				sock.getInputStream().read(buffer);

				comListener.onRecv(new String(buffer), 0);

			} else {
				Thread.sleep(10);
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
			printStream.close();
		} catch (IOException e) {
		}

	}

}
