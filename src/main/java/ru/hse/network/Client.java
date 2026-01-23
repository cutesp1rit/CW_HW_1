package ru.hse.network;

import java.io.*;
import java.net.Socket;
import java.util.Random;


public class Client {
    
    public static void main(String[] args) {
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
        
        System.out.println("Сервер: " + serverIP + ":" + port);
        System.out.println("N (шаг): " + N + " байт");
        System.out.println("M (итераций): " + M);
        System.out.println("Q (под-итераций): " + Q);
        
        long[][] results = new long[M][2];
        
        try (Socket socket = new Socket(serverIP, port)) {
            // Устанавливаем TCP_NODELAY
            socket.setTcpNoDelay(true);
            
            System.out.println("Подключено к серверу: " + socket.getInetAddress().getHostAddress());
            System.out.println("Начинаем измерения...\n");
            
            OutputStream output = socket.getOutputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Random random = new Random();
            
            for (int k = 0; k < M; k++) {
                int arraySize = N * k + 8;
                long totalTime = 0;
                
                for (int q = 0; q < Q; q++) {
                    byte[] data = new byte[arraySize];
                    random.nextBytes(data);
                    
                    long startTime = System.currentTimeMillis();
                    
                    output.write(data);
                    output.flush();
                    
                    String response = input.readLine();
                    
                    long endTime = System.currentTimeMillis();
                    
                    totalTime += (endTime - startTime);
                }
                
                long averageTime = totalTime / Q;
                
                results[k][0] = arraySize;
                results[k][1] = averageTime;
            }
            
            System.out.println("\nИзмерения завершены!");
            
        } catch (IOException e) {
            System.out.println("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        saveResultsToCSV(results, N, M, Q);
    }
    
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
}