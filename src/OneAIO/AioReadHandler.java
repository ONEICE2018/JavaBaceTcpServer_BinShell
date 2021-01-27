package OneAIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

public class AioReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel socket;

	public AioReadHandler(AsynchronousSocketChannel socket) {
		this.socket = socket;
		oneLogManager = new OneLogManager();
		oneFileSystem = new OneFileSystem();
		convertCode = new ConvertCode();
	}

	public void cancelled(ByteBuffer attachment) {
		System.out.println("cancelled");
	}

	private CharsetDecoder decoder = Charset.forName("GBK").newDecoder();

	@Override
	public void completed(Integer i, ByteBuffer buf) {
		if (i > 0) {
			buf.flip();
			try {
				System.out.println("收到" + socket.getRemoteAddress().toString() + "的消息:" + decoder.decode(buf));

				analysis(buf.array(), i);
				buf.compact();
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket.read(buf, buf, this);
		} else if (i == -1) {
			try {
				System.out.println("客户端断线:" + socket.getRemoteAddress().toString());
				buf = null;
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer buf) {
		System.out.println(exc);
	}

	public static OneLogManager oneLogManager;
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

	// 指令结构:lssue:pull:bin:size1;
	// lssue(对对象干的事情) 比如这里的下发数据
	// pull （对象要干什么操作）你要干什么 这里是我要拉取数据
	// bin (对象需操作的属性) 拉取什么 这里是拉取bin
	// size （被拉取的属性） 我先给你一个消息 我这里有 size后面的1个bin文件，你按照顺序找我要吧
	public void analysis(byte[] data, int getnum) {

		String strdata = new String(data, 0, getnum);

		// 列表传输部分:
		if (strdata.equals("Stop:Disconnect;")) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		if (strdata.equals("pull:bin:getbinlist;")) {
			binfilenames = oneFileSystem.getBinInBasePath();
			// 反馈数量

			senddata(socket, "pull:bin:size:" + binfilenames.size() + ";");
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
				senddata(socket, "pull:bin:num:" + num + ":" + binfilenames.get(num) + ";");
			} else {
				senddata(socket, "pull:bin:num:erro:overarea;");
			}
			return;
		}
		// bin烧写
		// 1要传输哪一个文件
		if (strdata.contains("Download:bin:") && strdata.endsWith(";")) {
			strdata = convertCode.getSubString(strdata, "Download:bin:");
			strdata = convertCode.getSubString(strdata, ";");
			if (strdata == null || strdata == "") {
				senddata(socket, "Download:bin:erro:namenull;");
				return;
			}
			updata_start(strdata);
		}
		if (strdata.contains("Download:NextBin:") && strdata.endsWith(";")) {
			strdata = convertCode.getSubString(strdata, "Download:NextBin:");
			strdata = convertCode.getSubString(strdata, ";");
			if (strdata == null || strdata == "") {
				senddata(socket, "Download:bin:erro:contextnull;");
				updata_over();
				return;
			}
			if (strdata.equals(previousBinPack)) {
				doUpData();
			} else {
				senddata(socket, "Download:bin:erro:contextERRO;");
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
				oneLogManager.putLogcat(e.getLocalizedMessage());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			senddata(socket, "Download:bin:erro:noSuchFile");
			e.printStackTrace();
			oneLogManager.putLogcat(e.getLocalizedMessage());
		}

		flen = 0;
		Updatapro = 0;
		Updating = true;
		updatastartsendok = false;
		senddata(socket, "Download:Bin:" + binname + ":Start;");
		previousBinPack = "OKStart";
		updatastartsendok = true;
		oneLogManager.putLogcat(socket.toString() + ":bin:" + binname + "开始传输");
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
				oneLogManager.putLogcat(e.getLocalizedMessage());
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
					senddata(socket, BinDataPack);
				} else {
					String BinDataPack = "Dowload:BinOver:" + flen + ";";
					previousBinPack = BinDataPack;
					senddata(socket, BinDataPack);
					oneLogManager.putLogcat(socket.toString() + ":bin文件传输完毕");
					updata_over();
				}
			} catch (IOException e) {
				e.printStackTrace();
				oneLogManager.putLogcat(e.getLocalizedMessage());
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

	public void senddata(AsynchronousSocketChannel socket, String data) {

		socket.write(convertCode.string2ByteBuffer(data));
		System.out.println("发送：" + data);

	}

}