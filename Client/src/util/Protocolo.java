package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Clase con metodos utiles para el correcto funcionamiento de la clase cliente,
 * como transferir bytes de buffer a otro o consumir el encabezado
 * 
 * @author Juan
 *
 */
public class Protocolo {

	public static final int KEY_SIZE_AES = 128;


	/**
	 * Transfiere los datos de un flujo de entrada a un flujo de salida
	 * 
	 * @param fuente
	 *            Flujo de entrada
	 * @param destino
	 *            Flujo de salida
	 * @throws IOException
	 */
	public static void transferirBytes(InputStream fuente, OutputStream destino) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readAmount = fuente.read(buffer);
			if (readAmount == -1)
				break;
			destino.write(buffer, 0, readAmount);
		}
	}

	/**
	 * Transfiere los datos de un flujo de entrada a un flujo de salida
	 * 
	 * @param fuente
	 *            Flujo de entrada
	 * @param destino
	 *            Flujo de salida
	 * @param tamano
	 *            Tamano de flujo de entrada
	 * @throws IOException
	 */
	public static void transferirBytes(InputStream fuente, OutputStream destino, long tamano) throws IOException {
		byte[] buffer = new byte[1024];
		long faltante = tamano;
		while (true) {
			if (faltante == 0)
				break;
			int cantidadLeida = fuente.read(buffer, 0, (int) faltante);
			if (cantidadLeida == -1)
				break;
			destino.write(buffer, 0, cantidadLeida);
			faltante -= cantidadLeida;
		}
	}

	/**
	 * Divide el encabezado para obtener la instruccion del usuario
	 * 
	 * @param in
	 *            Flujo de entrada con las intrucciones del usuario y otros datos
	 *            concerniente con el archivo que se envia
	 * @return Arreglo con las intrucciones divididas
	 * @throws IOException
	 */
	public static ArrayList<String> segmentacionYConsumoEncabezado(InputStream in) throws IOException {
		ArrayList<Character> myArreglo = new ArrayList<>();
		StringBuilder encabezado = new StringBuilder();
		int c;
		while ((c = in.read()) != -1) {
			myArreglo.add((char) c);
			encabezado.append((char) c);
			if (myArreglo.size() != 2)
				continue;
			if (myArreglo.get(0) == '\n' && myArreglo.get(1) == '\n') {
				encabezado.deleteCharAt(encabezado.length() - 1);
				break;
			}
			myArreglo.remove(0);
		}
		if (encabezado.length() == 0)
			return null;
		ArrayList<String> partesEncabezado = new ArrayList<String>();
		Scanner scanner = new Scanner(encabezado.toString());
		scanner.useDelimiter("\n");
		while (scanner.hasNext()) {
			partesEncabezado.add(scanner.next());
		}
		scanner.close();
		return partesEncabezado;
	}
}
