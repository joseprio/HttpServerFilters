package com.joseprio.httpserver;


import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;


public class GzipFilter extends Filter {

	private static final String FILTER_DESC = "This filter will compress the output with Gzip in case the client supports it";
	private static final String GZIP = "gzip";
	
	public GzipFilter() {
	}
	
	@Override
	public String description() {
		return FILTER_DESC;
	}

	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
try {
		// Pre process
		String acceptEncodingHeader = exchange.getRequestHeaders().getFirst("Accept-Encoding");
		
		if (acceptEncodingHeader != null && acceptEncodingHeader.indexOf(GZIP) > -1) {
			// Client supports gzip encoding
			exchange.getResponseHeaders().add("Content-Encoding", GZIP);
			OutputStream processedStream = new DelayedGZIPOutputStream(exchange.getResponseBody());
			exchange.setStreams(null, processedStream);
		}
		
		// Chain the request to request handler
		chain.doFilter(exchange);
} catch (Exception ex) {
	ex.printStackTrace();
}
	}
	
}

class DelayedGZIPOutputStream extends OutputStream {
	private boolean sentFirstByte = false;
	private OutputStream targetStream = null;
	
	public DelayedGZIPOutputStream(OutputStream targetStream) {
		this.targetStream = targetStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		if (!sentFirstByte) {
			targetStream = new GZIPOutputStream(targetStream);
			sentFirstByte = true;
		}
		targetStream.write(b);
	}
	
	@Override
	public void flush() throws IOException {
		targetStream.flush();
	}
	@Override
	public void close() throws IOException {
		targetStream.close();
	}
}