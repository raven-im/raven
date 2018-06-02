#!/usr/bin/env bash

# 创建数据库
mysql -uroot -p123456 -h127.0.0.1 -P3306 -e "CREATE DATABASE IF NOT EXISTS imdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行migrate
bash flyway migrate