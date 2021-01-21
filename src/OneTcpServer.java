
//120.79.56.190
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class OneTcpServer {
	// 监听端口
	private static final int PORT = 9981;
	public static OneLogManager oneLogManager;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket socket = null;
		oneLogManager = new OneLogManager();
		try {
			// 建立服务器的Socket，并设定一个监听的端口PORT
			serverSocket = new ServerSocket(PORT);
			System.out.println("OneTCPServer启动成功，服务端口号为:" + PORT);
			// 由于需要进行循环监听，因此获取消息的操作应放在一个while大循环中
			while (true) {
				try {
					// 建立跟客户端的连接
					socket = serverSocket.accept();
				} catch (Exception e) {
					OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":连接异常");
					e.printStackTrace();
				}
				ServerThread thread = new ServerThread(socket);
				thread.start();
			}
		} catch (Exception e) {
			OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":端口被占用");
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}
}

class ServerThread extends Thread {
	private Socket socket;
	InputStream readinputStream;
	OutputStream outputStream;
	ConvertCode convertCode;
	OneFileSystem oneFileSystem;
	List<String> binfilenames;
	private boolean isconnected = true;
	// bin文件烧写与传输
	boolean updatastartsendok = false;
	int upwithbusyflag = 0;
	public FileInputStream binFileInputStream = null;
	boolean Updating = false;
	int Updatapro = 0;
	long appsize = 0;
	String previousBinPack = "";
	int outtimeflag = 0;

	public ServerThread(Socket socket) {
		this.socket = socket;
		convertCode = new ConvertCode();
		oneFileSystem = new OneFileSystem();

	}

	public void senddata(String data) {

		// 向客户端发送消息
		try {
			outputStream = socket.getOutputStream();
			outputStream.write(data.getBytes(), 0, data.getBytes().length);
//			OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":send:" + data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OneTcpServer.oneLogManager.putLogcat(e.getLocalizedMessage());
			isconnected = false;
			System.out.println(socket.toString() + ":通信异常:客户端主动断开连接了");
		}

	}

