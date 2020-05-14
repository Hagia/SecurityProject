package model;

public class ServerLauncher {
	private static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		if(args.length == 1){
			new ServerManager(Integer.parseInt(args[0]));
		}else{
			new ServerManager(PORT);
		}

	}
}