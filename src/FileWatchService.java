package copyfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 测试JDK的WatchService监听文件变化
 * 
 * @author yangzhilong
 *
 */
public class FileWatchService {
	private static String oldpath;
	private static String newpath;
	private static String prename; // 新建目录前缀
	private static int minfilenum; // 拷贝至目标文件夹的最小个数
	// private static int foldernum = 0; //记录拷贝文件的数量

	public static void main(String[] args) throws IOException {
		// 需要监听的文件目录（只能监听目录）
		FileWatchService filewatchService = new FileWatchService();
		try {
			filewatchService.setParm();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File oldp = new File(oldpath);
		if (!oldp.isDirectory()) {
			System.out.println(oldpath + "is not a directory!");
			return;
		}
		filewatchService.mkCopyDirs();
		// 源文件目录中存在文件时，将源文件目录下的文件拷贝至目标目录下
		File[] addfiles = oldp.listFiles();
		if (addfiles.length > 0) {
			for (int i = 0; i < addfiles.length; i++) {
				filewatchService.copyFile(addfiles[i].getName());
			}
		}

		// 新启动一个线程，监控源文件目录的变化情况，若有新增文件，则拷贝至目标文件夹下
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Path p = Paths.get(oldpath);
		p.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);

		System.out.println("开始监控文件");

		Thread thread = new Thread(() -> {
			try {
				while (true) {
					WatchKey watchKey = watchService.take();
					List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
					for (WatchEvent<?> event : watchEvents) {
						// TODO 根据事件类型采取不同的操作。。。。。。。
						System.out
								.println("[" + watchEvents + "/" + event.context() + "]文件发生了[" + event.kind() + "]事件");
						if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
							String filepath = oldpath + File.separator + event.context().toString();
							boolean fileIsCreateSuccess = filewatchService.fileIsCreateSuccess(filepath);
							if (fileIsCreateSuccess) {
								// 拷贝文件
								filewatchService.copyFile(event.context().toString());
							}
						}
						/*
						 * if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY) ) { String
						 * filepath = oldpath + event.context().toString() ;
						 * 
						 * boolean fileIsCreateSuccess = filewatchService.fileIsCreateSuccess(filepath);
						 * if ( fileIsCreateSuccess ) { //拷贝文件
						 * filewatchService.copyFile(event.context().toString()); //eventfile = "" ; } }
						 */
					}
					watchKey.reset();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(false);
		thread.start();

		// 增加jvm关闭的钩子来关闭监听
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				watchService.close();
			} catch (Exception e) {
			}
		}));
	}

	// 判断文件是否处于可写状态
	// filePath为文件名，包括文件路径
	public boolean fileIsCreateSuccess(String filePath) {
		File file;
		file = new File(filePath);

		long len1, len2;
		len2 = file.length();
		try {
			do {
				len1 = len2;
				Thread.sleep(10000); // 每10s检查文件变化
				file = new File(filePath);
				len2 = file.length();
			} while (len1 < len2);

			while (true) {
				if (file.renameTo(file))
					break;
				Thread.sleep(10000); // 每10s检查文件变化
			}
		} catch (Exception e) {
			// logger.error("文件创建失败", e);
			return false;
		}
		return true;
	}

	/*
	 * 将源文件中变化的目录拷贝至目标位置
	 * 
	 */
	public void copyFile(String filename) {
		File addFile = new File(oldpath + File.separator + filename);
		if (addFile.isDirectory())
			return;

		GetSha256 sha = new GetSha256();
		String sha256 = sha.filesha256(addFile);

		File file2 = new File(oldpath + File.separator + sha256);
		addFile.renameTo(file2);

		if (!sha256.equals(filename))
			return;
		CopyFile copyFile = new CopyFile();
		copyFile.initCopyfile();
		// 源文件夹路径
		copyFile.setOldPath(oldpath);
		// 建立目标文件夹
		copyFile.setNewPath(newpath);
		// 文件前缀
		copyFile.setPreName(prename);
		// 最小数量
		copyFile.setFileNum(minfilenum);

		copyFile.setOldFile(sha256);

		copyFile.copy();
		// foldernum++ ;
	}

	/**
	 * 建立拷贝文件的目标位置需要的10个目录
	 */
	public void mkCopyDirs() {
		for (int i = 0; i < 10; i++) {
			String newfilepath = newpath + File.separator + prename + i;
			File targetpath = new File(newfilepath);
			if (!targetpath.exists()) {
				targetpath.mkdirs();
				if (targetpath.canWrite() == false)
					targetpath.setWritable(true);
			}
		}
	}

	/**
	 * 从配置文件中读出源文件、目标文件等参数
	 */
	//
	//
	public void setParm() throws Exception {
		// File config = new File("copyfile" + File.separator + "src" + File.separator +
		// "pathname.ini");//

		// 定义一个file对象，用来初始化FileReader
		// File config = new File("pathname.ini");// 定义一个file对象，用来初始化FileReader
		// FileReader reader = new FileReader(config);//
		// 定义一个fileReader对象，用来初始化BufferedReader
		// BufferedReader bReader = new BufferedReader(reader);//
		// new一个BufferedReader对象，将文件内容读取到缓存
		InputStream is = this.getClass().getResourceAsStream("pathname.ini");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();// 定义一个字符串缓存，将字符串存放缓存中
		String s = "";
		while ((s = bReader.readLine()) != null) {// 逐行读取文件内容，不读取换行符和末尾的空格
			if (s.length() == 0 || s == null)
				continue;

			int equallot = s.indexOf("=");
			if (equallot == -1)
				equallot = 0;

			switch (s.substring(0, equallot)) {
			case "SOURCE":
				oldpath = s.substring(equallot + 1, s.length());
				break;
			case "TARGET":
				newpath = s.substring(equallot + 1, s.length());
				break;
			case "MINNUM":
				String minnum = s.substring(equallot + 1, s.length());
				minfilenum = Integer.parseInt(minnum);
				break;
			case "PERNAME":
				prename = s.substring(equallot + 1, s.length());
				break;
			}
		}
		bReader.close();
	}
}