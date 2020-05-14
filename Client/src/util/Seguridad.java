
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Seguridad{

    public final static String SHA1 = "SHA1";
    public final static String RSA = "RSA";

    public static KeyPair GeneradorParClavesRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator generador = KeyPairGenerator.getInstance(RSA);
        generador.initialize(2048);
        KeyPair pc = generador.generateKeyPair();
        return pc;
    }

    public static byte[] checksum(File archivo) {
        try (InputStream entrada = new FileInputStream(archivo)) {
            MessageDigest dm = MessageDigest.getInstance(SHA1);
            byte[] bloque = new byte[4096];
            int tamano;
            while ((tamano = entrada.read(bloque)) > 0) {
                dm.update(bloque, 0, tamano);
            }
            return dm.digest();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String codificadorHex(final byte[] arregloBytes) {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < arregloBytes.length; i++) {
            buffer.append(converByteAHex(arregloBytes[i]));
        }
        return buffer.toString();
    }

    public static String converByteAHex(final byte numero) {
        final char[] digitosHex = new char[2];
        digitosHex[0] = Character.forDigit((numero >> 4) & 0xF, 16);
        digitosHex[1] = Character.forDigit((numero & 0xF), 16);
        return new String(digitosHex);
    }
}