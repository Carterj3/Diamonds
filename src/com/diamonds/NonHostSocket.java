package com.diamonds;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.provider.SyncStateContract.Constants;
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
			
			send(CONSTANTS.SOCKET_SendUsername+this.name);
		} catch (Exception e) {
			Log.d(MainActivity.tag,
					"NonHostSocket Creation Error m: " + e.getMessage());
		}
		try {
			int tries = 10;
			while (true) {

				try {
					byte[] lengthBuffer = new byte[4];
					sock.getInputStream().read(lengthBuffer);
					int length = convertFromBytes(lengthBuffer);
					
					Log.d(MainActivity.tag,"SocketReply reading:"+length);
					
					if (length > 0) {
						final byte[] buffer = new byte[length];
						sock.getInputStream().read(buffer);

						comListener.onRecv(new String(buffer), 0);

					} else {
						Thread.sleep(10);
					}
					
					if(!sock.isConnected()){
						closeSocket();
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
			outputStream.write(convertToBytes(msg.length()));
			(new PrintStream(outputStream)).print(msg);
			
		} catch (IOException e) {
			Log.d(MainActivity.tag, "NonHostSocket send e:" + e.getMessage());
		}

	}
	
	public byte[] convertToBytes(int i){
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(i);
		return b.array();
	}
	
	public int convertFromBytes(byte[] b){
		return ByteBuffer.wrap(b).getInt();
	}
	
	public void closeSocket(){
		comListener.onDisconnect(null);
		try {
			sock.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
