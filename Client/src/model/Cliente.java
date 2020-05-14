package model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

import util.Protocolo;
import util.Seguridad;

/**
 * Clase que representa el cliente
 * 
 * @author Juan
 *
 */
public class Cliente {

	private String nombreHost;
	private int numeroPuerto;
	private Socket socket;

	public Cliente(int pPuerto, String pNombrePuerto) throws UnknownHostException, IOException {
		this.nombreHost = pNombrePuerto;
		this.numeroPuerto = pPuerto;
	}

	/**
	 * Metodo que obtiene el socket del cliente, si existe lo envia, de lo contrario
	 * lo crea.
	 * 
	 * @return socket: el socket del cliente.
	 */
	public Socket obtenerSocket() {
		try {
			if (socket != null && !socket.isClosed()) {
				return this.socket;
			}
			socket = new Socket(nombreHost, numeroPuerto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return socket;
	}

	/**
	 * Inicializa todo lo relacionado con el cliente, obtiene la clave publica del
	 * servidor y llama al metodo que encripta y envia el archivo al servidor.
	 * Notifica al usuario como finalizo el envio.
	 */
	public void iniciarCliente() {
		System.out.println("Usando el n�mero de host: " + nombreHost + " y puerto n�mero: " + numeroPuerto + "...");
		byte[] rsaClavePublica;
		File[] directorioActual = new File(Paths.get(".").toAbsolutePath().toString()).listFiles();
		System.out.println("Archivos disponible en el directorio actual:");
		for (File f : directorioActual) {
			String nombreArchivo = f.getName();
			System.out.println(nombreArchivo);
		}
		try {
			rsaClavePublica = obtenerClavePublica();
			Scanner scanner = new Scanner(System.in);
			System.out.println("Ingrese el nombre del archivo a enviar: ");
			String nombreArchivo = scanner.next();
			enviarSHA(nombreArchivo);

			enviarArchivo(rsaClavePublica, new File(nombreArchivo));
			boolean esExitoso = confirmarRecepcion();
			if (esExitoso) {
				System.out.println("Envio de archivo exitoso!");
			} else {
				System.out.println("Archivo no enviado.");
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.err.println("Archivo no encontrado.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Hay un error en la conexion con el servidor.");
		} catch (GeneralSecurityException e) {
			System.err.println("Error desconocido.");
		}
	}

	/**
	 * Se encarga encriptar el archivo seleccionado con una clave publica cada
	 * 
	 * @param clavePublica
	 *            Clave publica dada por el servidor
	 * @param archivo
	 *            Archivo que sera encriptado usando la clave publica
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private void enviarArchivo(byte[] clavePublica, File archivo)
			throws FileNotFoundException, IOException, GeneralSecurityException {
		System.out.println("Enviando archivo...");
		Socket socket = obtenerSocket();
		BufferedOutputStream salida = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream entrada = new BufferedInputStream(socket.getInputStream());

		salida.write("FILE TRANSFER\n\n".getBytes("ASCII"));

		String archivoNombreYTamano = new String(archivo.getName() + "\n" + archivo.length() + "\n");
		ByteArrayInputStream infoArchivo = new ByteArrayInputStream(archivoNombreYTamano.getBytes("ASCII"));

		Cipher cifradorRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clavePublica));
		cifradorRSA.init(Cipher.ENCRYPT_MODE, pk);
		CipherOutputStream flujoSalidaCifrador = new CipherOutputStream(salida, cifradorRSA);
		Protocolo.transferirBytes(infoArchivo, flujoSalidaCifrador);

		FileInputStream flujoArchivoEntrada = new FileInputStream(archivo);
		Protocolo.transferirBytes(flujoArchivoEntrada, flujoSalidaCifrador);
		flujoSalidaCifrador.close();

		ArrayList<String> respuestaServidor = null;

		salida.write(cifradorRSA.doFinal());
		salida.write("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n".getBytes("ASCII"));

		System.out.println(!socket.isClosed());
		if (!socket.isClosed()) {
			salida.flush();
			respuestaServidor = Protocolo.segmentacionYConsumoEncabezado(entrada);
			socket.close();
		}
	}

	/**
	 * Permite obtener la clave publica del servidor
	 * 
	 * @return Clave publica en bytes
	 * @throws IOException
	 */
	private byte[] obtenerClavePublica() throws IOException {
		Socket socket = obtenerSocket();
		BufferedOutputStream salida = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream entrada = new BufferedInputStream(socket.getInputStream());
		salida.write("GET PUBLIC KEY\n\n".getBytes("ASCII"));
		salida.flush();
		ArrayList<String> partesEncabezado = Protocolo.segmentacionYConsumoEncabezado(entrada);
		if (!partesEncabezado.get(0).equals("PUBLIC KEY")) {
			System.err.println("Fallo obteniendo la clave. El servidor responde lo siguiente:");
			for (String msj : partesEncabezado)
				System.err.println(msj);
			System.exit(1);
		}
		int tamanoClave = Integer.parseInt(partesEncabezado.get(1));
		byte[] clavePublica = new byte[tamanoClave];
		entrada.read(clavePublica);
		socket.close();
		return clavePublica;
	}

	/**
	 * Envia el SHA del nombre del archivo que se envia (Proceso Checksum)
	 * 
	 * @param nombreArchivo
	 *            Nombre del archivo que se envia
	 * @throws IOException
	 */
	private void enviarSHA(String nombreArchivo) throws IOException {
		System.out.println("Enviando SHA");
		Socket socket = obtenerSocket();
		BufferedOutputStream salida = new BufferedOutputStream(socket.getOutputStream());
		salida.write("SHA\n\n".getBytes("ASCII"));
		salida.flush();
		PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
		String verificacion = Seguridad.codificadorHex(Seguridad.checksum(new File(nombreArchivo)));
		escritor.println(verificacion);
		escritor.flush();
		escritor.close();
		socket.close();
	}

	/**
	 * Permite confirmar la recepcion del archivo por parte del servidor
	 * 
	 * @return El servidor recibio o no el archivo que se le envio
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private boolean confirmarRecepcion() throws UnsupportedEncodingException, IOException {
		Socket socket = obtenerSocket();
		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		out.write("CONFIRMAR\n\n".getBytes("ASCII"));
		out.flush();
		ArrayList<String> headerParts = Protocolo.segmentacionYConsumoEncabezado(in);
		if (headerParts.get(0).equals("CORRECTO")) {
			return true;
		}
		return false;

	}
}