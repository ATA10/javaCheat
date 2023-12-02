import java.io.*;
import java.net.*;

public class Client1 {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader kullaniciGirisi = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter severOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            System.out.println("Lütfen mesajınızı girin (ex ile çıkış yapabilirsiniz)\n"
                    + "'list' ile aktif kullanıcıları görün\n"
                    + "'port' komutu girdikten sonra mesaj atmak istediğiniz poru girin:");
            String seciliPort = "";

            Thread cevapThread = new Thread(() -> {
                try {
                    while (true) {
                        String response = serverResponse.readLine();
                        if (response != null) {
                            System.out.println(">> " + response);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            cevapThread.start();            
            
            while (true) {
                String mesaj = kullaniciGirisi.readLine();
                if (mesaj.equals("ex")) {
                    break;
                } else if (mesaj.equals("list")) {
                    severOut.println(mesaj);
                } else if (mesaj.equals("port")) {
                    System.out.println("Lütfen hedef port numarasını girin:");
                    seciliPort = kullaniciGirisi.readLine();
                } else {
                    severOut.println(seciliPort + " " + mesaj);
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
