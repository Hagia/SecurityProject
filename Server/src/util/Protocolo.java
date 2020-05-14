package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Protocolo {

	public static final int KEY_SIZE_AES = 128;
	
	public static void imprimirArregloBytes(String msj, byte[] arregloBytes) {
		System.out.println(msj);
		System.out.println("Nùmero : " + arregloBytes.length + " de bytes.");
		StringBuilder resultado = new StringBuilder();
		for (int i = 0; i < arregloBytes.length; i++) {
			resultado.append(String.format("%02x", arregloBytes[i]));
			if ((i + 1) % 16 == 0)
				resultado.append("\n");
			else if ((i + 1) % 2 == 0)
				resultado.append(" ");
		}
		System.out.println(resultado.toString());	
	}
	
	
	public static void transferirBytes(InputStream fuente, OutputStream destino) throws IOException {
		byte[] buffer = new byte[1024];
		while(true) {
			int cantidadAleer = fuente.read(buffer);
			if (cantidadAleer == -1) break;
			destino.write(buffer,0,cantidadAleer);
		}
	}
	
	public static void transferirBytes(InputStream fuente, OutputStream destino,long tamano) throws IOException {
		byte[] buffer = new byte[1024];
		long faltante = tamano;
		while(true) {
			if (faltante == 0) break;
			
			int cantidadALeer = fuente.read(buffer,0,(int) faltante);
			if (cantidadALeer == -1) break;
			destino.write(buffer,0,cantidadALeer);
			faltante -= cantidadALeer;
		}
	}
	
	
	
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
				encabezado.deleteCharAt(encabezado.length()-1);
				break;
			}
			miArreglo.remove(0); // keep track of only the recent 2 bytes
		}
		if (encabezado.length() == 0) return null;
		ArrayList<String> partesEncabezado = new ArrayList<String>();
		Scanner scanner = new Scanner(encabezado.toString());
		scanner.useDelimiter("\n");
		while (scanner.hasNext()) {
			partesEncabezado.add(scanner.next());
		}
		scanner.close();
		return partesEncabezado;
	}

	public static void enviarMensaje(OutputStream flujoSalida, byte[] mensaje){

	}
}
