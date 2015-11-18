package com.qiaoda.unzip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
/*
 * 只需配置顶级目录，就可以解压目录下所有的zip，包括二级三级目录
 */
public class TopStartUp {
	private static final Logger logger = Logger.getLogger(TopStartUp.class);
	public void run(){
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("topzip-file.properties");
		Properties p = new Properties();
		try {
			p.load(in);
			
			String filepath = p.getProperty("topzip.filepath");
			String copypath = p.getProperty("topzip.copypath");
			String savepath = p.getProperty("topzip.savepath");
			ArrayList ziplist = new ArrayList();//压缩包路径
			ArrayList dirctList = new ArrayList();//文档目录
			File copyfile = new File(copypath);
			if(!copyfile.isDirectory()){
				copyfile.mkdir();
			}
			File[] files = new File(filepath).listFiles();
			logger.info("文件总数包括文件夹："+files.length);
			for(int i=0;i<files.length;i++){
				File f = files[i];
				if(f.isDirectory() && !f.getAbsolutePath().contains("/copyzip")){//移动后的目录不解压
					dirctList.add(f.getAbsolutePath());
				}else{
					ziplist.add(f.getAbsolutePath());
				}
			}
			unziplist(ziplist,savepath,copypath);
			undirctlist(dirctList,savepath,copypath);
		
		} catch (IOException e) {
			logger.info("初始化失败,topzip-file.properties是否未配置"+e);

		}
	}
	/*
	 * 路径集合跑
	 */
	public void unziplist(ArrayList list ,String savepath,String copypath){
		ArrayList zlist = new ArrayList();
		for(int i=0;i<list.size();i++){
			File f = new File(list.get(i).toString());
			if(!f.isFile()	&& !f.getAbsolutePath().contains("/copyzip")){
				zlist.add(f.getAbsolutePath());
			}
			else{
				try{
					
					logger.info("start "+f.getAbsolutePath());
					UnCompress un = new UnCompress(f.getAbsolutePath(),savepath, "rar,zip,doc,docx,xls,xlsx,txt,rtf,pdf,html,htm,xhtml,wml,msg,mht,xml,wps,eml".split(","), "rar,zip,7z".split(",")).process();
					logger.info("新文件名:"+copypath + File.separator + f.getName());
					FileUtils.copyTo(f, new File(copypath + File.separator + f.getName()));
					f.delete();
				}catch(Exception e){
					logger.error("解压文件异常:"+f.getAbsolutePath());
				}
			
			}
			
		}
		undirctlist(zlist,savepath,copypath);
	}
	/*
	 *目录的集合,获取后重新跑
	 */
	public void undirctlist(ArrayList list ,String savepath,String copypath){
		for(int i=0;i<list.size();i++){
			File f = new File(list.get(i).toString());
			if(f.isDirectory()&& !f.getAbsolutePath().contains("/copyzip")){
				File[] fs = f.listFiles();
				ArrayList l = new ArrayList();
				logger.info("文件夹路径："+f.getAbsolutePath());
				for(File file:fs){
					l.add(file.getAbsolutePath());
				}
				unziplist(l,savepath,copypath);
			}
			
			
		}
	}
	public void unzip(String filepath,String savepath){
		File[] file = new File(filepath).listFiles();
		logger.info("文件总数包括文件夹："+file.length);
		for(int i=0; i<file.length;i++){
			File f = file[i];
			if(f.isDirectory()&& !f.getAbsolutePath().contains("/copyzip")){
				unzip(f.getAbsolutePath(),savepath);
			}else{
				try{
					logger.info("start "+f.getAbsolutePath());
					UnCompress un = new UnCompress(f.getAbsolutePath(),savepath, "rar,zip,doc,docx,xls,xlsx,txt,rtf,pdf,html,htm,xhtml,wml,msg,mht,xml,wps,eml".split(","), "rar,zip,7z".split(",")).process();
					
				}catch(Exception e){
					
				}
				
			}
		}
	}
	
	public static void main(String[] args) {
		TopStartUp tsu = new TopStartUp();
		tsu.run();
		
	}

}
