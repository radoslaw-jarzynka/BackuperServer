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
//watek liczacy funkcje skrotu md5 plikow
public class Md5Generator extends NotifyingThread{
	
	private FileInputStream fis;
	private String md5;
	private String filePath;
	//nazwa uzytkownika - by watek wiedzial czyj jest dany plik
	private String username;
	//kolejka plikow oczekujacych na policzenie md5 (dokladniej kolejka sciezek do plikow)
	private volatile static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	//konstruktor pobierajacy sciezke pliku i nazwe uzytkownika
	public Md5Generator(String filePath, String username) {
		this.username = username;
		this.filePath = filePath;
	}
	//dodanie sciezki do kolejki
	public synchronized void addToQueue(String s) {
		queue.add(s);
	}
	
	//gettery i settery
	public String getMd5() {
		return md5;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String s) {
		filePath = s;
	}
	public String getUsername() {
		return username;
	}
	//funkcja uruchamiajaca watek
	public void doRun() {
		try {
			//wczytaj plik i licz jego md5
			fis = new FileInputStream(new File(filePath));
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			synchronized (queue) {
				//zajrzyj na wierzch kolejki - jesli pusta, po prostu zakoncz dzialanie, jesli nie - powiadom listenerow
				//ze skonczono liczyc jeden plik, pobierz kolejny plik z kolejki i licz znowu
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
