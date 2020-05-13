package util;

import java.io.File;
import java.security.MessageDigest;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.Exception;


public enum HashUtilities {

    MD5("MD5"), SHA1("SHA1"), SHA256("SHA-256"), SHA512("SHA-512");

    private String name;

    private HashUtilities(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance(getName());
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return digest.digest();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}