
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase con la funcionalidad de generar las claves y manejar el checksum
 * 
 * @author Juan
 *
 */
public final class Seguridad {

	public final static String SHA1 = "SHA1";
	public final static String RSA = "RSA";

	/**
	 * Crea un objeto que permite generar el par de claves RSA
	 * 
	 * @return Objeto generador
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generadorParClavesRSA() throws NoSuchAlgorithmException {
		KeyPairGenerator generador = KeyPairGenerator.getInstance(RSA);
		generador.initialize(2048);
		KeyPair pc = generador.generateKeyPair();
		return pc;
	}

	/**
	 * Genera el checksum de un archivo
	 * 
	 * @param archivo
	 *            Archivo el cual se quiere saber su checksum
	 * @return Checksum de archivo, null en caso contrario
	 */
	public static byte[] checksum(File archivo) {
		try (InputStream flujoEntrada = new FileInputStream(archivo)) {
			MessageDigest dm = MessageDigest.getInstance(SHA1);
			byte[] bloque = new byte[4096];
			int tamano;
			while ((tamano = flujoEntrada.read(bloque)) > 0) {
				dm.update(bloque, 0, tamano);
			}
			return dm.digest();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Codificador de byte a hexadecimal
	 * 
	 * @param arregloBytes
	 *            Arreglo que cual se quiere codificar
	 * @return el arreglo codificado
	 */
	public static String codificadorHex(final byte[] arregloBytes) {
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < arregloBytes.length; i++) {
			buffer.append(convertirByteAHex(arregloBytes[i]));
		}
		return buffer.toString();
	}

	/**
	 * Convierte un byte en hexadecimal
	 * 
	 * @param numero
	 *            El byte que se quiere convertir
	 * @return El byte en formato hexadecimal
	 */
	public static String convertirByteAHex(final byte numero) {
		final char[] digitosHex = new char[2];
		digitosHex[0] = Character.forDigit((numero >> 4) & 0xF, 16);
		digitosHex[1] = Character.forDigit((numero & 0xF), 16);
		return new String(digitosHex);
	}
}