# ecommerce-spring

Hệ thống e-commerce xây dựng bằng Spring Boot, với mô hình microservice, 

## Modules

| Module | Mô tả |
| --- | --- |
| `auth-service` | Service xác thực & phân quyền người dùng (JWT, gRPC) |
| `common` | Module dùng chung (DTO, API response, error code, security, tiện ích) |
| `api-gateway` | Đầu vào tiếp nhận tất cả request từ client, định tuyến tới các service, dùng WebFlux |
| `eureka-server` | Service Discovery (Netflix Eureka) — đăng ký & tra cứu các service |
| `catalog-service` | Service quản lý danh mục sản phẩm (product, category, attribute, variant), dùng gRPC + Kafka + JPA |

## Yêu cầu

- Java 25
- Maven 3.9+ (dùng kèm `mvnw`)

## Build & Run

```bash
# Build toàn bộ
./mvnw clean install

# Chạy auth-service
cd auth-service
./mvnw spring-boot:run
```
