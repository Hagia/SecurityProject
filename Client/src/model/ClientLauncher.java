package model;

import java.io.IOException;

public final class ClientLauncher {

    private final static int PORT = 8080;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        try {
			if (args.length == 2) {
				String hostName = args[0];
                int portNumber = Integer.parseInt(args[1]);
                new Client(portNumber, hostName).startClient();
			} else if (args.length == 0)
				// use defaults if no host name and port number are provided.
				new Client(PORT, HOST).startClient(); 
			else
				throw new IllegalArgumentException();
		} catch (IllegalArgumentException | IOException  e) {
			System.out.println("Usage: java ClientSender [hostName portNumber]");
			
        }
        
        
		
	}
    
}