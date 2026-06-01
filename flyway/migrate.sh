#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-imdb}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"

if [[ ! "${MYSQL_DATABASE}" =~ ^[a-zA-Z0-9_]+$ ]]; then
  echo "MYSQL_DATABASE must contain only letters, numbers, and underscores." >&2
  exit 1
fi

mysql_args=(-u "${MYSQL_USER}" -h "${MYSQL_HOST}" -P "${MYSQL_PORT}")
flyway_args=(
  "-url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true"
  "-user=${MYSQL_USER}"
)

if [[ -n "${MYSQL_PASSWORD}" ]]; then
  mysql_args+=("-p${MYSQL_PASSWORD}")
  flyway_args+=("-password=${MYSQL_PASSWORD}")
fi

# 创建数据库
mysql "${mysql_args[@]}" \
  -e "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行migrate
"${SCRIPT_DIR}/flyway" "${flyway_args[@]}" migrate
