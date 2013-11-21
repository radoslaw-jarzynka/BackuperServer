package utils;

import java.util.Vector;
import java.io.*;

public class Reader {
	
	private Vector<String> inputFile;
	private BufferedReader reader;
	
	public Vector<String> getInputFile() {
		return inputFile;
	}
	
	public Reader(String fileName) {
		inputFile = new Vector<String>();
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} 
		catch (FileNotFoundException e) {
			//System.out.println(e);
			//System.out.println("Wrong File Name!");
			inputFile = null;
		}
		while (true) {
			try{
				String line = reader.readLine();
				if (line == null) break;
				inputFile.add(line);
			}
			catch (IOException e) {
				break;
			}
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.toString();
		}
	}	
	
	
}
