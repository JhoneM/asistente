.PHONY: up down logs clean reset seed status build up-detached

up:           ## Levanta el sistema (construye imágenes si es necesario)
	docker compose up --build

up-detached:  ## Levanta todo en segundo plano
	docker compose up --build -d

down:         ## Detiene los contenedores (conserva los datos)
	docker compose down

clean:        ## Detiene contenedores Y borra el volumen de Postgres
	docker compose down -v

reset: clean up   ## Limpia todo y vuelve a levantar de cero

logs:         ## Muestra logs en vivo de los 3 servicios
	docker compose logs -f

status:       ## Estado de los contenedores
	docker compose ps

seed:         ## Fuerza re-poblado (borra DB y la rearma con seed data)
	docker compose down -v
	APP_SEED_ENABLED=true docker compose up --build

build:        ## Solo construye imágenes sin arrancar
	docker compose build
