echo "Cinema Booking System Test"
echo "========================="

# Controlla se i file Docker esistono
if [ ! -f "docker-compose.yml" ]; then
    echo "Creating docker-compose.yml..."
    cat > docker-compose.yml << 'EOF'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_REDIS_HOST=redis
    depends_on:
      - redis
EOF
fi

if [ ! -f "Dockerfile" ]; then
    echo "Creating Dockerfile..."
    cat > Dockerfile << 'EOF'
FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

EXPOSE 8081

CMD ["java", "-jar", "target/movie-service-0.0.1-SNAPSHOT.jar"]
EOF
fi

echo "Docker files ready"

# Build e avvia
echo "Starting services..."
docker-compose up --build -d

# Controlla Redis prima
echo "Checking Redis..."
sleep 10
docker-compose exec redis redis-cli ping 2>/dev/null || echo "Redis not responding yet"

# Attendi
echo "Waiting for startup..."
sleep 15

# Verifica con timeout e debug
TIMEOUT=120
COUNTER=0

echo "Checking application status..."
while ! curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; do
    echo "Still starting... ($COUNTER/$TIMEOUT seconds)"

    if [ $COUNTER -gt $TIMEOUT ]; then
        echo ""
        echo "Application failed to start. Checking logs..."
        echo "=============================================="
        docker-compose logs app --tail 30
        echo ""
        echo "Container status:"
        docker-compose ps
        echo ""
        echo "Cleanup with: docker-compose down"
        exit 1
    fi

    sleep 5
    COUNTER=$((COUNTER + 5))
done

echo "System ready"

# Test API
echo ""
echo "Testing APIs"
echo "============"

echo "Health check:"
curl -s http://localhost:8081/actuator/health

echo ""
echo "Bookings health:"
curl -s http://localhost:8081/api/bookings/health

echo ""
echo "Movies:"
curl -s http://localhost:8081/api/movies

echo ""
echo "Validation test:"
curl -s -X POST http://localhost:8081/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"screeningId": null, "userEmail": "invalid", "numberOfSeats": 0}'

echo ""
echo "Test completed"
echo ""
echo "Application: http://localhost:8081"
echo "Stop with: docker-compose down"