package com.diamonds;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class SocketServerThread extends Thread {

	private ServerSocket serverSocket;
	private OnCommunication comListener;
	private int id = 0;

	public SocketServerThread(OnCommunication comListener) {
		this.comListener = comListener;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(CONSTANTS.SOCKET_Port);

			while (true) {
				Socket socket = serverSocket.accept();
				SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
						comListener, socket, id);
				socketServerReplyThread.start();
				id++;
			}
		} catch (Exception e) {
			Log.d(MainActivity.tag, "ServerSocket died.");
		}
	}

}