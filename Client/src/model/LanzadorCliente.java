package model;

import java.io.IOException;

public final class LanzadorCliente {

    private final static int PORT = 8080;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        try {
			if (args.length == 2) {
				String nombreHost = args[0];
                int numeroPuerto = Integer.parseInt(args[1]);
                new Cliente(numeroPuerto, nombreHost).iniciarCliente();
			} else if (args.length == 0)
				
				new Cliente(PORT, HOST).iniciarCliente(); 
			else
				throw new IllegalArgumentException();
		} catch (IllegalArgumentException | IOException  e) {
			System.out.println("Usando: lanzador java [nombreHost numeroPuerto]");
			
        }      
		
	}
    
}