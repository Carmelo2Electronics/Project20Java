

package project20java;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Project20Java {
	public static void main(String[] args) throws URISyntaxException, IOException {
		new Configure();
	}
}


//888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888
class Configure{
	
	private int port=9999;
	private  HttpServer httpServer;
	private String valores[]= {"Led1 OFF","Led2 OFF","Led3 OFF"};
	
	public Configure() throws IOException, URISyntaxException {
		
    	Host host=new Host(port);  	
    	String DireccionIP=host.getIPadress();	

		httpServer = HttpServer.create(new InetSocketAddress(port), 0);		
		
		httpServer.createContext("/", new Home(DireccionIP, port));
		httpServer.createContext("/shutdown", new ShutDown(httpServer));
		httpServer.createContext("/execute1", new Execute1(DireccionIP, port, valores));
		httpServer.createContext("/execute2", new Execute2(DireccionIP, port, valores));

    	httpServer.setExecutor(null);
    	httpServer.start(); 	
	}
}
//888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888

//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class Host{
	private String SOName;
	private String stringIPadress;		
	private StringBuilder stringBuilder;
	private String s;
	private String stringText;
	private String[] splits;
	
	public Host(int port) throws IOException, URISyntaxException{

		SOName=System.getProperty("os.name");
		
		if(SOName.equals("Linux")) {		//You are in Linux

			new GPIO();		// Load Pi4J.	
			
			//I get the IP address in linux
			stringBuilder = new StringBuilder();
			Process p = Runtime.getRuntime().exec("ifconfig wlan0");                                                                                                                                               
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((s = stdInput.readLine()) != null) {
				stringBuilder.append(s);				
			}	
			stringText=stringBuilder.toString();
			splits = stringText.split("inet|netmask");	
			stringIPadress=splits[1].toString();
			stringIPadress=stringIPadress.replaceAll("\\s", "");	
			//I get the IP address in linux
						
			Runtime.getRuntime().exec("xdg-open http://localhost:"+ port);		//Open main page
			
		}else {								//You're not on linux
			JOptionPane.showMessageDialog(null, "This program must be run on Raspberry", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}		
	}

	public String getIPadress() {
		return stringIPadress;
	}
}
//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

//tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt
class GPIO{
	
	static GpioController gpioController;
	static GpioPinDigitalOutput GPIO_1;
	static GpioPinDigitalOutput GPIO_2;
	static GpioPinDigitalOutput GPIO_3;
	
	public GPIO() {	
		gpioController = GpioFactory.getInstance();
		GPIO_1 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_01, "01", PinState.LOW);
		GPIO_2 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02, "02", PinState.LOW);
		GPIO_3 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03, "03", PinState.LOW);
	}
	
	static void turnOffGPIO() {
		GPIO_1.low();
		GPIO_2.low();
		GPIO_3.low();
	}
}
//tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt

//PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
class Home implements HttpHandler{
	
	private int port;
	private String stringIPadress;
	private OutputStream outputStream;
	private String response;

	
	public Home(String IPadress, int port) {
		this.port=port;
		this.stringIPadress=IPadress;		
	}
	
	public void handle(HttpExchange he) throws IOException {

		response=
				"<!DOCTYPE html>"+
	    		"<html>"+
	    		"<body>"+
	    		
	    		"<h1> Home </h1>" +
	    		"<h2>" + "IP adress  "+ stringIPadress +":"+ port +"</h2>"+
	    						
				"<form action=\"/execute1\" method=\"get\">" + 
				"<p> START </p>" +  
				"<button name=\"subject\" type=\"submit\" value=\"CLICK\">" + "CLICK" + "</button>" + 		
				"</form>"+
									
				"<p>Shut Down </p>" + 
				"<form method=\"get\">" + 
				"<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/shutdown'\">" + 
				"</form>"+			
							
				"</body>"+
				"</html>";

		he.sendResponseHeaders(200, response.length());            
		outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();	
	}
}
//PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP

///wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww
class Execute1 implements HttpHandler{
	
	private OutputStream outputStream;
	private int port;
	private String stringIPadress;
	private String stringReceived;
	private String[] valores;
	private String response;
	
	public Execute1(String IPadress, int port, String[] valores) {
		this.valores=valores;
		this.port=port;
		this.stringIPadress=IPadress;		
	}

