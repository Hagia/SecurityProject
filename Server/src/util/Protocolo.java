package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Clase con metodos utiles para el correcto funcionamiento de la clase
 * Servidor, como transferir bytes de buffer a otro o consumir el encabezado
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
			int cantidadAleer = fuente.read(buffer);
			if (cantidadAleer == -1)
				break;
			destino.write(buffer, 0, cantidadAleer);
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

			int cantidadALeer = fuente.read(buffer, 0, (int) faltante);
			if (cantidadALeer == -1)
				break;
			destino.write(buffer, 0, cantidadALeer);
			faltante -= cantidadALeer;
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
	public static ArrayList<String> segmentarYConsumirEncabezado(InputStream entrada) throws IOException {
		ArrayList<Character> miArreglo = new ArrayList<>();
		StringBuilder encabezado = new StringBuilder();
		int c;
		while ((c = entrada.read()) != -1) {
			miArreglo.add((char) c);
			encabezado.append((char) c);
			if (miArreglo.size() != 2) // pipeline not full
				continue;
			if (miArreglo.get(0) == '\n' && miArreglo.get(1) == '\n') {
				encabezado.deleteCharAt(encabezado.length() - 1);
				break;
			}
			miArreglo.remove(0); // keep track of only the recent 2 bytes
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

	/**
	 * Transfiere los datos del flujo de salida un arreglo de bytes
	 * @param flujoSalida Flujo de salida con datos
	 * @param mensaje Datos transferidos en Bytes
	 */
	public static void enviarMensaje(OutputStream flujoSalida, byte[] mensaje) {

	}
}
