package model;

import java.net.ServerSocket;
import java.security.KeyPair;

public class ServerManager {
    private KeyPair keyPair;
    private String fileChecksum;
    
    public ServerManager(int port) throws Exception{
        System.out.println("The server is running.");
		ServerSocket listener = new ServerSocket(port);
		try {
			while (true) {
				Handler h = new Handler(listener.accept(), this);
				h.start();
			}
		} finally {
			listener.close();
		}
    }
    
    public void setKeyPair(KeyPair keyPair){
        this.keyPair = keyPair;
    }

    public KeyPair getKeyPair(){
        return this.keyPair;
    }

    public void setChecksum(String checksum){
        this.fileChecksum = checksum;
    }

    public String getChecksum(){
        return this.fileChecksum;
    }

}