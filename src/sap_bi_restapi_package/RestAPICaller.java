package sap_bi_restapi_package;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;

import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;


public class RestAPICaller {
	
	private String USER_AGENT = "Mozilla/5.0";
	private CrashAndRunChecker crashruncheck;
	
	public RestAPICaller(CrashAndRunChecker tempcrashruncheck) {
		crashruncheck = tempcrashruncheck;
	}
	
	
	// HTTP PUT request
	public RestAPIResponse sendPut_XML_token(String url, String xmlbodystr, String given_token) throws Exception {

		crashruncheck.println("\nSending HTTP 'PUT' XML TOKEN request to URL : " + url);
			
		URL obj = new URL(url);
				
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("PUT");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setRequestProperty("X-SAP-LogonToken", given_token);
		con.setRequestProperty("Accept", "application/xml");
		con.setRequestProperty("Content-Type", "application/xml");

			
		//send body
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(xmlbodystr);
		wr.flush();
		wr.close();

		RestAPIResponse myresponse = new RestAPIResponse();
		myresponse.responseCode = con.getResponseCode();

		crashruncheck.println("Response Code : " + myresponse.responseCode);

		if (myresponse.responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			myresponse.response = response.toString();
		}
			
		return myresponse;
	}//sendPut_XML_token
	
	
	// HTTP GET request
	public RestAPIResponse sendGet_token_file_output(String url, String acceptstr, String filename, String given_token) throws Exception {
			
			crashruncheck.println("\nSending HTTP 'GET' TOKEN request to URL : " + url);
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("X-SAP-LogonToken", given_token);
			con.setRequestProperty("Accept", acceptstr);
			
			RestAPIResponse myresponse = new RestAPIResponse();
			myresponse.responseCode = con.getResponseCode();

			crashruncheck.println("Response Code : " + myresponse.responseCode);

			if (myresponse.responseCode == 200) {
				FileOutputStream myfile = new FileOutputStream(filename);
				byte[] mybuf = new byte[16384];
				BufferedInputStream instream = new BufferedInputStream(con.getInputStream()); 
				
				int actuallyread = 0;
				while (actuallyread != -1) {
					int readsize = 0;
					int available = instream.available();
					if (available >= 16384)
						readsize = 16384;
					else if (available == 0)
						readsize = 1;
					else
						readsize = available;
					actuallyread = instream.read(mybuf, 0, readsize);
				
					if (actuallyread != -1)
						myfile.write(mybuf, 0, actuallyread);
				}
				myfile.close();
				instream.close();
			}
			else {
				
				InputStream my_input_stream = null;
				try {
					my_input_stream = con.getInputStream();
				}
				catch (Throwable tt) {
					if (myresponse.responseCode == 500) {
						String debugstr = String.format("RestAPICaller: sendGet_token_file_output: EXCEPTION CAUGHT trying to get con.getInputStream() on responseCode==500 url=%s ..... 500 means internal SAP BI v4.1SP3 error trying to get file output after refresh of document", url);
						crashruncheck.println(debugstr);
					}
					else {
						String debugstr = String.format("RestAPICaller: sendGet_token_file_output: EXCEPTION CAUGHT trying to get con.getInputStream() on responseCode==%d url=%s ..... maybe another SAP BI v4.1SP3 error trying to get file output after refresh of document", myresponse.responseCode, url);
						crashruncheck.println(debugstr);
					}
				}
				
				if (my_input_stream != null) {
					BufferedReader in = new BufferedReader(new InputStreamReader(my_input_stream));
					String inputLine;
					StringBuffer response = new StringBuffer();
	
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					myresponse.response = response.toString();
				}
				else {
					crashruncheck.println("RestAPICaller: sendGet_token_file_output: my_input_stream == null");
				}
			}
			
			return myresponse;
	}//sendGet_token_file_output
	
	
	// HTTP GET request
	public RestAPIResponse sendGet_token(String url, String given_token, boolean json_response_not_xml_response) throws Exception {
			
			crashruncheck.println("\nSending HTTP 'GET' TOKEN request to URL : " + url);
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("X-SAP-LogonToken", given_token);
			
			if (json_response_not_xml_response)
				con.setRequestProperty("Accept", "application/json");
			else
				con.setRequestProperty("Accept", "application/xml");
			
			RestAPIResponse myresponse = new RestAPIResponse();
			myresponse.responseCode = con.getResponseCode();

			crashruncheck.println("Response Code : " + myresponse.responseCode);

			if (myresponse.responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				myresponse.response = response.toString();
			}
			
			return myresponse;
	}//sendGet_token
	
	
	// HTTP GET request
	public RestAPIResponse sendGet(String url) throws Exception {
		
		crashruncheck.println("\nSending HTTP 'GET' request to URL : " + url);
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		
		RestAPIResponse myresponse = new RestAPIResponse();
		myresponse.responseCode = con.getResponseCode();

		crashruncheck.println("Response Code : " + myresponse.responseCode);

		if (myresponse.responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			myresponse.response = response.toString();
		}
		
		return myresponse;
	}//sendGet
	
	
	// HTTP POST request with token
	public RestAPIResponse sendPost_token(String url, String given_token) throws Exception {
			
			crashruncheck.println("\nSending HTTP 'POST' TOKEN request to URL : " + url);
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();


			con.setRequestMethod("POST");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("X-SAP-LogonToken", given_token);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Accept", "application/xml");
			
			RestAPIResponse myresponse = new RestAPIResponse();
			myresponse.responseCode = con.getResponseCode();

			crashruncheck.println("Response Code : " + myresponse.responseCode);

			if (myresponse.responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				myresponse.response = response.toString();
			}
			
			return myresponse;
	}//sendPost_token
	
		
	// HTTP POST request
	public RestAPIResponse sendPost_XML(String url, String xmlbodystr) throws Exception {

		crashruncheck.println("\nSending HTTP 'POST' request with XML body to URL : " + url);
		
		URL obj = new URL(url);
			
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		//without Accept in header you will get 406 error
		//Accept: */* will give you 415 error 
		//Accept:application/xml
		con.setRequestProperty("Accept", "application/xml");
		con.setRequestProperty("Content-Type", "application/xml");

		
		//send body
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(xmlbodystr);
		wr.flush();
		wr.close();

		RestAPIResponse myresponse = new RestAPIResponse();
		myresponse.responseCode = con.getResponseCode();

		crashruncheck.println("Response Code : " + myresponse.responseCode);

		if (myresponse.responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			myresponse.response = response.toString();
		}
		
		return myresponse;
	}//sendPost_XML
	
	
	