	@Override
	public void run() {
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (true && isconnected) {
						outtimeflag++;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (outtimeflag > 10) {
							senddata("0xFF");
							senddata("0xFF");
							outtimeflag = 0;

						}
					}
				}
			}).start();
			readinputStream = socket.getInputStream();
			byte[] bytes = new byte[2048];
			while (isconnected) {
				// 接收客户端的消息并打印
				int getnum = readinputStream.read(bytes);
//				String string = new String(bytes);
//				OneTcpServer.oneLogManager.putLogcat(socket.toString());
//				OneTcpServer.oneLogManager.putLogcat(string);
				if (getnum > 0) {
					outtimeflag = 0;
					analysis(bytes, getnum);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(socket.toString() + ":通信异常:客户端主动断开连接了");
		}
		OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":客户端主动断开连接了");
		// 操作结束，关闭socket
		try {
			socket.close();
		} catch (IOException e) {
			OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":关闭连接出现异常");
			e.printStackTrace();
		}

	}

	// 指令结构:lssue:pull:bin:size1;
	// lssue(对对象干的事情) 比如这里的下发数据
	// pull （对象要干什么操作）你要干什么 这里是我要拉取数据
	// bin (对象需操作的属性) 拉取什么 这里是拉取bin
	// size （被拉取的属性） 我先给你一个消息 我这里有 size后面的1个bin文件，你按照顺序找我要吧
	private void analysis(byte[] data, int getnum) {

		String strdata = new String(data, 0, getnum);

		// 列表传输部分:
		if (strdata.equals("Stop:Disconnect;")) {
			isconnected = false;
			return;
		}
		if (strdata.equals("pull:bin:getbinlist;")) {
			binfilenames = oneFileSystem.getBinInBasePath();
			// 反馈数量
			senddata("pull:bin:size:" + binfilenames.size() + ";");
			return;
		}
		if (strdata.contains("pull:bin:num:") && strdata.endsWith(";")) {
			strdata = convertCode.getSubString(strdata, "pull:bin:num:");
			strdata = convertCode.getSubString(strdata, ";");
			if (strdata == null || strdata == "") {
				return;
			}
			int num = Integer.valueOf(strdata);
			if (num < binfilenames.size()) {
				senddata("pull:bin:num:" + num + ":" + binfilenames.get(num) + ";");
			} else {
				senddata("pull:bin:num:erro:overarea;");
			}
			return;
		}
		// bin烧写
		// 1要传输哪一个文件
		if (strdata.contains("Download:bin:") && strdata.endsWith(";")) {
			strdata = convertCode.getSubString(strdata, "Download:bin:");
			strdata = convertCode.getSubString(strdata, ";");
			if (strdata == null || strdata == "") {
				senddata("Download:bin:erro:namenull;");
				return;
			}
			updata_start(strdata);
		}
		if (strdata.contains("Download:NextBin:") && strdata.endsWith(";")) {
			strdata = convertCode.getSubString(strdata, "Download:NextBin:");
			strdata = convertCode.getSubString(strdata, ";");
			if (strdata == null || strdata == "") {
				senddata("Download:bin:erro:contextnull;");
				updata_over();
				return;
			}
			if (strdata.equals(previousBinPack)) {
				doUpData();
			} else {
				senddata("Download:bin:erro:contextERRO;");
			}
		}
		if (strdata.equals("DownLoad:Erro_CloseBinfile;")) {
			updata_over();
		}

	}

	void updata_start(String binname) {
		try {
			File file = new File(oneFileSystem.SysbinFilePathgeter() + binname);
			binFileInputStream = new FileInputStream(file);
			try {
				appsize = getFileSize(file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				OneTcpServer.oneLogManager.putLogcat(e.getLocalizedMessage());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			senddata("Download:bin:erro:noSuchFile");
			e.printStackTrace();
			OneTcpServer.oneLogManager.putLogcat(e.getLocalizedMessage());
		}

		flen = 0;
		Updatapro = 0;
		Updating = true;
		updatastartsendok = false;
		senddata("Download:Bin:" + binname + ":Start;");
		previousBinPack = "OKStart";
		updatastartsendok = true;
		OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":bin:" + binname + "开始传输");
	}

	// 升级结束时调用
	void updata_over() {
		Updatapro = 0;
		Updating = false;
		if (binFileInputStream != null) {
			try {
				binFileInputStream.close();
				binFileInputStream = null;
			} catch (IOException e) {
				e.printStackTrace();
				OneTcpServer.oneLogManager.putLogcat(e.getLocalizedMessage());
				binFileInputStream = null;
			}
		}

	}

	long flen = 0;
	final int upsendlen = 512;

	public void doUpData() {
		if (binFileInputStream != null) {
			byte buffer[] = new byte[upsendlen];
			int len = 0;
			try {
				if ((len = binFileInputStream.read(buffer, 0, buffer.length)) > 0) {
					Updatapro = (int) (100 * ((flen * 1.0) / appsize));
					byte pro[] = { (byte) (Updatapro >> 8), (byte) (Updatapro & 0xff) };
					flen = flen + len;
					String BinFramData = convertCode.bytes2HexString(buffer, len);
					// "Dowload:BinFream:"+2bytHex进度
					String BinDataPack = "Dowload:BinFream:" + convertCode.bytes2HexString(pro, 2) + BinFramData + ";";
					previousBinPack = BinFramData;
					senddata(BinDataPack);
				} else {
					String BinDataPack = "Dowload:BinOver:" + flen + ";";
					previousBinPack = BinDataPack;
					senddata(BinDataPack);
					OneTcpServer.oneLogManager.putLogcat(socket.toString() + ":bin文件传输完毕");
					updata_over();
				}
			} catch (IOException e) {
				e.printStackTrace();
				OneTcpServer.oneLogManager.putLogcat(e.getLocalizedMessage());
			}

		}

	}

	private static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
			fis.close();
		} else {
			size = 0;
		}
		return size;
	}

}