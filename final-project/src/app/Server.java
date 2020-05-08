public class Server{
    private int port;
    private ServerSocket server;
    private Socket currentClient;
    private InputStream stdIn;
    private OutputStream sdtOut;


    public Server(String port){
        this.port = port;
        this.socket = new ServerSocket(port);
    }

    public void startListening(){
        Socket s = server.accept();

        stdIn = s.getInputStream();
        sdtOut = s.getOutputStream();
    }

    public void sendToClient(String message){
        PrintWriter writer = new PrintWriter(sdtOut, true);
        writer.println(message);
    }

    public String readFromClient(){
        InputStreamReader reader = new InputStreamReader(stdIn);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String message = reader.readLine();
        reaer.close();
        return message;
    }
}