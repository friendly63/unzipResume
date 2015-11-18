package com.qiaoda.unzip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Random;

/**
 *
 * 文件工作类
 *
 * @author shifenghu
 *
 * @version 1.0
 * 
 * @date 2014-5-19下午8:46:40 
 *
 */
public class FileUtils {

	private static final int length = 5;
	
	/**
	 * 临时目录
	 */
	public static final String TempDirectory = "temps";
	
	/**
	 * 正式目录
	 */
	public static final String FileDirectory = "files";
	
	/**
	 * 
	 * 获取文件流的 hashcode
	 *
	 * @param stream
	 * @return
	 * 
	 * @date 2014-5-19下午8:47:32
	 */
	public static String hashCode(InputStream stream){
		try {
			byte[] data = new byte[stream.available()];
			stream.read(data);
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(data);
			byte[] byteArray = md.digest();
			StringBuffer rs = new StringBuffer();
			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){
					 rs.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				}
				else{
					rs.append(Integer.toHexString(0xFF & byteArray[i]));
				}
			}
			return rs.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		} finally{
			try {
				stream.close();
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	
	
	/**
	 * 
	 * 计算文件的hash code
	 *
	 * @param file
	 * @return
	 * 
	 * @date 2014-5-19下午8:53:54
	 */
	public static String hashCode(File file){
		try {
			FileInputStream in = new FileInputStream(file);
			return hashCode(in);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * 
	 * 随机生成一个文件名称
	 *
	 * @return
	 * 
	 * @date 2014-5-23下午9:10:30
	 */
	public static String generaicName(){
		StringBuffer name = new StringBuffer("" + System.currentTimeMillis());
		Random ran = new Random();
		for(int i = 0;i < length ;i ++){
			name.append(ran.nextInt(10000));
		}
		return hashCode(new ByteArrayInputStream(name.toString().getBytes()));
	}
	/**
	 * 
	 * 获取文件扩展名
	 *
	 * @param name
	 * @return
	 * 
	 * @date 2014-5-23下午9:11:08
	 */
	public static String getExtName(String name){
		if(name == null || name.indexOf(".") == -1){
			return name;
		}
		return name.substring(name.lastIndexOf(".") + 1);
	}
	
	/**
	 * 文件转换
	 *
	 *
	 * @param source
	 * @param as
	 * 
	 * @date 2014-5-22下午10:20:42
	 */
//	public static void saveAs(MultipartFile source, File as){
//		try {
//			source.transferTo(as);
//		}catch (IOException e) {
//			throw new IllegalArgumentException(e);
//		}
//	}
	
	/**
	 * 
	 * 将原始文件绑定到正式目录
	 *
	 * @param tempName
	 * @param formal
	 * 
	 * @date 2014-5-29下午9:30:32
	 */
//	public static void saveAs(String tempName,File formal){
//		File parent = WebApplicationUtils.getDirectory(TempDirectory);
//		File temp = new File(parent, tempName);
//		if(!temp.isFile()){
//			throw new IllegalArgumentException(tempName + " 文件不存在！");
//		}
//		temp.renameTo(formal);
//	}
	
	/**
	 * 
	 * 获取文件二进制内容
	 *
	 * @param path
	 * @return
	 * 
	 * @date 2015年1月24日下午5:06:23
	 */
	public static byte[] getBytes(String path){
		FileInputStream in = null;
		byte[] bytes = null;
		try {
			in = new FileInputStream(path);
			bytes = new byte[in.available()];
			in.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}
	
	private static void close(InputStream in){
		try {
			in.close();
		} catch (IOException e) {}
	}
	
	private static void close(OutputStream in){
		try {
			in.close();
		} catch (IOException e) {}
	}
	
	private static void close(FileChannel in){
		try {
			in.close();
		} catch (IOException e) {}
	}
	
	/**
	 * 复制文件
	 * @param source
	 * @param target
	 */
	public static void copyTo(File source,File target){
	 FileChannel in = null;  
	    FileChannel out = null;  
	    FileInputStream inStream = null;  
	    FileOutputStream outStream = null;  
	    try {  
	        inStream = new FileInputStream(source);  
	        outStream = new FileOutputStream(target);  
	        in = inStream.getChannel();  
	        out = outStream.getChannel();  
	        in.transferTo(0, in.size(), out);  
	    } catch (IOException e) {  
	       throw new IllegalArgumentException(e);
	    } finally {  
	        close(inStream);  
	        close(in);  
	        close(outStream);  
	        close(out);  
	    } 
}
	
}
