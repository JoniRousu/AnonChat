/** Copyright (C) 2019 Joni Rousu
* 	This work is free. It comes without any warranty, to the extent permitted 
*	by applicable law. You can redistribute it and/or modify it under the
* 	terms of the Do What The Fuck You Want To Public License, Version 2,
* 	as published by Sam Hocevar. 
*	See the LICENCE file or http://www.wtfpl.net/ for more details.
*/

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ConnectionHandler extends Thread {
	/**
	 * @param ServerSocket, Port of running Server 
	 * @author Joni Rousu
	 */

	public static final int MAXCLIENTS = 10;
	public Socket[] clients = new Socket[MAXCLIENTS];
	public volatile int NrClients = 0;
	public OutputStream serverOutput;
	static int port;
	static ServerSocket server;
	private int ClientNr = 1;
	private int[] freeSocket = new int[MAXCLIENTS];
	private int[] clientNrs = new int[MAXCLIENTS];
	
	ConnectionHandler(ServerSocket s, int p) {
		port = p;
		server = s;
	}

	public void close(int index) {
		try {
			clients[index].close();
			clients[index] = null;
			freeSocket[index] = 1;
			NrClients = NrClients-1;
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int j = 0; j< MAXCLIENTS; j++) {
			if (clients[j] != null) {
				try {
					serverOutput = clients[j].getOutputStream();
					serverOutput.write(("Stranger " + getClientNr(index) + " has left the Chat!\n\r>>").getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void freeAll(){
		for(int i = 0; i< MAXCLIENTS; i++){
			freeSocket[i] = 1;
		}
	}
	
	private int nextFreeSocketIndex() {
		for (int i = 0; i< MAXCLIENTS; i++){
			if (freeSocket[i] == 1){
				return i;
			}
		}
		return -1;
	}
	
	public int getClientNr(int index) {
		return clientNrs[index];
	}

	public void run(){
		freeAll();
		while(true){
			try {
				int NewClient = nextFreeSocketIndex();
				if(NrClients < MAXCLIENTS){
					clients[NewClient] = server.accept();
					clientNrs[NewClient] = ClientNr;
					freeSocket[NewClient] = 0;
					ClientNr++;
				} else {
					Socket refuse = server.accept();
					refuse.getOutputStream().write("Sorry, Server is full!".getBytes());
					refuse.close();
					continue;
				}
				serverOutput = clients[NewClient].getOutputStream();
				serverOutput.write(("Hello Stranger " + getClientNr(NewClient) + "!\n\r").getBytes());
				serverOutput.write("To leave simply press the ESC button and send it\n\r>>".getBytes());
				for (int i = 0; i< MAXCLIENTS; i++){
					if (clients[i] != null){
						serverOutput = clients[i].getOutputStream();
						serverOutput.write(("Stranger " + getClientNr(NewClient) + " has joined the Chat!\n\r>>").getBytes());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
