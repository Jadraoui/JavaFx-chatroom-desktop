package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import javafx.scene.text.Font;


public class Main extends Application {
	
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	ServerSocket serverSocket;
	
	public void startServer(String IP,int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP,port));
			
		}catch(Exception e) {
			System.out.println("cc");
			if(!serverSocket.isClosed())
				stopServer();
			return;
		}
		
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("......."+ socket.getRemoteSocketAddress()+ ": " +Thread.currentThread().getName());
					}catch(Exception e) {
						if(!serverSocket.isClosed())
							stopServer();
						break;
					}
				}
			}
			
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
			if(threadPool != null && !threadPool.isShutdown())
				threadPool.shutdown();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("red",15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("start");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton,new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9098;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("start")) {
				startServer(IP, port);
				Platform.runLater(()->{
					String message = String.format("[start]\n", IP,port);
					textArea.appendText(message);
					textArea.setText("started");
				});
			}else {
				stopServer();
				Platform.runLater(()-> {
					String message = String.format("[serverStoped]\n", IP,port);
					textArea.appendText(message);
					textArea.setText("Stoped");
			});
			}
		});
		
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("chat chat");
		primaryStage.setOnCloseRequest(event->stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
