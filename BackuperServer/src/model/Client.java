/**
 * created on 22:59:04 29 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package model;

import java.io.File;
import java.util.Vector;

public class Client {
	
	private String username;
	private String password;
	// file, lastModified,md5hex
	//private Map<File, MapModel> clientFilesMap;
	private Vector<File> clientFiles;
	private Vector<Long> clientFilesLastModified;
	private Vector<String> clientFilesMd5hex;
	
	private boolean isCalculatingMd5;
	
	public Client() {
		
		setIsCalculatingMd5(false);
		username = new String();
		password = new String();
		
		clientFiles = new Vector<File>();
		clientFilesLastModified = new Vector<Long>();
		clientFilesMd5hex = new Vector<String>();
	}
	
	public Client(String username, String password) {
		this.username = username;
		this.password = password;
		
		clientFiles = new Vector<File>();
		clientFilesLastModified = new Vector<Long>();
		clientFilesMd5hex = new Vector<String>();
	}
	
	public Client(Client c) {
		this.username = c.username;
		this.password = c.password;
		
		clientFiles = c.getClientFiles();
		clientFilesLastModified = c.getClientFilesLastModified();
		clientFilesMd5hex = c.getClientFilesMd5hex();
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the clientFiles
	 */
	public Vector<File> getClientFiles() {
		return clientFiles;
	}

	/**
	 * @param clientFiles the clientFiles to set
	 */
	public void setClientFiles(Vector<File> clientFiles) {
		this.clientFiles = clientFiles;
	}

	/**
	 * @return the clientFilesLastModified
	 */
	public Vector<Long> getClientFilesLastModified() {
		return clientFilesLastModified;
	}

	/**
	 * @param clientFilesLastModified the clientFilesLastModified to set
	 */
	public void setClientFilesLastModified(Vector<Long> clientFilesLastModified) {
		this.clientFilesLastModified = clientFilesLastModified;
	}

	/**
	 * @return the clientFilesMd5hex
	 */
	public Vector<String> getClientFilesMd5hex() {
		return clientFilesMd5hex;
	}

	/**
	 * @param clientFilesMd5hex the clientFilesMd5hex to set
	 */
	public void setClientFilesMd5hex(Vector<String> clientFilesMd5hex) {
		this.clientFilesMd5hex = clientFilesMd5hex;
	}

	/**
	 * @return the isCalculatingMd5
	 */
	public boolean isCalculatingMd5() {
		return isCalculatingMd5;
	}

	/**
	 * @param isCalculatingMd5 the isCalculatingMd5 to set
	 */
	public void setIsCalculatingMd5(boolean isCalculatingMd5) {
		this.isCalculatingMd5 = isCalculatingMd5;
	}
	
}