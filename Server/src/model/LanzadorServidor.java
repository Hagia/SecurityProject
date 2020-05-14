package model;

public class LanzadorServidor {
	private static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		if(args.length == 1){
			new ManejadorServidor(Integer.parseInt(args[0]));
		}else{
			new ManejadorServidor(PORT);
		}
	}
}