	public void handle(HttpExchange he) throws IOException {
		
		stringReceived=he.getRequestURI().getQuery();			
		executeMethodPut1();
		
		response=		
				"<!DOCTYPE html>"+
	    		"<html>"+
	    		"<body>"+
	    		
	    		"<h1> Running </h1>" +
	    		"<h2>" + "IP adress  "+ stringIPadress +":"+ port +"</h2>"+
	  
				"<form action=\"/execute2\" method=\"get\">" + 				
				"<br>"+				
				"<button name=\"subject\" type=\"submit\" value=\"Led1\">" + valores[0] + "</button>" + 		
				"<br>"+
				"<br>"+				
				"<button name=\"subject\" type=\"submit\" value=\"Led2\">" + valores[1] + "</button>" + 				
				"<br>"+
				"<br>"+				
				"<button name=\"subject\" type=\"submit\" value=\"Led3\">" + valores[2] + "</button>" +			
				"</form>"+
				
				"<p> Shut Down </p>" + 
				"<form method=\"get\">" + 
				"<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/shutdown'\">" + 
				"</form>"+	
			
				"</body>"+
				"</html>";
			
		he.sendResponseHeaders(200, response.length());            
		outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();	
	}
	
	public void executeMethodPut1() {

		if(stringReceived.equals("subject=Led1")) {	
			GPIO.GPIO_1.low();
			valores[0]="Led1 OFF";
		}		
		if(stringReceived.equals("subject=Led2")) {	
			GPIO.GPIO_2.low();
			valores[1]="Led2 OFF";
		}		
		if(stringReceived.equals("subject=Led3")) {	
			GPIO.GPIO_3.low();
			valores[2]="Led3 OFF";
		}
	}
}
///wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww

//bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
class Execute2 implements HttpHandler{
	
	private OutputStream outputStream;
	private int port;
	private String stringIPadress;
	private String stringReceived;
	private String[] valores;
	private String response;
	
	public Execute2(String IPadress, int port, String[] valores) {
		this.valores=valores;
		this.port=port;
		this.stringIPadress=IPadress;		
	}

	public void handle(HttpExchange he) throws IOException {
		
		stringReceived=he.getRequestURI().getQuery();			
		executeMethodPut2();

		response=		
				"<!DOCTYPE html>"+
	    		"<html>"+
	    		"<body>"+
	    		
	    		"<h1> Running </h1>" +
	    		"<h2>" + "IP adress  "+ stringIPadress +":"+ port +"</h2>"+
				
				"<form action=\"/execute1\" method=\"get\">" + 				
				"<br>"+				
				"<button name=\"subject\" type=\"submit\" value=\"Led1\">" + valores[0] + "</button>" + 			
				"<br>"+
				"<br>"+			
				"<button name=\"subject\" type=\"submit\" value=\"Led2\">" + valores[1] + "</button>" + 				
				"<br>"+
				"<br>"+			
				"<button name=\"subject\" type=\"submit\" value=\"Led3\">" + valores[2] + "</button>" +
				"</form>"+
				
				"<p> Shut Down </p>" + 
				"<form method=\"get\">" + 
				"<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/shutdown'\">" + 
				"</form>"+			
			
				"</body>"+
				"</html>";
		
		he.sendResponseHeaders(200, response.length());            
		outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();		
	}
	
	public void executeMethodPut2() {
		
		if(stringReceived.equals("subject=Led1")) {	
			GPIO.GPIO_1.high();
			valores[0]="Led1  ON";
		}		
		if(stringReceived.equals("subject=Led2")) {	
			GPIO.GPIO_2.high();
			valores[1]="Led2  ON";
		}		
		if(stringReceived.equals("subject=Led3")) {	
			GPIO.GPIO_3.high();
			valores[2]="Led3  ON";
		}
	}
}
//bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb

//Hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class ShutDown implements HttpHandler{
	
	private  HttpServer httpServer;
	private String response;
	
	public ShutDown(HttpServer httpServer) {
		this.httpServer= httpServer;
	}
	
	public void handle(HttpExchange he) throws IOException {
		
		response=				
				"<!DOCTYPE html>"+
	    		"<html>"+
	    		"<body>"+
	    		
				"<h1> Goodbye </h1>" +
				"</body>"+
				"</html>";
		
		he.sendResponseHeaders(200, response.length());            
		OutputStream outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close(); 
		httpServer.stop(1);
		GPIO.gpioController.shutdown();
		GPIO.turnOffGPIO() ;
		System.exit(0);
	}
}
//Hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

