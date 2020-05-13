package model;

public class ServerLauncher {
	private static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		new ServerManager(PORT);
	}
}