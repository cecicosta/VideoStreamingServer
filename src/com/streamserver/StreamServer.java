package com.streamserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.stream.encoder.movie.JpegImagesToMovie;
import com.streamreceiver.window.StreamReceiverWindow;

public class StreamServer {
	Thread t;
	Socket client;
	
	private int width;
	private int height;
	public StreamServer() throws IOException {
		//System.setProperty("java.awt.headless", "true");
		socket = new ServerSocket(10400);
		System.out.println("Waiting for connection...");
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
				int bytesRead = 0;
				try{
					//Read resolution
					bytesRead = reader.read(body);
				}catch(Exception e){ System.out.println("Failed to get resolution" + e.getMessage()); return; }
				String[] resolution = new String(body, 0, bytesRead).split(",");
				
				width = resolution.length > 0 ? Integer.parseInt(resolution[0]) : 0;
				height = resolution.length > 1 ? Integer.parseInt(resolution[1]) : 0;
				
				System.out.println("Resolution: " + width + ", " + height);
				
				while(true){

					bytesRead = 0;
					try{
						bytesRead = reader.read(body);
					}catch(Exception e){ System.out.println(e.getMessage()); break; }
					
					int totalBytesToRead = Integer.parseInt(new String(body,0 , bytesRead));
					
					String msg = "REQUEST_FILE: " + totalBytesToRead;
					try{
						writer.write(msg.getBytes());
					}catch(Exception e){ System.out.println(e.getMessage()); break; }

					body = new byte[totalBytesToRead];
					
					//Read file data
					bytesRead = 0;
					int totalBytesRead = 0;
					try{
						while(totalBytesRead < totalBytesToRead && bytesRead > -1) {
							bytesRead = reader.read(body, totalBytesRead, totalBytesToRead - totalBytesRead);
							totalBytesRead += bytesRead < -1? 0 : bytesRead;
						}
					}catch(Exception e){ System.out.println(e.getMessage()); break; }
					
					if(totalBytesRead != totalBytesToRead) {							
						msg = "BYTES_MISMATCH";
						try{ 
							writer.write(msg.getBytes());
						}catch(Exception e){System.out.println(e.getMessage()); break; }
						System.out.println("read bytes mismatch: " + totalBytesRead + " read, expected: " + totalBytesToRead);
						continue;
					}
					
					if(firstFrame) {
						receiver.initiate(body, width, height);
						receiver.setOnCloseCallback((Void) -> StreamServer.this.onWindowClosed());
						firstFrame = false;
					}else {
						receiver.updateFrame(body);
					}
					
					msg = "ALL_BYTES_RECEIVED";
					try{
						writer.write(msg.getBytes());
					}catch(Exception e){ System.out.println(e.getMessage()); break; }
				}
				try {
					client.close();
				} catch (IOException e) {
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
			JpegImagesToMovie.CreateVideoFile(width, height, StreamReceiverWindow.GetInstance().destinationDirectory,
					new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()).toString());
			JpegImagesToMovie.deleteSourceFiles();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private byte[] body = new byte[2048];
	private ServerSocket socket;
	
}


