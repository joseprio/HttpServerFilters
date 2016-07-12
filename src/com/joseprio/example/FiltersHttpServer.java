package com.joseprio.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.joseprio.httpserver.ErrorFilter;
import com.joseprio.httpserver.GzipFilter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class FiltersHttpServer {

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/info", new InfoHandler());
    HttpContext hc1 = server.createContext("/error", new ErrorHandler());
    hc1.getFilters().add(new ErrorFilter());
    
    HttpContext hc2 = server.createContext("/gzip", new GzipHandler());
    hc2.getFilters().add(new GzipFilter());

    server.setExecutor(null); // creates a default executor
    server.start();
    System.out.println("The server is running");
  }

  // http://localhost:8000/info
  static class InfoHandler implements HttpHandler {
    public void handle(HttpExchange httpExchange) throws IOException {
      String response = "<html><body><ul><li>Use /error/[[error code]] to receive an error response (i.e. /error/404)</li><li>Use /gzip to get a gzipped HTTP reply</li></ul></body></html>";
      FiltersHttpServer.writeResponse(httpExchange, response.toString());
    }
  }

  static class ErrorHandler implements HttpHandler {
	    public void handle(HttpExchange httpExchange) throws IOException {
	      String requestURI = httpExchange.getRequestURI().toString();
	      int errorCode = Integer.parseInt(requestURI.substring(requestURI.lastIndexOf('/')+1));
	      if (errorCode < 1 || errorCode == 200) {
	          String response = "OK response";
	          FiltersHttpServer.writeResponse(httpExchange, response.toString());
	      } else {
		      httpExchange.sendResponseHeaders(errorCode, 0);
		      OutputStream os = httpExchange.getResponseBody();
		      os.close();
	      }
	  }
  }

  static class GzipHandler implements HttpHandler {
	    public void handle(HttpExchange httpExchange) throws IOException {
          String response = "<html><body><h1>Gzipped reply!</h1><h2>The transferred data should be smaller than the size</h2><p></i>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</i></p></body></html>";
          // The length in the header cannot be calculated, so put 0 instead
          httpExchange.sendResponseHeaders(200, 0);
          OutputStream os = httpExchange.getResponseBody();
          os.write(response.getBytes());
          os.close();
        }
  }

  public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
    httpExchange.sendResponseHeaders(200, response.length());
    OutputStream os = httpExchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

}
