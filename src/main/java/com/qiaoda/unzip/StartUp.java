package com.qiaoda.unzip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
/*
 * 文件路径为zip目录，不支持二级嵌套
 */
public class StartUp {
	private static final Logger logger = Logger.getLogger(StartUp.class);
	public void run(){
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("zip-file.properties");
		Properties p = new Properties();
		try {
			p.load(in);
			
			int count = Integer.parseInt(p.getProperty("zip.filecount"));
			String savepath = p.getProperty("zip.savepath");
			for(int i=1;i<=count;i++){
				String filepath = p.getProperty("zip.filepath"+i);
				File f1 = new File(filepath);
				File[] files = f1.listFiles();
				for(int n=0;n<files.length;n++){
					File f = files[n];
					logger.info("开始解压："+f.getName());
					String zippath = f.getAbsolutePath();
					getFiles(f.getAbsolutePath(),savepath);
				}
				
			}
		} catch (IOException e) {
			logger.info("初始化失败,zip-file.properties是否未配置"+e);

		}
	}
	
	public void getFiles(String zippath,String savepath){
		try{
			
			UnCompress un = new UnCompress(zippath,savepath, "rar,zip,doc,docx,xls,xlsx,txt,rtf,pdf,html,htm,xhtml,wml,msg,mht,xml,wps,eml".split(","), "rar,zip,7z".split(",")).process();
			
		}catch(Exception e){
			
		}
	}
	public static void main(String[] args) {
		StartUp stu = new StartUp();
		logger.info("开始解压");
		stu.run();
		logger.info("解压结束");
	}

}
