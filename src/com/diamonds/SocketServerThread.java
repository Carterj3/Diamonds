package com.diamonds;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

import org.rosehulman.edu.carterj3.Player;

import android.util.Log;

public class SocketServerThread extends Thread {

	public static SocketServerThread globalSocket = null;
	public static TreeMap<Integer, Player> globalMap = new TreeMap<Integer,Player>();
	
	private ServerSocket serverSocket;
	OnCommunication comListener;
	private int id = 1;

	public SocketServerThread(OnCommunication comListener) {
		this.comListener = comListener;
		SocketServerThread.globalSocket = this;
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