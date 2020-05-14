package model;

import java.net.ServerSocket;
import java.security.KeyPair;

public class ManejadorServidor {
    private KeyPair parClaves;
    private String checksumArchivo;
    
    public ManejadorServidor(int port) throws Exception{
        System.out.println("El servidor esta corriendo.");
		ServerSocket escuchador = new ServerSocket(port);
		try {
			while (true) {
				Servidor serv = new Servidor(escuchador.accept(), this);
				serv.start();
			}
		} finally {
			escuchador.close();
		}
    }
    
    public void definirParClaves(KeyPair pParClaves){
        this.parClaves = pParClaves;
    }

    public KeyPair obtenerParClaves(){
        return this.parClaves;
    }

    public void definirChecksum(String checksum){
        this.checksumArchivo = checksum;
    }

    public String obtenerChecksum(){
        return this.checksumArchivo;
    }

}