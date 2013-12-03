/**
 * created on 16:50:20 27 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package mainApp;

import common.BackuperInterface;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import utils.Reader;
import utils.ThreadCompleteListener;
import utils.Writer;
import model.Client;
import model.Md5Generator;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

//klasa serwera
public class ServerApp extends UnicastRemoteObject implements BackuperInterface, ThreadCompleteListener {

	private static final long serialVersionUID = 1L;

	private int numberOfConnectedClients;
	
	private Vector<Client> connectedClients;
	
	private Vector<Client> clients;
	
	private Md5Generator md5gen;
	
	private int port;
	
	//glowna metoda
	public static void main(String[] args) {
	        try {
	        	System.setProperty("java.security.policy", "policy");
	            System.setSecurityManager(new RMISecurityManager());
	        } catch (SecurityException e) {
	            System.err.println("Security violation " + e);
	        }
	    	
	        try {
	            ServerApp serverApp = new ServerApp();
	            serverApp.port = 1099;
	            BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));
	            System.out.println("Enter port:");
	            String line = null;
	            try {
	            	line = brIn.readLine();
	            	serverApp.port = Integer.parseInt(line);
	            } catch (Exception e) {
	            	serverApp.port = 1099;
	            }
	            System.out.println("Running server on port " + serverApp.port);
	            LocateRegistry.createRegistry(serverApp.port);
	            Naming.rebind("//localhost:"+serverApp.port+"/BackuperServer", serverApp);
	        } catch (Exception e) {
	            System.err.println(e.toString());
	            System.err.println("Something is wrong with chosen port. Please, try again");
	            String [] x = new String[0];
	            main(x);
	        }
	    }
	//konstruktor, ustala liczbę połączonych klientów na 0, tworzy wektory połączonych klientów
	//i pozostałych klientów i wczytuje ustawienia z pliku settings.txt
	public ServerApp() throws RemoteException {
		super();		
		numberOfConnectedClients = 0;
		connectedClients = new Vector<Client>();
		clients = new Vector<Client>();
		readSettings();
	}

	//wgranie pliku na serwer
	public void uploadFile(String username, String fileName, long lastModified, RemoteInputStream remoteFileData) throws RemoteException {
		//wyszukaj, czy klient posiada już taki plik
		for (Client client : connectedClients) {
			if (client.getUsername().equals(username)) {
				for (File f : client.getClientFiles()) {
					if (f.getName().equals(fileName)) {
						int index = client.getClientFiles().indexOf(f);
						client.getClientFiles().remove(index);
						client.getClientFilesLastModified().remove(index);
						client.getClientFilesMd5hex().remove(index);
						break;
					}
				}
			}
		}
		InputStream istream = null;
		try {
			istream = RemoteInputStreamClient.wrap(remoteFileData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream ostream = null;
		    try {
		    	File receivedFile = new File(username + "/" +fileName);
		        ostream = new FileOutputStream(receivedFile);
		        System.out.println("Writing file " + receivedFile);
		        byte[] buf = new byte[1024];
		        int bytesRead = 0;
		        while((bytesRead = istream.read(buf)) >= 0) {
		        	ostream.write(buf, 0, bytesRead);
		        }	
		        ostream.flush();
		        // liczenie funkcji skrótu md5
		        for (Client client : connectedClients) {
		        	if (client.getUsername().equals(username)) {
		        		client.getClientFiles().add(receivedFile);
		        		int temp = client.getClientFiles().indexOf(receivedFile);
		        	  	client.getClientFilesLastModified().add(temp, lastModified);
		        	    client.getClientFilesMd5hex().add(temp, " ");
		        	    if (md5gen == null) { // jeśli wątek nie został jeszcze utworzony
							System.out.println("Started calculating md5 for "+ receivedFile.getName());
							md5gen = new Md5Generator(receivedFile.getAbsolutePath(), client.getUsername());
							md5gen.addListener(this);
							md5gen.start();
							client.setIsCalculatingMd5(true);
						} else if (!md5gen.isAlive()) { // jeśli wątek został utworzony, ale nie działa
							System.out.println("Started calculating md5 for "+ receivedFile.getName());
							md5gen = new Md5Generator(receivedFile.getAbsolutePath(), client.getUsername());
							md5gen.addListener(this);
							md5gen.start();
							client.setIsCalculatingMd5(true);
						} else { //jeśli wątek działa
							System.out.println("Calculating Md5 for " + receivedFile. getName() + " queued");
							md5gen.addToQueue(receivedFile.getAbsolutePath());
							client.setIsCalculatingMd5(true);
						}
		        	  	break;
		        	 }
		         }
		         System.out.println("Finished writing file " + receivedFile);
		        
		          
		        } catch (IOException e) {
					e.printStackTrace();
				} finally {
		          try {
		            if(istream != null) {
		              istream.close();
		            }
		          } catch (IOException e) {
					e.printStackTrace();
				} finally {
		            if(ostream != null) {
		              try {
						ostream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		            }
		          }
		        }
		    //zapisanie ustawien do pliku settings.txt
		    writeSettings();
		}
	//pobranie pliku z serwera
	public RemoteInputStream getFile(String username, String fileName) throws RemoteException {
		
		SimpleRemoteInputStream istream = null;
		//poszukaj, czy klient ma taki plik
		for (Client client : connectedClients) {
			if (client.getUsername().equals(username)) {
				for (File f : client.getClientFiles()) {
					System.out.println(f.getName());
					System.out.println(fileName);
					if (f.getName().equals(fileName)) {
						try {
							istream = new SimpleRemoteInputStream(new FileInputStream(f.getAbsolutePath()));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} 
						finally {
						//	istream.close();
						}
						break;
					}
				}
			}
		}
		//zapisanie ustawien do settings.txt
		writeSettings();
		return istream;
	}
	//pobranie hashu md5 danego pliku
	public String getFileMD5(String username, String fileName) throws RemoteException {
		String temp = new String();
		for (Client client : connectedClients) {
			if (client.getUsername().equals(username)) {
				if (!client.isCalculatingMd5()) {
					int index = -1;
					for (File f : client.getClientFiles()) {
						if (f.getName().equals(fileName)) {
							index = client.getClientFiles().indexOf(f);
						}
					}
					if (index != -1) {
						temp = client.getClientFilesMd5hex().get(index);
					} else return null;
				} else return null;
			}
		}
		return temp;
	}

	//pobiera liste nazw plikow na serwerze
	public Vector<String> getListOfFilesOnServer(String username) throws RemoteException {
		Vector<String> v = new Vector<String>();
		for (Client client : connectedClients) {
			if (client.getUsername().equals(username)) {
				for (File f : client.getClientFiles()) {
					v.add(f.getName());
				}
			}
		}
		//zapisanie ustawien
		writeSettings();
		return v;
	}

	//zalogowanie sie na serwer
	@Override
	public boolean logIn(String username, String password) throws RemoteException {
		boolean temp = false;
		for (Client client : clients) {
			if (client.getUsername().equals(username)) {
				if (client.getPassword().equals(password)) {
					//kopiuje klienta z wektora Clients do connectedClients
					connectedClients.add(client);
					numberOfConnectedClients++;
					temp = true;
				} else {
					temp = false;
				}
			} 
		}
		writeSettings();
		return temp;
	}
	//rejestracja nowego uzytkownika
	public boolean register(String username, String password)
			throws RemoteException {
		clients.add(new Client(username, password));
		//tworzenie folderu uzytkownika i zapisanie ustawien
		File dir = new File(username);
		dir.mkdir();
		writeSettings();
		return true;
	}
	//usuniecie danego pliku z serwera
	public void removeSelectedFile(String username, String fileName) throws RemoteException {
		for (Client client : connectedClients) {
			if (client.getUsername().equals(username)) {
				System.out.println("username found");
				for (File f : client.getClientFiles()) {
					if (f.getName().equals(fileName)) {
						System.out.println("file found");
						client.getClientFilesLastModified().remove(client.getClientFiles().indexOf(f));
						client.getClientFilesMd5hex().remove(client.getClientFiles().indexOf(f));
						client.getClientFiles().remove(f);
						if (f.delete()) System.out.println("file deleted");
						break;
					}
				}
			}
		}
		//zapisanie ustawien
		writeSettings();
	}
	
	public void disconnect(String username) throws RemoteException {
	
		//szuka connectedClienta z danym imieniem i Clienta z danym imieniem, usuwa wpis w Clients i zastępuje go tym z connectedClients
		
		for (Client connectedClient : connectedClients) {
			if (connectedClient.getUsername().equals(username)) {
				for (Client client : clients) {
					if (client.getUsername().equals(username)) {
						clients.remove(client);
						clients.add(new Client(connectedClient));
						connectedClients.remove(connectedClient);
						numberOfConnectedClients--;
					}
				}
			}
		}	
		writeSettings();
	}

	//zwraca mapę plików na serwerze w postaci <nazwaPliku, lastModified>
	public HashMap<String, Long> getMapOfFilesOnServer(String username) throws RemoteException {
		HashMap<String, Long> tempMap = new HashMap<String, Long>();
		for (Client client : connectedClients) {
			if (client.getUsername().equals(username)) {
				for (File f : client.getClientFiles()) {
					tempMap.put(f.getName(), client.getClientFilesLastModified().elementAt(client.getClientFiles().indexOf(f)));
				}
				break;
			}
		}
		//zapisanie ustawien
		writeSettings();
		return tempMap;
	}
	
	//wczytanie ustawien z serwera
	private void readSettings() {
		try {
			//zaladowanie pliku konfiguracyjnego
			Reader reader = new Reader("settings.txt");
			
			//czytanie ustawien
			if (reader.getInputFile()!=null) {
				String tempUsername;
				String tempPassword;
				Client tempClient = null;
				for (String s : reader.getInputFile()) {
					if (s.charAt(0) == (char)'#' && s.charAt(1) == (char)'#' && s.charAt(2) == (char)'#') {
						if (tempClient != null) clients.add(tempClient);
						tempClient = new Client(); 
						tempUsername = s.substring(3);
						tempClient.setUsername(tempUsername);
					} else if (s.charAt(0) == (char)'%' && s.charAt(1) == (char)'%' && s.charAt(2) == (char)'%') {
						tempPassword = s.substring(3);
						tempClient.setPassword(tempPassword);
					//} else if (s == "~EOF") {
				//		if (tempClient != null) clients.add(tempClient);
					//	break;
					} else {
						// s wyglada tak: [sciezka dostepu do pliku]___[last modified pliku]___[md5 pliku]
						try {
						String[] str = s.split("___");
						File f = new File(str[0]);
						tempClient.getClientFiles().add(f);
						tempClient.getClientFilesLastModified().add(Long.parseLong(str[1]));
						tempClient.getClientFilesMd5hex().add(str[2]);
						} catch (ArrayIndexOutOfBoundsException e) {
						}
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//zapisanie ustawien
	private void writeSettings() {
		Vector<Client> tempClients = new Vector<Client>();
		for (Client c : clients) {
			tempClients.add(c);
		}
		for(Client c : connectedClients) {
			boolean inVector = false;
			for (Client c1 : tempClients) {
				if (c1.getUsername().equals(c.getUsername())) {
					c1 = c;
					inVector = true;
				}
			}
			if (!inVector) tempClients.add(c);
		}
		
		Vector<String> tempSettings = new Vector<String>();
		
		for (Client c : tempClients) {
			tempSettings.add("###" + c.getUsername());
			tempSettings.add("%%%" + c.getPassword());
			if (!c.getClientFiles().isEmpty()) {
				for (File file : c.getClientFiles()) {
					tempSettings.add(file.getAbsolutePath() + "___" + c.getClientFilesLastModified().get(c.getClientFiles().indexOf(file)) + "___" + c.getClientFilesMd5hex().elementAt(c.getClientFiles().indexOf(file)));
				}
			}
		}
		tempSettings.add("###EOF");
		
		Writer w = new Writer(tempSettings);
	}
	
	//funkcja wywolywana po tym, jak watek powiadomi listener ze skonczyl dzialanie
	public void notifyOfThreadComplete(Thread thread) {		
		//przypisanie md5 do danego pliku
		if(thread instanceof Md5Generator) {
			for (Client client : connectedClients) {
				if (((Md5Generator) thread).getUsername().equals(client.getUsername())) {
					for (File f : client.getClientFiles()) {
						if (f.getAbsolutePath().equals(((Md5Generator) thread).getFilePath())) {
							client.getClientFilesMd5hex().add(client.getClientFiles().indexOf(f), ((Md5Generator) thread).getMd5());
							System.out.println("Finished calculating md5 for file " + f.getName());
							System.out.println(((Md5Generator) thread).getMd5());
							client.setIsCalculatingMd5(false);
							}
						}
					break;
					}
				}
			}
	}
}
