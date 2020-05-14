package model;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

import util.Protocolo;
import util.Seguridad;

public class Servidor extends Thread {
    private Socket socket;
    private InputStream entrada;
    private OutputStream salida;
    private ManejadorServidor servidor;

    public Servidor(Socket socket, ManejadorServidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    public void definirParClaves() throws NoSuchAlgorithmException {
        servidor.definirParClaves(Seguridad.generadorParClavesRSA());
    }

    private void enviarClavePublica() throws IOException {
        StringBuilder encabezado = new StringBuilder();
        encabezado.append("PUBLIC KEY\n");

        PublicKey pk = servidor.obtenerParClaves().getPublic();

        encabezado.append(pk.getEncoded().length + "\n\n");
        salida.write(encabezado.toString().getBytes("ASCII"));
        salida.write(pk.getEncoded());
        salida.flush();
    }

    private void enviarMensajeError(String msj) {
        try {
            msj = "ERROR\n" + msj + "\n\n";
            salida.write(msj.getBytes("ASCII"));
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje.");
            System.exit(1);
        }
    }

    private String leerLineaFlujoCifrador(CipherInputStream flujoCifrador) throws IOException {
        StringBuilder linea = new StringBuilder();
        char c;
        while ((c = (char) flujoCifrador.read()) != '\n') {
            linea.append(c);
        }
        return linea.toString();
    }

    private File RecibirYDesencriptarArchivo() throws GeneralSecurityException, IOException {
        Cipher RASCifrador = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        RASCifrador.init(Cipher.DECRYPT_MODE, servidor.obtenerParClaves().getPrivate());
        CipherInputStream flujoEntradaCifrador = new CipherInputStream(entrada, RASCifrador);

        String nombreArchivo = leerLineaFlujoCifrador(flujoEntradaCifrador);
        String tamanoArchivo = leerLineaFlujoCifrador(flujoEntradaCifrador);
        File archivoRecibido = new File(nombreArchivo.toString());
        FileOutputStream flujoSalidaArchivo = new FileOutputStream(archivoRecibido);
        Protocolo.transferirBytes(flujoEntradaCifrador, flujoSalidaArchivo, Long.parseLong(tamanoArchivo));
        flujoSalidaArchivo.flush();
        flujoSalidaArchivo.close();
        
        return archivoRecibido;
    }

    private String obtenerChecksum() throws IOException {
        System.out.println("Receiving SHA-1");
        BufferedReader lector = new BufferedReader(new InputStreamReader(entrada));
        String sha = lector.readLine();
        return sha;
    }

    public void run() {
        String instruccion;
        try {
            entrada = new BufferedInputStream(socket.getInputStream());
            salida = new BufferedOutputStream(socket.getOutputStream());
            ArrayList<String> parteEncabezado = Protocolo.segmentarYConsumirEncabezado(entrada);
            instruccion = parteEncabezado.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Se perdio la conexion con el cliente.");
            return;
        } catch (NullPointerException e) {
            System.err.println("No fue posible leer la instruccion del cliente");
            return;
        }
        switch (instruccion) {
            case "GET PUBLIC KEY":
                try {
                    definirParClaves();
                    enviarClavePublica();
                    System.out.println("Clave publica enviada!");
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.err.println("Se perdio la conexion con el cliente. Fallo en el envio de la clave.");
                }
                break;
            case "SHA":
                try {
                    servidor.definirChecksum(obtenerChecksum());
                   
                } catch (IOException e) {
                    System.err.println("Se perdio la conexion con el cliente.  Fallo en el envio de la clave.");
                }
                break;
            case "FILE TRANSFER":
                try {
                 
                    File archivo = RecibirYDesencriptarArchivo();

                    Optional<String> extension = Optional.ofNullable(archivo.getName())
                        .filter(f -> f.contains("."))
                        .map(f -> f.substring(archivo.getName().lastIndexOf(".") + 1));

                    String directorio = System.getProperty("user.dir") + "/testData";

                    BufferedWriter bufferEscritor = new BufferedWriter(new PrintWriter(new File(directorio + "/recievedFile." + extension.get())));
                    BufferedReader bufferLector = new BufferedReader(new FileReader(archivo));

                    bufferLector.transferTo(bufferEscritor);
                    bufferLector.close();
                    bufferEscritor.close();

                    System.out.println("Archivo Recibido");
                    System.out.println("Nombre: " + archivo.getName());
                    System.out.println("Tamaño:" + archivo.length());
                    System.out.println("Checksum:" + servidor.obtenerChecksum());
                    String verificacion = Seguridad.codificadorHex(Seguridad.checksum(new File(archivo.getName())));
                    System.out.println("Checksum Calculado:" + verificacion);

                    if (verificacion.equals(servidor.obtenerChecksum())) {
                        System.out.println("Archivo recibido correctamente");
                     
                        BufferedOutputStream bufferFlujoSalida = new BufferedOutputStream(salida);
                        bufferFlujoSalida.write("SUCCESS\nsuccessful transmission\n\n".getBytes("ASCII"));
                        bufferFlujoSalida.flush();
                        
                        //bufferFlujoSalida.close();
                        //socket.close();
                    } else {
                        System.out.println("El archivo se corrompio");
                        salida.write("FAIL\nunsuccessful transmission\n\n".getBytes("ASCII"));
                        salida.flush();
                        socket.close();
                    }
                } catch (GeneralSecurityException e) {
                    enviarMensajeError("Fallo desencriptando el archivo.");
                    System.err.println("Fallo desencriptando el archivo.");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("La conexion con el cliente se ha perdido.");
                    return;
                }
                break;
            default:
                enviarMensajeError("INVALID COMMAND");
                System.out.println("Instruccion invalida: " + instruccion);
        }
    }
}