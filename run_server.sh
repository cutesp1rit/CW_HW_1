#!/bin/bash

# Скрипт для запуска сервера
# Использование: ./run_server.sh <port>

if [ $# -eq 0 ]; then
    echo "Использование: ./run_server.sh <port>"
    echo "Пример: ./run_server.sh 8080"
    exit 1
fi

PORT=$1

echo "Запуск сервера на порту $PORT..."
java -Xms100M -Xmx200M -jar target/server.jar $PORT