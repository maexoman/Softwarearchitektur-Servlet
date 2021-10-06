package de.hsw;

import de.hsw.server.Server;
import de.hsw.server.configs.SurfletMapper;

import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        Scanner s = new Scanner (System.in);
        boolean running = true;
        int port = 8080;

        if (args.length > 0) {
            String sPort = args [0];
            try {
                port = Integer.parseInt (sPort);
            } catch (RuntimeException e) {}
        }

        System.out.println("You can quit/redeploy at any time. Just type \"quit\" or \"redeploy\".");

        new Thread(new Server (port)).start();

        while (running) {
            String command = s.nextLine();
            String[] arguments = command.split(" ");
            switch (arguments[0].trim().toLowerCase()) {
                case "quit":
                    System.exit(0);
                    break;
                case "redeploy":
                    String path = "web.xml";
                    if (arguments.length > 1) {
                        path = arguments [1];
                    }
                    SurfletMapper.reload (path);
                    break;
                default: System.out.println("[ERROR]: unknown command.");
            }
        }




    }
}