# ecommerce-spring

Hệ thống e-commerce xây dựng bằng Spring Boot, tổ chức theo dạng multi-module.

## Modules

| Module | Mô tả |
| --- | --- |
| `auth-service` | Service xác thực & phân quyền người dùng |
| `common` | Module dùng chung (DTO, API response, tiện ích) |

## Yêu cầu

- Java 17+
- Maven 3.9+ (dùng kèm `mvnw`)

## Build & Run

```bash
# Build toàn bộ
./mvnw clean install

# Chạy auth-service
cd auth-service
./mvnw spring-boot:run
```
