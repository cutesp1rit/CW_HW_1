#!/bin/bash

# Скрипт для запуска клиента
# Использование: ./run_client.sh <IP> <port> <N> <M> <Q>

if [ $# -ne 5 ]; then
    echo "Использование: ./run_client.sh <IP> <port> <N> <M> <Q>"
    echo ""
    echo "Примеры сценариев:"
    echo "  Сценарий 1: ./run_client.sh 192.168.1.100 8080 8 5000 25"
    echo "  Сценарий 2: ./run_client.sh 192.168.1.100 8080 1024 5000 10"
    echo "  Сценарий 3: ./run_client.sh 192.168.1.100 8080 64 3000 15"
    exit 1
fi

IP=$1
PORT=$2
N=$3
M=$4
Q=$5

echo "Запуск клиента с параметрами:"
echo "  Сервер: $IP:$PORT"
echo "  N=$N, M=$M, Q=$Q"
echo ""

java -jar target/client.jar $IP $PORT $N $M $Q