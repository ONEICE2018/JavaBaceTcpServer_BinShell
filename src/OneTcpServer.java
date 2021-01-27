
//120.79.56.190
import java.io.IOException;

import OneAIO.AioTcpServer;

public class OneTcpServer {
	// 监听端口
	private static final int PORT = 9981;

	public static void main(String[] args) throws IOException {

		AioTcpServer server;
		try {
			server = new AioTcpServer(PORT);
			new Thread(server).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			System.out.println("erro!:" + e.getLocalizedMessage());
		}
	}
}
