package com.diamonds;

public interface OnCommunication {

	public void onRecv(String msg, int id);

	public void onConnection(SocketServerReplyThread newSocket, int id);
}
