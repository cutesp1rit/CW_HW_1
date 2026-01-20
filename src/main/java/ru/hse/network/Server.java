package ru.hse.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Серверная часть приложения для измерения пропускной способности сети.
 * Принимает подключения от клиентов, получает данные и отправляет timestamp в ответ.
 */
public class Server {
    private static final int BUFFER_SIZE = 8192;
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Использование: java -jar server.jar <port>");
            System.out.println("Пример: java -jar server.jar 8080");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: порт должен быть числом");
            return;
        }
        
        System.out.println("Запуск сервера на порту " + port + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер успешно запущен и ожидает подключений");
            System.out.println("IP адрес сервера: " + java.net.InetAddress.getLocalHost().getHostAddress());
            
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("\nНовое подключение от: " + clientSocket.getInetAddress().getHostAddress());
                    
                    // Обрабатываем клиента в отдельном потоке
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
    
    /**
     * Обработка подключения клиента
     */
    private static void handleClient(Socket clientSocket) {
        try (
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output, true)
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int requestCount = 0;
            
            while (true) {
                // Читаем размер данных (первые 4 байта)
                int bytesRead = 0;
                int totalBytes = 0;
                
                // Читаем данные от клиента
                while ((bytesRead = input.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                    
                    // Формируем timestamp
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    String timestamp = dateFormat.format(new Date());
                    
                    // Отправляем ответ клиенту
                    writer.println(timestamp);
                    writer.flush();
                    
                    requestCount++;
                    
                    // Выводим статистику каждые 100 запросов
                    if (requestCount % 100 == 0) {
                        System.out.println("Обработано запросов: " + requestCount + 
                                         ", последний размер: " + totalBytes + " байт");
                    }
                    
                    // Сбрасываем счетчик для следующего запроса
                    totalBytes = 0;
                }
                
                break; // Клиент отключился
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