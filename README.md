# representative-service
Representative Service for VotingBooth

## Running locally

`docker-compose.yml` starts Postgres (host port 5433) and SeaweedFS as a local S3 store (host port 9000), with the
`representative-raw` bucket created and versioned.

```sh
docker compose up -d
```

The `local` profile of the job and the API points at them. Both need JDK 21:

```sh
./mvnw -B package -DskipTests
java -jar job/target/representative-job-0.0.1-SNAPSHOT.jar --spring.profiles.active=local            # sync (exits when done)
java -jar job/target/representative-job-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --force    # reprocess every record
java -jar api/target/representative-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=local            # API on :8085
```

Without JDK 21, run the images instead (`host.docker.internal` reaches the ports compose publishes):

```sh
docker build -f job/Dockerfile -t representative-job .
docker build -f api/Dockerfile -t representative-api .
docker run --rm -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/representatives \
  -e STORAGE_ENDPOINT=http://host.docker.internal:9000 representative-job
docker run --rm -p 8085:8085 -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/representatives representative-api
```

`docker compose down -v` removes the containers and their data.
