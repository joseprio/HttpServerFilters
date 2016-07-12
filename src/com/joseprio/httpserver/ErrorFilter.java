package com.joseprio.httpserver;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;


public class ErrorFilter extends Filter {

	private static final String FILTER_DESC = "ErrorFilter will show error pages when requests haven't been handled properly";
	private static final String TEMPLATE_ENCODING = "ISO-8859-1";
	
	// In code configuration
	
	// If the response code is an error, force ErrorFilter's error page
	private boolean forceErrorPage = false;
	
	// Path to the template file to use for errors
	private String errorTemplatePath = "/error.html";
	
	// END in code configuration
	
	private String errorTemplate;
	
	public ErrorFilter() {
		// Read template
		initFilter();
	}
	
	private void initFilter() {
		InputStream res =
				getClass().getResourceAsStream(errorTemplatePath);
		
		errorTemplate = readStream(res);
	}

	@Override
	public String description() {
		return FILTER_DESC;
	}

	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

		// Pre process
		ErrorFilterOutputStream filterOutputStream = new ErrorFilterOutputStream(exchange, forceErrorPage, exchange.getResponseBody());
		exchange.setStreams(null, filterOutputStream);
		
		// Chain the request to request handler
		chain.doFilter(exchange);
		
		try {
			// Post process
			int responseCode = exchange.getResponseCode();
			if (responseCode >= 400 && responseCode < 600 && !filterOutputStream.hasSentFirstByte()) {
				String errorPage = errorTemplate
						.replaceAll("\\{\\{CODE\\}\\}", Integer.toString(responseCode))
						.replaceAll("\\{\\{DESCRIPTION\\}\\}", getErrorDescription(responseCode));
				filterOutputStream.forceWrite(errorPage.getBytes(TEMPLATE_ENCODING));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		filterOutputStream.flush();
		filterOutputStream.forceClose();
	}
	
	private static String getErrorDescription(int code) {
		switch (code) {
		case 400:
			return "Bad request";
		case 401:
			return "Unauthorized";
		case 403:
			return "Forbidden";
		case 404:
			return "Page not found";
		case 410:
			return "Gone";
		case 500:
			return "Internal server error";
		case 501:
			return "Not implemented";
		case 503:
			return "Service unavailable";
		}
		
		return "An error has occurred";
	}
	
	private static String readStream(InputStream is) {
	    StringBuilder sb = new StringBuilder(512);
	    Reader r = null;
	    
	    try {
	        r = new InputStreamReader(is, "UTF-8");
	        int c = 0;
	        while ((c = r.read()) != -1) {
	            sb.append((char) c);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    } finally {
	    	try {
	    		r.close();
	    	} catch (NullPointerException npex) {
	    	} catch (IOException ioex) {
	    	}
	    }
	    return sb.toString();
	}
}

class ErrorFilterOutputStream extends OutputStream {
	private HttpExchange he;
	private boolean inhibitOutputOnError = false;
	private OutputStream os;
	private boolean inhibitOutput = false;
	private boolean sentFirstByte = false;
	
	public ErrorFilterOutputStream(HttpExchange he, boolean inhibitOutputOnError, OutputStream os) {
		this.inhibitOutputOnError = inhibitOutputOnError;
		this.he = he;
		this.os = os;
	}
	
	@Override
	public void write(int b) throws IOException {
		if (!inhibitOutput) {
			if (sentFirstByte && inhibitOutputOnError) {
				int responseCode = he.getResponseCode();
				if (responseCode >= 400 && responseCode < 600) {
					inhibitOutput = true;
				} else {
					os.write(b);
					sentFirstByte = true;
				}
			} else {
				os.write(b);
			}
		}
	}
	
	public void forceWrite(int b) throws IOException {
		os.write(b);
	}
	
	public void forceWrite(byte[] array) throws IOException {
		for (int c=0;c<array.length;c++) {
			forceWrite(array[c]);
		}
	}
	
	@Override
	public void flush() throws IOException {
		if (sentFirstByte) {
			os.flush();
		}
	}
	
	@Override
	public void close() {
		// Do nothing
	}
	
	public void forceClose() throws IOException {
		os.close();
	}
	
	public boolean hasSentFirstByte() {
		return sentFirstByte;
	}
}