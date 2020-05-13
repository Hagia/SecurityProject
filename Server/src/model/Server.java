package model;

import java.net.ServerSocket;

public class Server {
	private static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running.");
		ServerSocket listener = new ServerSocket(PORT);
		try {
			while (true) {
				Handler h = new Handler(listener.accept());
				h.start();
			}
		} finally {
			listener.close();
		}
	}
}