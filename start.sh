#!/bin/bash
# start.sh — Arranca el sistema completo
set -e

if [ ! -f .env ]; then
  echo "Copiando .env.example a .env (editá si necesitás cambios)..."
  cp .env.example .env
fi

docker compose up --build
