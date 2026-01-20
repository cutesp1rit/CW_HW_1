package ru.hse.network;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Клиентская часть приложения для измерения пропускной способности сети.
 * Отправляет данные серверу и замеряет время отклика.
 */
public class Client {
    
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Использование: java -jar client.jar <IP> <port> <N> <M> <Q>");
            System.out.println("Где:");
            System.out.println("  IP   - IP адрес сервера");
            System.out.println("  port - порт сервера");
            System.out.println("  N    - шаг увеличения размера массива");
            System.out.println("  M    - количество итераций");
            System.out.println("  Q    - количество под-итераций для усреднения");
            System.out.println("\nПример: java -jar client.jar 192.168.1.100 8080 8 5000 25");
            return;
        }
        
        String serverIP = args[0];
        int port;
        int N, M, Q;
        
        try {
            port = Integer.parseInt(args[1]);
            N = Integer.parseInt(args[2]);
            M = Integer.parseInt(args[3]);
            Q = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: параметры port, N, M, Q должны быть числами");
            return;
        }
        
        System.out.println("=== Параметры эксперимента ===");
        System.out.println("Сервер: " + serverIP + ":" + port);
        System.out.println("N (шаг): " + N + " байт");
        System.out.println("M (итераций): " + M);
        System.out.println("Q (под-итераций): " + Q);
        System.out.println("==============================\n");
        
        // Массив для хранения результатов
        long[][] results = new long[M][2]; // [размер, среднее время]
        
        try (Socket socket = new Socket(serverIP, port)) {
            // Устанавливаем TCP_NODELAY
            socket.setTcpNoDelay(true);
            
            System.out.println("Подключено к серверу: " + socket.getInetAddress().getHostAddress());
            System.out.println("Начинаем измерения...\n");
            
            OutputStream output = socket.getOutputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Random random = new Random();
            
            // Основной цикл по итерациям
            for (int k = 0; k < M; k++) {
                int arraySize = N * k + 8;
                long totalTime = 0;
                
                // Под-итерации для усреднения
                for (int q = 0; q < Q; q++) {
                    // Генерируем случайный массив байт
                    byte[] data = new byte[arraySize];
                    random.nextBytes(data);
                    
                    // Замеряем время отправки и получения ответа
                    long startTime = System.currentTimeMillis();
                    
                    // Отправляем данные
                    output.write(data);
                    output.flush();
                    
                    // Ждем ответ от сервера
                    String response = input.readLine();
                    
                    long endTime = System.currentTimeMillis();
                    
                    // Накапливаем время
                    totalTime += (endTime - startTime);
                }
                
                // Вычисляем среднее время
                long averageTime = totalTime / Q;
                
                // Сохраняем результат
                results[k][0] = arraySize;
                results[k][1] = averageTime;
                
                // Выводим прогресс каждые 10% итераций
                if ((k + 1) % (M / 10) == 0 || k == 0) {
                    System.out.printf("Прогресс: %d/%d (%.1f%%) - Размер: %d байт, Среднее время: %d мс\n",
                            k + 1, M, ((k + 1) * 100.0 / M), arraySize, averageTime);
                }
            }
            
            System.out.println("\nИзмерения завершены!");
            
        } catch (IOException e) {
            System.out.println("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Сохраняем результаты в CSV файл
        saveResultsToCSV(results, N, M, Q);
        
        // Выводим результаты в консоль
        printResults(results);
    }
    
    /**
     * Сохранение результатов в CSV файл
     */
    private static void saveResultsToCSV(long[][] results, int N, int M, int Q) {
        String filename = String.format("results_N%d_M%d_Q%d.csv", N, M, Q);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Размер (байт),Среднее время (мс)");
            
            for (long[] result : results) {
                writer.printf("%d,%d\n", result[0], result[1]);
            }
            
            System.out.println("\nРезультаты сохранены в файл: " + filename);
            
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении результатов: " + e.getMessage());
        }
    }
    
    /**
     * Вывод результатов в консоль (первые и последние 10 записей)
     */
    private static void printResults(long[][] results) {
        System.out.println("\n=== Результаты измерений ===");
        System.out.println("Размер (байт) | Среднее время (мс)");
        System.out.println("--------------------------------");
        
        // Первые 10 записей
        int displayCount = Math.min(10, results.length);
        for (int i = 0; i < displayCount; i++) {
            System.out.printf("%13d | %18d\n", results[i][0], results[i][1]);
        }
        
        if (results.length > 20) {
            System.out.println("        ...   |        ...");
            
            // Последние 10 записей
            for (int i = results.length - 10; i < results.length; i++) {
                System.out.printf("%13d | %18d\n", results[i][0], results[i][1]);
            }
        }
        
        System.out.println("================================");
    }
}