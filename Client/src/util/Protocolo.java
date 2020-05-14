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
		System.out.println("Tamaño: " + arregloBytes.length + " bytes.");
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
			int readAmount = fuente.read(buffer);
			if (readAmount == -1) break;
			destino.write(buffer,0,readAmount);
		}
	}

	public static void transferirBytes(InputStream fuente, OutputStream destino,long tamano) throws IOException {
		byte[] buffer = new byte[1024];
		long faltante = tamano;
		while(true) {
			if (faltante == 0) break;
			int cantidadLeida = fuente.read(buffer,0,(int) faltante);
			if (cantidadLeida == -1) break;
			destino.write(buffer,0,cantidadLeida);
			faltante -= cantidadLeida;
		}
	}
	
	
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
				encabezado.deleteCharAt(encabezado.length()-1);
				break;
			}
			myArreglo.remove(0);
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
}
