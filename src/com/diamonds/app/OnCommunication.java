package com.diamonds.app;

import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;

public interface OnCommunication {

	public void onRecv(String msg, int id);

	public Player onConnection(SocketServerReplyThread newSocket, int id) throws PlayerNotFoundException;
	
	public void onDisconnect(Player player);
}
