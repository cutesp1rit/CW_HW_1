package ru.hse.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Server {
    
    public static void main(String[] args) {
        
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: порт должен быть числом");
            return;
        }
        
        System.out.println("Запуск сервера на порту " + port + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен и ожидает подключений");
            System.out.println("IP адрес сервера: " + java.net.InetAddress.getLocalHost().getHostAddress());
            
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("\nНовое подключение от: " + clientSocket.getInetAddress().getHostAddress());
                    
                    new Thread(() -> handleClient(clientSocket)).start();
                    
                } catch (IOException e) {
                    System.out.println("Ошибка при подключении клиента: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.out.println("Не удалось запустить сервер: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private static void handleClient(Socket clientSocket) {
        try (
            DataInputStream dataInput = new DataInputStream(clientSocket.getInputStream());
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            int requestCount = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            
            while (true) {
                try {
                    // Читаем размер массива (4 байта)
                    int arraySize = dataInput.readInt();
                    
                    // Читаем ТОЧНО arraySize байт
                    byte[] data = new byte[arraySize];
                    dataInput.readFully(data);
                    
                    // Отправляем timestamp после получения всего массива
                    String timestamp = dateFormat.format(new Date());
                    writer.println(timestamp);
                    writer.flush();
                    
                    requestCount++;
                    
                    // Выводим прогресс каждые 100 запросов
                    if (requestCount % 100 == 0) {
                        System.out.printf("Обработано запросов: %d (последний размер: %d байт)\n", 
                            requestCount, arraySize);
                    }
                    
                } catch (EOFException e) {
                    // Клиент закрыл соединение
                    break;
                }
            }
            
            System.out.println("Клиент отключился. Всего обработано запросов: " + requestCount);
            
        } catch (IOException e) {
            System.out.println("Ошибка при обработке клиента: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Игнорируем ошибки при закрытии
            }
        }
    }
}