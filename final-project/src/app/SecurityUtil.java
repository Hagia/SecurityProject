package app;

public class SecurityUtil {

    public final static int KEY_LENGTH = 1024;

    public static KeyPair KetGenerator(){
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "SUN");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(KEY_LENGTH, random);
        return keyGen.generateKeyPair();
    }

    public static void encrypt(){

    }

    public static void decrypt(){
        
    }
}