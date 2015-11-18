package com.qiaoda.unzip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;


/**
 * 解压文件
 * @author shifenghu
 *
 */
public class UnCompress {
	
	private static final Logger logger = Logger.getLogger(UnCompress.class);
	
	private static final String extsName = "compress.file.exts";
	
	private static final String uncompressExtName = "compress.file.unexts";
	
	private static final String savePath = "compress.file.temp.directory";
	
	
	/**
	 * 存储的目录
	 */
	private String direcotry;
	
	/**
	 * 解压的文件源
	 */
	private String source;
	
	/**
	 * 解压后的文件
	 */
	private Map<String, String> unfiles;
	
	
	/**
	 * 是否已经处理
	 */
	private boolean isProcessed;
	
	/**
	 * 原始文件大小
	 */
	private long fileLength;
	
	/**
	 * 文件数量
	 */
	private int count;
	
	/**
	 * 支持的文件格式
	 */
	private String[] exts;
	
	/**
	 * 需要解压缩的文件格式
	 */
	private String[] compressExts;
	/**
	 * 构造函数
	 * @param source
	 */
	public UnCompress(String source){
		this(source, savePath, extsName.split(","), uncompressExtName.split(","));
	}
	
	/**
	 * 构造函数
	 * @param source
	 * @param savePath
	 * @param exts
	 * @param compressExts
	 */
	public UnCompress(String source, String savePath, String[] exts, String[] compressExts){
		this.source = source;
		this.direcotry = savePath;
		unfiles = new HashMap<String, String>(32);
		isProcessed = false;
		count = 0;
		this.exts = exts;
		this.compressExts = compressExts;
	}
	
	/**
	 * 获取文件长度
	 * @return
	 */
	public long getFileLength(){
		return fileLength;
	}
	
	/**
	 * 获取解压结果文件
	 * @return
	 */
	public Map<String, String> getUnCompressFiles(){
		return unfiles;
	}
	
	/**
	 * 获取本地解压的文件数量
	 * @return
	 */
	public int getUnComplressSize(){
		return count;
	}
	
	/**
	 * 设置本地解压的文件数量
	 * @return
	 */
	public int setUnComplressSize(int count){
		return this.count = count;
	}
	
	/**
	 * 开始处理
	 * @return 当前解压对象
	 */
	public UnCompress process() throws Exception{
		if(isProcessed){
			return this;
		}
		isProcessed = true;
		//zip文件
		if(source.endsWith("zip")){
			unzip();
		}
		//rar
		else if(source.endsWith("rar")){
			unrar();
		}else if(source.endsWith("7z")){
			un7z();
		}else{
			noUnCompress();
		}
		recursive();
		return this;
	}
	/**
	 * 不需要处理的文件
	 */
	private void noUnCompress(){
		File f = new File(source);
		if(!f.isFile()){
			logger.error(source + " 文件不存在");
		}else{
			String ext;
			//检查格式
			if((ext = fileAllowExt(f.getName())) == null){
				logger.error(source + " 文件不支持次格式");
			}else{
				ext = getUuid() + "." + ext;
				fileLength = f.length();
				FileUtils.copyTo(f, new File(direcotry + File.separator + ext));
				count ++;
				unfiles.put(ext, f.getName());
			}
		}
	}
	
	/**
	 * 处理递归文件
	 */
	private void recursive(){
		Iterator<String> it = unfiles.keySet().iterator();
		String key;
		UnCompress un;
		Map<String, String> subs = new HashMap<String, String>(64);
		int countT = 0;
		while(it.hasNext()){
			key = it.next();
			if(isCompressFile(key) != null){
				logger.debug("递归 解压缩 " + direcotry + File.separator + key);
				try{
					un = new UnCompress(direcotry + File.separator + key);
					un.process();
					//删除原始文件
					new File(direcotry + File.separator + key).delete();
					//将当前的所有元素增加到file里面
					subs.putAll(un.getUnCompressFiles());
					countT += un.getUnComplressSize();
				} catch (Throwable ex){
					logger.error(ex.getMessage(), ex);
				}
			}
		}
		//添加到父列表中
		unfiles.putAll(subs);
		//删除压缩文件
		it = unfiles.keySet().iterator();
		while(it.hasNext()){
			key = it.next();
			if(isCompressFile(key) != null){
				it.remove();
			}
		}
		count += countT;
	}
	
	/**
	 * 是否压缩文件
	 * @param name
	 * @return
	 */
	private String isCompressFile(String name){
		for(String str : compressExts){
			if(name.toLowerCase().endsWith(str.toLowerCase())){
				return str;
			}
		}
		return null;
	}
	
	public void unzip(){
		File file = new File(direcotry + File.separator + getUuid());
		fileLength = file.length();
		file.mkdir();
		try{
			Project p = new Project();     
	        Expand e = new Expand();
	        e.setProject(p);     
	        e.setSrc(new File(source));
	        e.setOverwrite(false);     
	        e.setDest(file);
	        e.setEncoding("gbk"); 
	        e.execute();
	        moveTo(file);
		}finally{
			deletes(file);
		}
	}
	
