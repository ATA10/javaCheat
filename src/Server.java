import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final Object lock = new Object(); // Object for synchronization

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Sunucu başlatıldı. İstemci bağlantıları bekleniyor...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni istemci bağlandı: " + clientSocket);
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                synchronized (lock) {
                    clients.add(clientHandler);
                }
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ClientHandler> getClients() {
        synchronized (lock) {
            return new ArrayList<>(clients);
        }
    }

    // Tüm istemcilere güncel port listesini gönderen metod
    public static void sendPortList() {
        for (ClientHandler client : getClients()) {
            try {
                PrintWriter clientOut = new PrintWriter(client.getClientSocket().getOutputStream(), true);
                clientOut.println("portlist " + getClientPorts());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Aktif istemci portlarını bir string olarak döndüren metod
    public static String getClientPorts() {
        synchronized (lock) {
            StringBuilder portList = new StringBuilder();
            for (ClientHandler client : clients) {
                portList.append(client.getClientSocket().getPort()).append(" ");
            }
            return portList.toString().trim();
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final List<ClientHandler> clients;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
    
    public int getSenderPort() {
    return clientSocket.getPort();
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("Sunucuya bağlandınız.");

            while (true) {
                String mesaj = input.readLine();
                if (mesaj == null || mesaj.equals("ex")) {
                    break;
                } else if (mesaj.equals("list")) {
                    for (ClientHandler client : Server.getClients()) {
                        output.println(client.getClientSocket().getPort());
                    }
                } else {
                    int hedefPort = Integer.parseInt(mesaj.split(" ")[0]);
                    String mesajToSend = mesaj.substring(mesaj.indexOf(" ") + 1);
                    broadcastMesaj(hedefPort, mesajToSend);
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMesaj(int hedefPort, String mesaj) throws IOException {
        boolean validPort = false;

        while (!validPort) {
            for (ClientHandler client : Server.getClients()) {
                if (client.clientSocket.getPort() == hedefPort) {
                    int kimden = getSenderPort();
                    validPort = true;
                    PrintWriter clientOut = new PrintWriter(client.clientSocket.getOutputStream(), true);
                    clientOut.println(kimden +"->" + mesaj);
                    System.out.println(kimden + "->" + hedefPort + " : " + mesaj);
                    return;
                }
            }

            if (!validPort) {
                PrintWriter aa = new PrintWriter(clientSocket.getOutputStream(), true);
                aa.println("Geçersiz port numarası, lütfen yeniden girin:");
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                hedefPort = Integer.parseInt(input.readLine());
            }
        }
        System.out.println("Hedef port bulunamadı: " + hedefPort);
    }
}
