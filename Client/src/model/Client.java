package model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import util.ProtocolUtilities;
import util.SecurityUtilities;

public class Client {

	private String hostName;
	private int portNumber;
	private Socket socket;

	public Client(int port, String hostName) throws UnknownHostException, IOException {
		this.hostName = hostName;
		this.portNumber = port;
	}

	public Socket getSocket(){
		try{
			if(socket != null && !socket.isClosed()){
				return this.socket;
			}socket =  new Socket(hostName, portNumber);
		}catch (Exception e){
			e.printStackTrace();
		}return socket;
	}

	public void startClient(){
		System.out.println("Using host name: " + hostName + " and port number: " + portNumber + "...");
		byte[] publicRsaKey, secretAesKey;
		File[] currentDirFiles = new File(Paths.get(".").toAbsolutePath().toString()).listFiles();
		System.out.println("Available files in the current directory:");
		for (File f : currentDirFiles) {
			String fileName = f.getName();
			if (fileName.charAt(0) == '.') // ignore hidden files.
				continue;
			System.out.println(fileName);
		}
		try {
			publicRsaKey = getPublicKey();
			secretAesKey = generateAesKey();
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter the name of the file to send: ");
			String fileName = scanner.next();
			sendSHA(fileName);

			String dir = System.getProperty("user.id");

			boolean isSuccessful = sendFile(publicRsaKey, secretAesKey, new File(fileName));
			if (isSuccessful) {
				System.out.println("File was successfully sent.");
			} else {
				System.out.println("File was not sent.");
				boolean retry = false;
				while (!retry) {
					System.out.println("Retrying to send the file. ");
					retry = sendFile(publicRsaKey, secretAesKey, new File(fileName));
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There was an error connecting to the server.");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Failed to generate AES key.");
		} catch (GeneralSecurityException e) {
			System.err.println("Unknown security error.");
		}
	}

	private void sendEcryptedAesKEY(OutputStream out, byte[] publicKey, byte[] aesKey)
			throws GeneralSecurityException, IOException {
		Cipher pkCipher = Cipher.getInstance("RSA");
		PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
		pkCipher.init(Cipher.ENCRYPT_MODE, pk);
		ByteArrayOutputStream tempByteStream = new ByteArrayOutputStream();
		CipherOutputStream cipherStream = new CipherOutputStream(tempByteStream, pkCipher);
		cipherStream.write(aesKey);
		cipherStream.close();
		tempByteStream.writeTo(out);
	}

	private boolean sendFile(byte[] publicKey, byte[] aesKey, File file)
			throws FileNotFoundException, IOException, GeneralSecurityException {
		Socket socket = getSocket();
		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		// send header and encrypted AES. AES is encrypted using private RSA key.
		out.write("FILE TRANSFER\n\n".getBytes("ASCII"));
		sendEcryptedAesKEY(out, publicKey, aesKey);
		// Encrypt the name of the file and its size using AES and send it over the
		// socket
		String fileNameAndSize = new String(file.getName() + "\n" + file.length() + "\n");
		ByteArrayInputStream fileInfoStream = new ByteArrayInputStream(fileNameAndSize.getBytes("ASCII"));
		SecretKeySpec aeskeySpec = new SecretKeySpec(aesKey, "AES");
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		CipherOutputStream cipherOutStream = new CipherOutputStream(out, aesCipher);
		ProtocolUtilities.sendBytes(fileInfoStream, cipherOutStream);
		// send the the actual file itself and append some bytes so cipher would know
		// it's the end of the file
		FileInputStream fileStream = new FileInputStream(file);
		ProtocolUtilities.sendBytes(fileStream, cipherOutStream);

		out.write(aesCipher.doFinal());
		out.write("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n".getBytes("ASCII"));
		out.flush();
		ArrayList<String> serverResponse = ProtocolUtilities.consumeAndBreakHeader(in);
		socket.close();
		if (!serverResponse.get(0).equals("SUCCESS")) {
			System.err.println("Failed to send file. The Server responded with the following:");
			for (String msg : serverResponse)
				System.err.println(msg);
			return false;
		}
		return true;
	}

	private byte[] getPublicKey() throws IOException {
		Socket socket = getSocket();
		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		out.write("GET PUBLIC KEY\n\n".getBytes("ASCII"));
		out.flush();
		ArrayList<String> headerParts = ProtocolUtilities.consumeAndBreakHeader(in);
		if (!headerParts.get(0).equals("PUBLIC KEY")) {
			System.err.println("Failed to obtain public key. The Server responded with the following:");
			for (String msg : headerParts)
				System.err.println(msg);
			System.exit(1);
		}
		int keySize = Integer.parseInt(headerParts.get(1));
		byte[] publicKey = new byte[keySize];
		in.read(publicKey);
		socket.close();
		return publicKey;
	}

	private void sendSHA(String fileName) throws IOException {
		Socket socket = getSocket();
		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
		out.write("SHA\n\n".getBytes("ASCII"));
		out.flush();
		PrintWriter outS = new PrintWriter(socket.getOutputStream(), true);
		String check = SecurityUtilities.encodeHexString(SecurityUtilities.checksum(new File(fileName)));
		outS.println(check);
		outS.close();
		socket.close();
	}

	private byte[] generateAesKey() throws NoSuchAlgorithmException {
		byte[] secretAesKey = null;
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(ProtocolUtilities.KEY_SIZE_AES); // AES key length 128 bits (16 bytes)
		secretAesKey = kgen.generateKey().getEncoded();
		return secretAesKey;
	}

	
}