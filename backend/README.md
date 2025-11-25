# Encomienda Backend (Spring Boot 2.7.18, Java 8)

## 1. Objetivo
Backend minimal para gestionar envíos (Shipments) y notificaciones (Notifications) con CRUD completo, base de datos en memoria H2 y endpoints consumibles desde Android o Postman.

## 2. Requisitos
- Java 8 (1.8.x)
- Maven 3.8+ (probado con 3.9.11)

## 3. Cómo ejecutar
```
cd backend
mvn spring-boot:run
```
Servidor: http://localhost:8080

## 4. Base de datos H2
- Consola: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:encomienda`
- Usuario: `sa` (sin password por defecto)

## 5. Entidades
### Shipment
Campos: id, address, status, type, latitude, longitude, userEmail, courierEmail, createdAt, updatedAt
### Notification
Campos: id, userEmail, title, body, readFlag, createdAt

## 6. Endpoints
### Shipments (`/api/shipments`)
| Método | Path | Descripción |
|--------|------|-------------|
| GET | /api/shipments | Lista todos (opcional `?courierEmail=`) |
| GET | /api/shipments/{id} | Obtener por id |
| POST | /api/shipments | Crear nuevo |
| PUT | /api/shipments/{id} | Actualización completa |
| PATCH | /api/shipments/{id}/status | Cambiar solo estado (`{"status":"DELIVERED"}`) |
| DELETE | /api/shipments/{id} | Eliminar |
| GET | /api/shipments/user/{userEmail} | Envíos por usuario |
| GET | /api/shipments/status/{status} | Envíos por estado |

### Notifications (`/api/notifications`)
| Método | Path | Descripción |
|--------|------|-------------|
| GET | /api/notifications | Lista todas (opcional `?userEmail=`) |
| GET | /api/notifications/{id} | Obtener por id |
| POST | /api/notifications | Crear nueva |
| GET | /api/notifications/unread-count?userEmail= | Cantidad no leídas |
| GET | /api/notifications/unread?userEmail= | Listar no leídas |
| PATCH | /api/notifications/{id}/read | Marcar una como leída |
| PATCH | /api/notifications/mark-all-read?userEmail= | Marcar todas como leídas |
| DELETE | /api/notifications/{id} | Eliminar |

## 7. Ejemplos cURL
Crear shipment:
```
curl -X POST http://localhost:8080/api/shipments \
  -H "Content-Type: application/json" \
  -d '{"address":"Calle 123","status":"PENDING","type":"BOX","latitude":4.65,"longitude":-74.05,"userEmail":"user@mail.com","courierEmail":"courier@mail.com"}'
```
Actualizar estado:
```
curl -X PATCH http://localhost:8080/api/shipments/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED"}'
```
Crear notificación:
```
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"userEmail":"user@mail.com","title":"Envio entregado","body":"Tu envío #1 fue entregado"}'
```
Marcar leída:
```
curl -X PATCH http://localhost:8080/api/notifications/1/read
```

## 8. Validaciones
Se usan anotaciones de Bean Validation (@NotBlank, @Email). Los errores se devuelven automáticamente con 400 Bad Request si falla validación.

## 9. Estructura principal
```
backend/
  pom.xml
  src/main/java/com/encomienda/api/
    EncomiendaApplication.java
    entity/ (Shipment, Notification)
    repository/ (ShipmentRepository, NotificationRepository)
    controller/ (ShipmentController, NotificationController)
```

## 10. Cómo extender
- Agregar seguridad: Spring Security + JWT.
- Persistencia real: Cambiar H2 por PostgreSQL/MySQL (modificar propiedades).
- Documentar API: Agregar Springdoc OpenAPI.

## 11. Notas
- La BD se reinicia en cada arranque (memoria).
- Para datos persistentes, usar `jdbc:h2:file:./data/encomienda` y desactivar `DB_CLOSE_ON_EXIT`.
- CORS: se añadirá configuración para permitir acceso desde Android.

## 12. Licencia
Uso interno/demo.
