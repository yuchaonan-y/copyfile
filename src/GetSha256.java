package copyfile;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.math.BigInteger;

public class GetSha256 {

    /**
     * 对字符串加密,加密算法使用MD5,SHA-1,SHA-256,默认使用SHA-256
     * 
     * @param strSrc  要加密的字符串
     * @param encName 加密类型
     * @return
     */
    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    public static String txt2String(File file, String encName) {
        try {
            MessageDigest md = null;
            FileInputStream fis = new FileInputStream(file);

            // 分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。
            byte[] buffer = new byte[1024];
            int length = -1;
            try {
                if (encName == null || encName.equals("")) {
                    encName = "SHA-256";
                }
                md = MessageDigest.getInstance(encName);
                while ((length = fis.read(buffer, 0, 1024)) != -1) {
                    md.update(buffer, 0, length);
                }
                fis.close();
                // 转换并返回包含16个元素字节数组,返回数值范围为-128到127
                byte[] md5Bytes = md.digest();
                BigInteger bigInt = new BigInteger(1, md5Bytes);// 1代表绝对值

                String string = bigInt.toString(16);// 转换为16进制
                while (string.length() < 64)
                    string = "0" + string;

                return string;
                // return bigInt.toString(16);

            } catch (Exception e) {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String filesha256(File filepath) {
        String s = GetSha256.txt2String(filepath, "");
        return s;
    }
}