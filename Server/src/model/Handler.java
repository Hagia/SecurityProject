package model;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

import util.HashUtilities;
import util.ProtocolUtilities;
import util.SecurityUtilities;

public class Handler extends Thread {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private KeyPair keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String fileChecksum;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void setKeyPair() throws NoSuchAlgorithmException {
        keyPair = SecurityUtilities.RSAKeyPairGenerator();
    }

    private void sendPublicKey() throws IOException {
        StringBuilder messageHeader = new StringBuilder();
        messageHeader.append("PUBLIC KEY\n");

        PublicKey pk = keyPair.getPublic();

        messageHeader.append(pk.getEncoded().length + "\n\n");
        out.write(messageHeader.toString().getBytes("ASCII"));
        out.write(pk.getEncoded());
        out.flush();
    }

    private void sendErrorMessage(String msg) {
        try {
            msg = "ERROR\n" + msg + "\n\n";
            out.write(msg.getBytes("ASCII"));
        } catch (IOException e) {
            System.out.println("Failed to send an error message to client.");
            System.exit(1);
        }
    }

    private byte[] readAndDecryptAesKey() throws GeneralSecurityException, IOException {
        // read the encrypted AES key from the socket
        byte[] encryptedAesKey = new byte[ProtocolUtilities.KEY_SIZE_AES * 2];
        in.read(encryptedAesKey);
        // put the private RSA key in the appropriate data structure
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);
        // Decipher the AES key using the private RSA key
        Cipher pkCipher = Cipher.getInstance("RSA");
        pkCipher.init(Cipher.DECRYPT_MODE, privateKey);
        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encryptedAesKey),
                pkCipher);
        byte[] aesKey = new byte[ProtocolUtilities.KEY_SIZE_AES / 8];
        cipherInputStream.read(aesKey);
        cipherInputStream.close();
        return aesKey;
    }

    private String scanLineFromCipherStream(CipherInputStream cstream) throws IOException {
        StringBuilder line = new StringBuilder();
        char c;
        while ((c = (char) cstream.read()) != '\n') {
            line.append(c);
        }
        return line.toString();
    }

    private File receiveDecryptFile(byte[] aesKey) throws GeneralSecurityException, IOException {
        Cipher aesCipher = Cipher.getInstance("AES");
        SecretKeySpec aeskeySpec = new SecretKeySpec(aesKey, "AES");
        aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
        CipherInputStream cipherInputStream = new CipherInputStream(in, aesCipher);
        String fileName = scanLineFromCipherStream(cipherInputStream);
        String fileSize = scanLineFromCipherStream(cipherInputStream);
        File receivedFile = new File(fileName.toString());
        FileOutputStream foStream = new FileOutputStream(receivedFile);
        ProtocolUtilities.sendBytes(cipherInputStream, foStream, Long.parseLong(fileSize));
        foStream.flush();
        foStream.close();
        return receivedFile;
    }

    private String getChecksum() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String sha = br.readLine();
        return sha;
    }

    public void run() {
        String command;
        try {
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            ArrayList<String> headerParts = ProtocolUtilities.consumeAndBreakHeader(in);
            command = headerParts.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Connection to client dropped.");
            return;
        } catch (NullPointerException e) {
            System.err.println("Unable to read command from client");
            return;
        }
        switch (command) {
            case "GET PUBLIC KEY":
                try {
                    setKeyPair();
                    sendPublicKey();
                    System.out.println("Sent public key!");
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.err.println("Connection to client dropped. Failed to send public key.");
                }
                break;
            case "SHA":
                try {
                    fileChecksum = getChecksum();
                    // System.out.println("Recieved SHA-256 Checksum:" + fileChecksum);
                } catch (IOException e) {
                    System.err.println("Connection to client dropped. Failed to send public key.");
                }
                break;
            case "FILE TRANSFER":
                try {
                    byte[] aesKey = readAndDecryptAesKey();
                    File file = receiveDecryptFile(aesKey);

                    Optional<String> extension = Optional.ofNullable(file.getName())
                        .filter(f -> f.contains("."))
                        .map(f -> f.substring(file.getName().lastIndexOf(".") + 1));

                    String dir = System.getProperty("user.dir") + "/testData";

                    BufferedWriter bw = new BufferedWriter(new PrintWriter(new File(dir + "/recievedFile." + extension.get())));
                    BufferedReader br = new BufferedReader(new FileReader(file));

                    br.transferTo(bw);
                    br.close();
                    bw.close();

                    System.out.println("Received File");
                    System.out.println("Name: " + file.getName());
                    System.out.println("Size:" + file.length());
                    System.out.println("Received SHA256 Checksum:" + fileChecksum);
                    String check = SecurityUtilities.encodeHexString(HashUtilities.SHA256.checksum(new File(file.getName())));
                    System.out.println("Calculated SHA256 Checksum:" + check);
                    if (check.equals(fileChecksum)) {
                        System.out.println("File intact");
                        out.write("SUCCESS\nsuccessful transmission\n\n".getBytes("ASCII"));
                        out.flush();
                        socket.close();
                    } else {
                        System.out.println("File is corrupted");
                        out.write("FAIL\nunsuccessful transmission\n\n".getBytes("ASCII"));
                        out.flush();
                        socket.close();
                    }
                } catch (GeneralSecurityException e) {
                    sendErrorMessage("Failed to decrypt AES key and/or file content.");
                    System.err.println("Server failed to decrypt AES key and/or file content.");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Connection to client dropped.");
                    return;
                }
                break;
            default:
                sendErrorMessage("INVALID COMMAND");
                System.out.println("Invalid command detected: " + command);
        }
    }
}