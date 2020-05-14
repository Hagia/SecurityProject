package model;

import java.net.ServerSocket;
import java.security.KeyPair;

/**
 * Clase que se encarga de iniciar la instancia del servidor, maneja la
 * funcionalidad de la claves y del cheksum
 * 
 * @author Juan
 *
 */
public class ManejadorServidor {
	private KeyPair parClaves;
	private String checksumArchivo;
	private String resultadoComparacion;

	/**
	 * Crea la instancia del servidor con su respectivo Socket
	 * 
	 * @param port
	 *            Puerto por el cual se establecera la comunicacion
	 * @throws Exception
	 */
	public ManejadorServidor(int port) throws Exception {
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

	/**
	 * Permite definir el objeto que genera las claves
	 * 
	 * @param pParClaves
	 *            Generador de claves
	 */
	public void definirParClaves(KeyPair pParClaves) {
		this.parClaves = pParClaves;
	}

	/**
	 * Obtiene el objeto que genera las claves
	 * 
	 * @return Objeto generador de claves
	 */
	public KeyPair obtenerParClaves() {
		return this.parClaves;
	}

	/**
	 * Define el checksum
	 * 
	 * @param checksum
	 *            Checksum
	 */
	public void definirChecksum(String checksum) {
		this.checksumArchivo = checksum;
	}

	/**
	 * Obtiene el checksum
	 * 
	 * @return checksum
	 */
	public String obtenerChecksum() {
		return this.checksumArchivo;
	}

	/**
	 * Define el resultado de la comparacion
	 * 
	 * @param comparacion
	 *            resultado de la comparacion
	 */
	public void setCompare(String comparacion) {
		this.resultadoComparacion = comparacion;

	}

	/**
	 * Obtiene el resultado de la comparacion
	 * 
	 * @return resultado de la comparacion
	 */
	public String getCompare() {
		return this.resultadoComparacion;
	}

}