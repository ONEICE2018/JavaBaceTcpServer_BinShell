package OneAIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OneLogManager {
	File Logfile;
	FileOutputStream logWriteStream;

	public OneLogManager() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(System.currentTimeMillis());
		Logfile = new File("." + File.separator + "log" + File.separator + "log_" + formatter.format(date) + ".txt");
		// 文件夹不存在则创建
		if (!Logfile.getParentFile().exists()) {
			Logfile.getParentFile().mkdirs();
		}

		if (!Logfile.exists()) {
			try {
				Logfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			logWriteStream = new FileOutputStream(Logfile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void putLogcat(String logmsg) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(System.currentTimeMillis());
		logmsg = formatter.format(date) + ":" + logmsg + "\n";
		byte[] msgbyte = logmsg.getBytes();
		try {
			logWriteStream.write(msgbyte, 0, msgbyte.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("LogManager,文件打开异常,撰写通道关闭，请重启服务器");
			try {
				logWriteStream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
