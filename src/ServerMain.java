/** Copyright (C) 2019 Joni Rousu
* 	This work is free. It comes without any warranty, to the extent permitted 
*	by applicable law. You can redistribute it and/or modify it under the
* 	terms of the Do What The Fuck You Want To Public License, Version 2,
* 	as published by Sam Hocevar. 
*	See the LICENCE file or http://www.wtfpl.net/ for more details.
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ServerMain {
	/**
	 * @param port
	 * @throws IOException 
	 * @author Joni Rousu
	 */
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.out.println("No Port specified!\n\rUse Server.jar port");
			System.exit(1);
		}
		if(Integer.parseInt(args[0]) < 1024) {
			System.out.println("You are not allowed to use well known ports (0-1023)!");
			System.exit(1);
		}
		if(Integer.parseInt(args[0]) > 65535) {
			System.out.println("Your port is out of range!\n\rThe maximum port number is 65535!");
			System.exit(1);
		}

		int port = Integer.parseInt(args[0]);
		ServerSocket server = new ServerSocket(port);
		OutputStream serverOutput;
		ConnectionHandler handler = new ConnectionHandler(server, port);
		handler.start();

		System.out.println("Your anonymous chat server is running at port: " + port);

		while(true){
			for (int i = 0; i < ConnectionHandler.MAXCLIENTS; i++) {
				if (handler.clients[i] != null && handler.clients[i].getInputStream().available() > 0) {
					InputStream clientInput = handler.clients[i].getInputStream();
					byte[] buffer = new byte[1024];
					int message, index = 0;

					do {
						message = clientInput.read();
						buffer[index++] = (byte)message;
					} while (message != 10);

					boolean toBeClosed = false;

					if (buffer[0] == 27){
						toBeClosed = true;
					}

					for (int j = 0; j< ConnectionHandler.MAXCLIENTS; j++) {
						if (!toBeClosed) {
							if (handler.clients[j] == null) {
								continue;
							}
							serverOutput = handler.clients[j].getOutputStream();
							serverOutput.write(("\n\r" + (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()))).getBytes());
							if (handler.getClientNr(i) == handler.getClientNr(j)) {
								serverOutput.write(" You >> ".getBytes());
							} else {
								serverOutput.write((" Stranger " + handler.getClientNr(i) + " >> ").getBytes());
							}
							serverOutput.write(buffer, 0, index);
							serverOutput.write(">> ".getBytes());
							serverOutput.flush();
						}
					}
					if (toBeClosed) handler.close(i);
				}
			}
		}
	}
}
