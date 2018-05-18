package com.streaming.app;

import java.io.IOException;

import com.streamserver.StreamServer;

public class ServerApp {

	public static void main(String[] args) throws IOException {
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					new StreamServer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}