	public void un7z(){
		SevenZFile sevenZFile = null;
		SevenZArchiveEntry entry = null;
		try {
			sevenZFile = new SevenZFile(new File(source));
			FileOutputStream out;
			while((entry = sevenZFile.getNextEntry()) != null){
				if(fileAllowExt(entry.getName()) == null){
                	continue;
                }
				count ++;
				out = new FileOutputStream(newFile(entry.getName()));
		        byte[] content = new byte[(int) entry.getSize()];
		        sevenZFile.read(content, 0, content.length);
		        out.write(content);
		        out.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		finally{
			try{
				if (sevenZFile != null){
					sevenZFile.close();
				}
			}catch(IOException ex){
				logger.error(ex);
			}
		}
		
	}
	
	/**
	 * 递归删除
	 * @param file
	 */
	private void deletes(File file){
		if(file.isDirectory()){
			File[] subs = file.listFiles();
			for(File f : subs){
				deletes(f);
			}
		}
		file.delete();
	}
	
	/**
	 * 移动
	 * @param file
	 */
	private void moveTo(File file){
		if(file.isDirectory()){
			File[] subs = file.listFiles();
			for(File f : subs){
				moveTo(f);
			}
		}else{
			count ++;
		}
		if(fileAllowExt(file.getName()) != null){
			file.renameTo(newFile(file.getName()));
		}
		file.delete();
	}
	/**
	 * 解压rar
	 * @param fileName
	 * @param extPlace
	 */
	public void unrar(){
		Archive archive = null;  
        FileOutputStream fos = null;
        File file = new File(source);
        fileLength = file.length();
        try {
        	logger.debug("解压 " + source);
            archive = new Archive(file);  
            FileHeader fh = archive.nextFileHeader();
            while (fh != null) {
                if (fh.isDirectory()) {  
                    fh = archive.nextFileHeader();  
                    continue;  
                }
                count ++;
                String filename = fh.getFileNameString().trim();
                if(existZH(filename)){
                	filename=  fh.getFileNameW().trim();  
                }
                if(fileAllowExt(filename) == null){
                	fh = archive.nextFileHeader();
                	continue;
                }
                fos = new FileOutputStream(newFile(filename));
                archive.extractFile(fh, fos);  
                fos.close();  
                fos = null;
                fh = archive.nextFileHeader();
            }  
            archive.close();  
            archive = null;
        } catch (Exception e) {  
        	throw new IllegalArgumentException(e);
        } finally {  
            if (fos != null) {  
                try {  
                    fos.close();  
                    fos = null;  
                } catch (Exception e) {  
                    e.printStackTrace();
                }  
            }  
            if (archive != null) {  
                try {  
                    archive.close();  
                    archive = null;  
                } catch (Exception e) {  
                	e.printStackTrace();
                }  
            }  
        }  
	}
	/*
	 * 是否有中文
	 */
	public static boolean existZH(String str) {  
	    String regEx = "[\\u4e00-\\u9fa5]";  
	    Pattern p = Pattern.compile(regEx);  
	    Matcher m = p.matcher(str);  
	    while (m.find()) {  
	        return true;  
	    }  
	    return false;  
	}  
	/**
	 * 新建一个文件
	 * @param name
	 * @return
	 */
	private File newFile(String name){
		String uuid = getUuid() + "." +  fileAllowExt(name);
		unfiles.put(uuid, name);
		return new File(direcotry + File.separator + uuid);
	}
	
	/**
	 * 获取一个uuid名称
	 * @return
	 */
	private String getUuid(){
		return UUID.randomUUID().toString();
	}
	
	/**
	 * 是否允许的格式
	 * @param name
	 * @return
	 */
	private String fileAllowExt(String name){
		if(isHideFile(name)){
			return null;
		}
		String t;
		if((t = isCompressFile(name)) != null){
			return t;
		}
		for(String ext : exts){
			if(name.toLowerCase().endsWith(ext)){
				return ext;
			}
		}
		return null;
	}
	
	private boolean isHideFile(String name){
		String[] ss = name.split(File.separator + File.separator);
		String n = ss[ss.length - 1];
		if(n.startsWith(".")){
			return true;
		}
		return false;
	}
	
	/**
	 * 删除生成的文件
	 */
	public void delete(){
		Iterator<String> it = unfiles.keySet().iterator();
		while(it.hasNext()){
			new File(direcotry + File.separator + it.next()).delete();
		}
	}
	
	/**
	 * 删除特定文件
	 * @param file
	 */
	public void delete(String file){
		new File(direcotry + File.separator + file).delete();
	}
	
	/**
	 * 获取文件引用
	 * @param file
	 * @return
	 */
	public File getFile(String file){
		return new File(direcotry + File.separator + file); 
	}
	
	public static void main(String[] args) {
		long l = System.currentTimeMillis();
		

	}
}
