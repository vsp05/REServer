# Real Estate Microservices

This project implements a microservices architecture for a real estate sales system with three independently executable services.

## Architecture

### 1. API Gateway (Port 7070)
- **Purpose**: Accepts HTTP requests from users and routes them to appropriate microservices
- **Dependencies**: Property Server, Analytics Server
- **Features**: 
  - Routes all property-related requests to Property Server
  - Routes all analytics-related requests to Analytics Server
  - Automatically increments access counts when properties or postcodes are accessed

### 2. Property Server (Port 7071)
- **Purpose**: Manages all real estate property data
- **Database**: MongoDB (shared with Analytics Server)
- **Features**:
  - Create new property sales
  - Retrieve properties by ID
  - Search properties by postcode
  - Get average prices by date range
  - Find properties under a specific price
  - Get all properties

### 3. Analytics Server (Port 7072)
- **Purpose**: Manages access count analytics for properties and postcodes
- **Database**: MongoDB (shared with Property Server)
- **Features**:
  - Increment property access counts
  - Increment postcode access counts
  - Retrieve property access counts
  - Retrieve postcode access counts

## Database Design

Both Property Server and Analytics Server share the same MongoDB database (`homesale.sales` collection) but have different responsibilities:

- **Property Server**: Can read and write property data, but cannot modify access count fields
- **Analytics Server**: Can only read and write access count fields (`property_accessed_count`, `post_code_accessed_count`)

## API Endpoints

### API Gateway (http://localhost:7070)

#### Property Operations
- `GET /sales` - Get all properties
- `POST /sales` - Create new property
- `GET /sales/{propertyID}` - Get property by ID (auto-increments access count)
- `GET /sales/postcode/{postcode}` - Get properties by postcode (auto-increments access count)
- `GET /sales/average-price/dates/{startDate}/{endDate}` - Get average price by date range
- `GET /sales/under/{price}` - Get properties under specified price

#### Analytics Operations
- `GET /sales/{propertyID}/accessed-count` - Get property access count
- `GET /sales/postcode/{postcode}/accessed-count` - Get postcode access count

### Property Server (http://localhost:7071)

#### Direct Property Operations
- `GET /properties` - Get all properties
- `POST /properties` - Create new property
- `GET /properties/{propertyID}` - Get property by ID
- `GET /properties/postcode/{postcode}` - Get properties by postcode
- `GET /properties/average-price/dates/{startDate}/{endDate}` - Get average price by date range
- `GET /properties/under/{price}` - Get properties under specified price

### Analytics Server (http://localhost:7072)

#### Direct Analytics Operations
- `POST /analytics/property/{propertyID}/increment` - Increment property access count
- `POST /analytics/postcode/{postcode}/increment` - Increment postcode access count
- `GET /analytics/property/{propertyID}/count` - Get property access count
- `GET /analytics/postcode/{postcode}/count` - Get postcode access count

## Building and Running

### Prerequisites
- Java 21
- Maven 3.6+
- MongoDB Atlas connection (configured in DAO classes)

### Build All Services
```bash
cd microservices
mvn clean package
```

### Run Services

1. **Start Property Server**:
```bash
cd property-server
java -jar target/property-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

2. **Start Analytics Server**:
```bash
cd analytics-server
java -jar target/analytics-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

3. **Start API Gateway**:
```bash
cd api-gateway
java -jar target/api-gateway-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Testing

Once all services are running, you can test them:

1. **API Gateway**: http://localhost:7070
2. **Property Server**: http://localhost:7071
3. **Analytics Server**: http://localhost:7072

Each service provides Swagger documentation at `/docs/swagger` and ReDoc at `/docs/redoc`.

## Service Coordination

The API Gateway coordinates between services:

1. When a property is accessed via the gateway, it:
   - Fetches property data from Property Server
   - Increments access count via Analytics Server
   - Returns property data to client

2. When a postcode is accessed via the gateway, it:
   - Fetches properties from Property Server
   - Increments postcode access count via Analytics Server
   - Returns properties to client

This design ensures that analytics are automatically tracked while maintaining separation of concerns between property data and analytics data. 