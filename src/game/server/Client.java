package game.server;

import game.GameLogic;
import game.Main;

import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client
{
    private static AtomicInteger row_atomic = new AtomicInteger(-1),
            column_atomic = new AtomicInteger(-1);
    public static boolean client_turn = true;
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final InetAddress host_address;
    private final int host_port;
    private final byte[] buffer;

    public Client(String host, int port) throws IOException{
        socket = new DatagramSocket();
        host_address = InetAddress.getByName(host);
        host_port = port;
        buffer = new byte[1024];
        packet = new DatagramPacket(buffer, buffer.length);
        final Client.Task[] task = {Task.SENDING};


        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable task_loop = () -> {
            int row = row_atomic.get();
            int column = column_atomic.get();

            switch (task[0]) {
                case Client.Task.RECEIVING -> {
                    try {task[0] = receiveData();}
                    catch (IOException e) {e.printStackTrace();}
                    client_turn = true;
                }
                case Client.Task.SENDING -> {
                    if (row < 0 || column < 0) {break;}
                    try {task[0] = sendData(("move: " + row + ", "+ column));}
                    catch (IOException e) {e.printStackTrace();}
                }
                case Client.Task.ERROR -> {System.out.println("some error occurred");}
                case Client.Task.EXIT -> {executorService.shutdown();}
            }

            row_atomic = new AtomicInteger(-1);
            column_atomic = new AtomicInteger(-1);
        };
        executorService.scheduleAtFixedRate(task_loop, 0, 100, TimeUnit.MILLISECONDS);
    }

    enum Task {
        RECEIVING,
        SENDING,
        EXIT,
        ERROR
    }


    public static void setRow(int value) {row_atomic = new AtomicInteger(value);}
    public static void setColumn(int value) {column_atomic = new AtomicInteger(value);}

    //handles sending data && returns next task
    private Task sendData(String data) throws IOException {
        socket.send(new DatagramPacket(data.getBytes(), data.length(),
                host_address, host_port));

        DatagramPacket response = new DatagramPacket(buffer, buffer.length);

        socket.receive(response);
        String command = new String(response.getData(), 0, response.getLength());

        if (command.equals("wait for command")) {
            return Task.RECEIVING;
        }

        return Task.ERROR;
    }

    //handles receiving data && returns next task
    private Task receiveData() throws IOException {
        socket.receive(packet);

        String command = new String(packet.getData(), 0, packet.getLength());
        System.out.printf("host command: %s \n", command);

        if (command.startsWith("move:")) {
            String[] info = (command.split(":")[1].split(","));
            int[] position = {Integer.parseInt(info[0].strip()), Integer.parseInt(info[1].strip())};

            GameLogic.setGame_board(position[0], position[1], GameLogic.player1);
            Main.getGame().getGameUI().renderGameBoard(position[0], position[1]);

            String response = ("wait for command");
            socket.send(new DatagramPacket(response.getBytes(), response.length(),
                    host_address, host_port));

            return Task.SENDING;
        } else if (command.equals("exit")) {
            System.out.println("host stopped loop");
            return Task.EXIT;
        }

        return Task.ERROR;
    }

    public void sendExit() throws IOException {
        String command = "exit";
        socket.send(new DatagramPacket(command.getBytes(), command.length(),
                host_address, host_port));
    }


}