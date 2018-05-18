package com.streamserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.stream.encoder.movie.JpegImagesToMovie;
import com.streamreceiver.window.StreamReceiverWindow;

public class StreamServer {
	Thread t;
	Socket client;
	public StreamServer() throws IOException {
		//System.setProperty("java.awt.headless", "true");
		socket = new ServerSocket(10400);
		client = socket.accept();
		System.out.println("Connection from client accepted");
		StreamReceiverWindow receiver = StreamReceiverWindow.GetInstance();
		
		t = new Thread(new Runnable(){
			@Override
			public void run() {
				InputStream reader = null;
				OutputStream writer = null;
				try {
					reader = client.getInputStream();
					writer = client.getOutputStream();
					System.out.println("SocketConnected: server");
				} catch (IOException e) {
					e.printStackTrace();
				}
				boolean firstFrame = true;
				while(true){
					try {	
						
						int bytesRead = 0;
						bytesRead = reader.read(body);
						int totalBytesToRead = Integer.parseInt(new String(body,0 , bytesRead));
						//System.out.println("Bytes to read: " + totalBytesToRead);
						
						String msg = "REQUEST_FILE: " + totalBytesToRead;
						writer.write(msg.getBytes());
						
						body = new byte[totalBytesToRead];
						//Read file data
						bytesRead = 0;
						int totalBytesRead = 0;
						while(totalBytesRead < totalBytesToRead && bytesRead > -1) {
							bytesRead = reader.read(body, totalBytesRead, totalBytesToRead - totalBytesRead);
							totalBytesRead += bytesRead < -1? 0 : bytesRead;
							//System.out.println("Read: " + totalBytesRead + " of " + totalBytesToRead);
						}
						
						if(totalBytesRead != totalBytesToRead) {							
							//System.out.println("read bytes mismatch: " + totalBytesRead + " read, expected: " + totalBytesToRead);
							msg = "BYTES_MISMATCH";
							writer.write(msg.getBytes());
							continue;
						}
						
						if(firstFrame) {
							receiver.Initiate(body);
							receiver.setOnCloseCallback((Void) -> StreamServer.this.onWindowClosed());
							firstFrame = false;
						}else {
							receiver.SetNewFrame(body);
						}
						
						msg = "ALL_BYTES_RECEIVED";
						writer.write(msg.getBytes());
						
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	public Void onWindowClosed(){
		try {
			client.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println("Server has stopped. Starting to encode video file.");
			JpegImagesToMovie.CreateVideoFile(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private byte[] body = new byte[2048];
	private ServerSocket socket;
	
}


