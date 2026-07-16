# Country Routing Service

Spring Boot REST service that computes a land route between two countries using `cca3` country codes and border data from:

`https://raw.githubusercontent.com/mledoze/countries/master/countries.json`

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Build and Test

```bash
mvn test
```

## Run

```bash
mvn spring-boot:run
```

The service starts on port `8080` by default.

## API

```http
GET /routing/{origin}/{destination}
```

Example:

```bash
curl http://localhost:8080/routing/CZE/ITA
```

Response:

```json
{"route":["CZE","AUT","ITA"]}
```

The service returns HTTP `400` when either country code is invalid or no land route exists.

## Notes

- Country data is loaded lazily on first use and cached for the lifetime of the application.
- The route is computed with breadth-first search over the land border graph.
- `countries.source-url` can be overridden if a different compatible `countries.json` source is needed.
