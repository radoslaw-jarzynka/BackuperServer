/**
 * created on 22:20:28 2 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import utils.NotifyingThread;
import utils.ThreadCompleteListener;

public class Md5Generator extends NotifyingThread{
	
	private FileInputStream fis;
	private String md5;
	private String filePath;
	
	private volatile static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	
	public Md5Generator(String filePath) {
		this.filePath = filePath;
	}
	
	public synchronized void addToQueue(String s) {
		queue.add(s);
	}
	
	public String getMd5() {
		return md5;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String s) {
		filePath = s;
	}
	
	public void doRun() {
		try {
			fis = new FileInputStream(new File(filePath));
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			synchronized (queue) {
				if (queue.peek() != null) {
					for (ThreadCompleteListener listener : listeners) {
						listener.notifyOfThreadComplete(this);
					}
					filePath = queue.poll();
					doRun();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
