public class ServerController{
    private KeyGenerator generator;
    private Server server;
    private File recieved;
    private KetPair keyPair;

    public ServerController(){

    }

    //Creates a pair of private and public key and send de public key to the client.
    public void connectionStarted(){
        
    }

    //From a raw date tranference creates a file and decrypts it with the previously created private key.
    public void createFile(Byte[] rawData){

    }

    //Compare the SHA-1 sum send from the client with the SHA-1 sum of the file decripted
    public void verifyFileIntegrity(long shaSum){

    }
}