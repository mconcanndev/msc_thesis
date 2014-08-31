package com.acme.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			URL request = new URL("http://localhost:8080/chatroom");
			URLConnection connection = request.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
				System.out.println(inputLine);
			in.close();
		}
		catch (Exception e) {

		}
	}
}
