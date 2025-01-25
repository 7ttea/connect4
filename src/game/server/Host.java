package game.server;

import game.GameLogic;
import game.Main;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Host
{

    private static AtomicInteger row_atomic = new AtomicInteger(-1),
                                 column_atomic = new AtomicInteger(-1);
    public static boolean host_turn = false;
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final int port = 6942;

    public Host() throws IOException {
        socket = new DatagramSocket(port);
        byte[] buffer = new byte[1024];
        packet = new DatagramPacket(buffer, buffer.length);
        final Task[] task = {Task.RECEIVING};


        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable task_loop = () -> {
            int row = row_atomic.get();
            int column = column_atomic.get();

            switch (task[0]) {
                case Task.RECEIVING -> {
                    try {task[0] = receiveData();}
                    catch (IOException e) {e.printStackTrace();}
                    host_turn = true;
                }
                case Task.SENDING -> {
                    if (row < 0 || column < 0) {break;}
                    try {task[0] = sendData(("move: " + row + ", "+ column));}
                    catch (IOException e) {e.printStackTrace();}
                }
                case Task.ERROR -> {
                    System.out.println("some error occurred");
                    socket.close();
                    executorService.shutdown();
                }
                case Task.EXIT -> {
                    executorService.shutdown();
                }

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


    public InetAddress getAddress() throws UnknownHostException {return InetAddress.getLocalHost();}
    public int getPort() {return port;}
    public static void setRow(int value) {row_atomic = new AtomicInteger(value);}
    public static void setColumn(int value) {column_atomic = new AtomicInteger(value);}

    //handles sending data && returns next task
    private Task sendData(String data) throws IOException {
        socket.send(new DatagramPacket(data.getBytes(), data.length(),
                                       packet.getAddress(), packet.getPort()));

        socket.receive(packet);
        String command = new String(packet.getData(), 0, packet.getLength());

        if (command.equals("wait for command")) {
            return Task.RECEIVING;
        }

        return Task.ERROR;
    }

    //handles receiving data && returns next task
    private Task receiveData() throws IOException {
        socket.receive(packet);

        String command = new String(packet.getData(), 0, packet.getLength());
        System.out.printf("client command:\n %s \n", command);

        if (command.startsWith("move:")) {
            String[] info = (command.split(":")[1]).split(",");
            int[] position = {Integer.parseInt(info[0].strip()), Integer.parseInt(info[1].strip())};

            GameLogic.setGame_board(position[0], position[1], GameLogic.player2);
            Main.getGame().getGameUI().renderGameBoard(position[0], position[1]);

            String response = ("wait for command");
            socket.send(new DatagramPacket(response.getBytes(), response.length(),
                    packet.getAddress(), packet.getPort()));

            return Task.SENDING;
        } else if (command.equals("exit")) {
            System.out.println("client stopped loop");
            return Task.EXIT;
        }

        return Task.ERROR;
    }

    public void sendExit() throws IOException {
        String command = "exit";
        socket.send(new DatagramPacket(command.getBytes(), command.length(),
                packet.getAddress(), packet.getPort()));
    }

    public static void main(String[] args) throws IOException {
        new Host();
    }
}
