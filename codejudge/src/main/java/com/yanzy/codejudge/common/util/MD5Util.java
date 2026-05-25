package com.yanzy.codejudge.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 生成 MD5 哈希值
     *
     * @param input 原始字符串
     * @return 32位小写十六进制字符串
     */
    public static String getMD5(String input) {
        try {
            // 1. 获取 MessageDigest 实例，指定 MD5 算法
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2. 计算摘要 (注意使用 UTF-8 编码)
            byte[] messageDigest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 3. 将字节数组转换为 Hex 字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                // 将字节转换为16进制，不足两位前面补0
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法错误", e);
        }
    }
}
