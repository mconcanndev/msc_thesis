package com.acme.server.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtils {

	public static String InputStringToString(InputStream data) {

		StringBuilder crunchifyBuilder = new StringBuilder();
        
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(data));
            String line = null;
            
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
        }
        catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        
        return crunchifyBuilder.toString();
	}
}
