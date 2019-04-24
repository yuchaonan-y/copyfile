package copyfile;

import java.io.*;
import java.lang.Runnable;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Random;

public class CopyFile {
    private String oldpath; // 源文件目录
    private String oldfile; // 源文件名
    private String newpath; // 目标文件目录
    private String prename; // 新建目录前缀
    private int filenum; // 拷贝至目标文件夹的最小个数

    public void setOldPath(String oldP) {
        oldpath = oldP;
    }

    public String getOldPath() {
        return oldpath;
    }

    public void setOldFile(String oldF) {
        oldfile = oldF;
    }

    public void setNewPath(String newP) {
        newpath = newP;
    }

    public void setPreName(String pname) {
        prename = pname;
    }

    public void setFileNum(int filenm) {
        filenum = filenm;
    }

    public void initCopyfile() {
        oldpath = "";
        newpath = "";
        prename = "";
        filenum = 0;
    }

    // 定义内部类，用来产生新的线程
    class NewThread implements Runnable {
        private String filename;
        private int num;

        // 使用构造函数给类成员赋值
        public NewThread(String name, int num) {
            this.filename = name;
            this.num = num;
        }

        // 重载run函数
        public void run() {
            copyDir(filename, num);
        }
    }

    /**
     * 将文件随机拷贝至filenum+1个连续的目录中
     */
    public void copy() {
        Random rand = new Random();
        int filenum = rand.nextInt(3) + 3;
        int folder[] = new int[filenum + 1];

        for (int i = 0; i < filenum; i++) {
            int num = rand.nextInt(10);
            while (contain(folder, num)) {
                num = rand.nextInt(10);
            }
            folder[i] = num;
            Runnable rb = new NewThread(oldfile, folder[i]); // 创建，并初始化NewThread对象rb
            Thread td = new Thread(rb); // 通过Thread创建线程
            td.start(); // 启动线程td
            // System.out.println(td.getName());
        }
    }

    /**
     * 拷贝至指定序号的目录中
     * 
     * @oldfile为 源文件的文件名，不包含路径
     * @foldernum 放置在哪个序号目录下
     */
    public void copyDir(String oldf, int foldernum) {
        File source = new File(oldpath + File.separator + oldf);
        // System.out.println(source.getName());

        if (source.isDirectory()) {
            String[] filePath = source.list();

            for (int dirnum = 0; dirnum < filePath.length; dirnum++) {
                if (source.isDirectory()) {
                    copyDir(oldf + File.separator + filePath[dirnum], foldernum);
                }
            }
        } else if (source.isFile()) {
            String newfile = newpath + File.separator + prename + (foldernum) + File.separator + oldf;
            System.out.println(newfile);
            File target = new File(newfile);
            if (!target.exists()) {
                File tarParent = new File(target.getParent());
                if (!tarParent.exists())
                    tarParent.mkdirs();
            }
            copyfile(source, target);
        }

    }

    /**
     * 通过文件通道进行复制
     * 
     * @param source 源文件
     * @param target 目标文件
     */
    public void copyfile(File source, File target) {
        FileInputStream s = null;
        FileOutputStream t = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            s = new FileInputStream(source);
            t = new FileOutputStream(target);
            in = s.getChannel();// 得到对应的文件通道
            out = t.getChannel();// 得到对应的文件通道

            out.transferFrom(in, 0, in.size());// 连接两个通道，并且从in通道读取，然后写入out通道
            // in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                s.close();
                t.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 判断int型数组all中是否包含某个数num
    public Boolean contain(int[] all, int num) {
        for (int i = 0; i < all.length; i++) {
            if (all[i] == num)
                return true;
        }
        return false;
    }
}