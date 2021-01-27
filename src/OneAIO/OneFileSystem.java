package OneAIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OneFileSystem {

	private String SysFilePath = "." + File.separator + "updatabin" + File.separator;// ./ ：当前目录

	public OneFileSystem() {

	}

	/**
	 * 获取路径下的所有文件/文件夹
	 * 
	 * @param directoryPath  需要遍历的文件夹路径
	 * @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
	 * @return
	 */
	public List<String> getAllbinFile(String directoryPath, boolean isAddDirectory) {
		List<String> list = new ArrayList<String>();
		File baseFile = new File(directoryPath);
		if (baseFile.isFile() || !baseFile.exists()) {
			return list;
		}
		File[] files = baseFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (isAddDirectory) {
					list.add(file.getAbsolutePath());
				}
				list.addAll(getAllbinFile(file.getAbsolutePath(), isAddDirectory));
			} else {
				String filename = file.getAbsolutePath();
				if (filename.endsWith(".bin")) {
					list.add(filename);
				}
			}
		}
		return list;
	}

	/**
	 * 获取路径下的所有文件/文件夹
	 * 
	 * @param directoryPath  需要遍历的文件夹路径
	 * @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
	 * @return
	 */
	public List<String> getBinInBasePath() {

		List<String> list = new ArrayList<String>();
		File baseFile = new File(SysFilePath);
//		try {
//			System.out.println(baseFile.getCanonicalPath());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(System.getProperty("user.dir"));
		if (baseFile.isFile() || !baseFile.exists()) {
			return list;
		}
		File[] files = baseFile.listFiles();
		for (File file : files) {
			String filename = file.getAbsolutePath();
			if (filename.endsWith(".bin")) {
				list.add(file.getName());
			}
		}
		return list;
	}

	public String SysbinFilePathgeter() {
		return SysFilePath;
	}

}
