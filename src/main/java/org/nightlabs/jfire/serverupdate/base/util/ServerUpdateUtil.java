/**
 * 
 */
package org.nightlabs.jfire.serverupdate.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author abieber
 *
 */
public class ServerUpdateUtil {

	protected ServerUpdateUtil() {
	}

	public static boolean prompt(String message, String expectedAnswer) {
		
		System.out.println(message);

		//  open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String answer = null;

		try {
			answer = br.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read user input!");
			return false;
		}
		return answer.toLowerCase().equals(expectedAnswer);
	}

}
