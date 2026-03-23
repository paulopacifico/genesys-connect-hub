.PHONY: build test up down logs migrate

build:
	mvn clean package -DskipTests

test:
	mvn test

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	docker-compose logs -f app

migrate:
	docker-compose run --rm app java -jar app.jar --spring.flyway.enabled=true --spring.jpa.hibernate.ddl-auto=none
