package com.diamonds;

public class CONSTANTS {
	public static final String SOCKET_GetUsernames = "GetUsernames";
	public static final String SOCKET_GetUsername = "GetUsername";
	public static final String SOCKET_SendUsername = "SendUsername:";
	public static final String SOCKET_SendUsernames = "SendUsernames:";
	public static final String SOCKET_SendChat = "SendChat:";

	public static final int SOCKET_Port = 8080;
	
	public static boolean strncmp(String actual, String expected) {
		if (actual.length() < expected.length()) {
			return false;
		}
		return actual.substring(0, expected.length()).equals(expected);
	}
}
