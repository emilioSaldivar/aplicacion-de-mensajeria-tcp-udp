package udp_chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class MessageSender implements Runnable {
	public final static int PORT = 2020;//constante indica la numeracion del puerto utilizado
	private DatagramSocket socket;//conexion establecida para datagramas
	private String hostName;//nombre del host
	private ClientWindow window;//ventana para cada cliente

	MessageSender(DatagramSocket sock, String host, ClientWindow win) {
		this.socket = sock;//conexion datagramas
		this.hostName = host;//nombre del host
		this.window = win;//ventana establecida donde seran consumidos los datos introducidos por parte del cliente
	}

	private void sendMessage(String s) throws Exception {
		byte buffer[] = s.getBytes();//convierte la cadena en bytes para el array de bytes
		InetAddress address = InetAddress.getByName(hostName);//obtiene la direccion ip
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);//convierte todo el mensaje en un datagrama
		socket.send(packet);//envia el datagrama
	}

	public void run() {
		boolean connected = false;
		do {
			try {
				sendMessage("Nuevo cliente conectado - BIENVENIDO!!!");
				connected = true;//cambia el estado a conectado
			} catch (Exception e) {
				window.displayMessage(e.getMessage());
			}
		} while (!connected);
		while (true) {
			try {
				while (!window.message_is_ready) {
					Thread.sleep(100);
				}
				sendMessage(window.getMessage());//se envia el mensaje proveido de la ventana del cliente
				window.setMessageReady(false);//se cambia el estado a nolisto
			} catch (Exception e) {
				window.displayMessage(e.getMessage());
			}
		}
	}
}

class MessageReceiver implements Runnable {
	DatagramSocket socket;//conexion datagrama
	byte buffer[];//array de bytes que contendra el mensaje por medio de bytes
	ClientWindow window;//ventana del cliente

	MessageReceiver(DatagramSocket sock, ClientWindow win) {
		this.socket = sock;//se establece conexion desde recibido el datagrama
		this.buffer = new byte[1024];//se genera un nuevo array de bytes para recibir un mensaje
		this.window = win;
	}

	public void run() {
		while (true) {
			try {
				DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);//se crea un nuevo paquete datagrama con el array de bytes que va a recibir y la longitud
				this.socket.receive(packet);//recibimos el paquete
				String received = new String(packet.getData(), 1, packet.getLength() - 1).trim();//se convierte en cadena el paquete recibido hay que averiguar que contiene el primer byte
				System.out.println(received);//se imprime en pantalla lo que se recibio
				this.window.displayMessage(received);//se muestra en la ventana abierta lo que se recibio
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
}

public class ChatClient {//aqui creamos el chat del cliente

	public static void main(String args[]) throws Exception {
		
		ClientWindow window = new ClientWindow();//se crea una ventana para interactuar
		String host = window.getHostName();//se obtiene el nombre del host o direccion ip del servidor
		window.setTitle("UDP CHAT  Server: " + host);//se menciona el nomre del servidor o direccion ip
		DatagramSocket socket = new DatagramSocket();//
		MessageReceiver receiver = new MessageReceiver(socket, window);
		MessageSender sender = new MessageSender(socket, host, window);
		Thread receiverThread = new Thread(receiver);
		Thread senderThread = new Thread(sender);
		receiverThread.start();
		senderThread.start();
	}
}