	// HTTPS GET request
	public RestAPIResponse sendGet_https(String url) throws Exception {
		
		crashruncheck.println("\nSending HTTPS 'GET' request to URL : " + url);
		
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		RestAPIResponse myresponse = new RestAPIResponse();
		myresponse.responseCode = con.getResponseCode();
		
		crashruncheck.println("Response Code : " + myresponse.responseCode);

		if (myresponse.responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			myresponse.response = response.toString();
		}
		
		return myresponse;
	}
	
	//HTTPS POST request
	public RestAPIResponse sendPost_https_XML(String url, String xmlbodystr) throws Exception {

		crashruncheck.println("\nSending HTTPS 'POST' request with XML body to URL : " + url);
		
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("POST");
		
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		//without Accept in header you will get 406 error
		//Accept: */* will give you 415 error 
		//Accept:application/xml
		con.setRequestProperty("Accept", "application/xml");
		con.setRequestProperty("Content-Type", "application/xml");
		
		//send body
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(xmlbodystr);
		wr.flush();
		wr.close();

		RestAPIResponse myresponse = new RestAPIResponse();
		myresponse.responseCode = con.getResponseCode();

		crashruncheck.println("Response Code : " + myresponse.responseCode);

		if (myresponse.responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			myresponse.response = response.toString();
		}
		
		return myresponse;
	}

}//class
