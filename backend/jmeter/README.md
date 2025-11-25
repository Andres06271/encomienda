# Pruebas JMeter - API Encomienda

Este directorio contiene las pruebas de rendimiento y funcionales para el backend de Encomienda usando Apache JMeter.

## 📋 Requisitos Previos

1. **Apache JMeter 5.x o superior**
   - Descargar desde: https://jmeter.apache.org/download_jmeter.cgi
   - Extraer en una ubicación de tu preferencia
   - Agregar `bin` de JMeter al PATH (opcional)

2. **Backend en ejecución**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   El servidor debe estar corriendo en `http://localhost:8080`

## 📁 Archivos del Proyecto

```
backend/jmeter/
├── Encomienda_API_Test_Plan.jmx    # Plan de pruebas principal
├── test_data.csv                    # Datos de prueba para shipments
├── notification_data.csv            # Datos de prueba para notificaciones
└── README.md                        # Este archivo
```

## 🚀 Cómo Ejecutar las Pruebas

### Opción 1: Modo GUI (Recomendado para desarrollo)

1. Abrir JMeter GUI:
   ```bash
   # Windows
   jmeter.bat
   
   # Linux/Mac
   jmeter.sh
   ```

2. Abrir el plan de pruebas:
   - File → Open → Seleccionar `Encomienda_API_Test_Plan.jmx`

3. Ejecutar las pruebas:
   - Click en el botón verde "Start" (▶️)
   - Ver resultados en los listeners disponibles

### Opción 2: Modo CLI (Recomendado para CI/CD)

```bash
# Ejecutar desde el directorio backend/jmeter/
jmeter -n -t Encomienda_API_Test_Plan.jmx -l results.jtl -e -o report/

# Opciones:
# -n : Modo no-GUI
# -t : Archivo del test plan
# -l : Archivo de resultados (JTL)
# -e : Generar reporte HTML
# -o : Directorio de salida del reporte
```

Ver reporte HTML generado en: `backend/jmeter/report/index.html`

## 📊 Plan de Pruebas

### Thread Groups Configurados

#### 1. **Shipments API Tests** (10 usuarios concurrentes, ramp-up 5s)
   - ✅ POST Create Shipment
   - ✅ GET All Shipments
   - ✅ GET Shipment by ID
   - ✅ GET Shipments by User Email
   - ✅ GET Shipments by Status
   - ✅ PATCH Update Shipment Status
   - ✅ PUT Update Shipment Complete
   - 🔒 DELETE Shipment (deshabilitado por defecto)

#### 2. **Notifications API Tests** (10 usuarios concurrentes, ramp-up 5s)
   - ✅ POST Create Notification
   - ✅ GET All Notifications
   - ✅ GET Notification by ID
   - ✅ GET Unread Count
   - ✅ GET Unread Notifications
   - ✅ PATCH Mark Notification as Read
   - ✅ PATCH Mark All as Read
   - 🔒 DELETE Notification (deshabilitado por defecto)

### Variables Configuradas

- `BASE_URL`: localhost (modificable)
- `PORT`: 8080 (modificable)

### Aserciones Implementadas

Cada request incluye verificación del código de respuesta HTTP esperado:
- 200 OK para operaciones de lectura y actualización
- 201 Created para operaciones de creación
- 204 No Content para operaciones de eliminación

### Listeners Disponibles

1. **View Results Tree**: Muestra cada request/response en detalle
2. **Summary Report**: Resumen estadístico (tiempos, throughput, errores)
3. **Graph Results**: Gráfico de tiempos de respuesta
4. **View Results in Table**: Tabla con todos los resultados

## 🔧 Personalización

### Cambiar número de usuarios concurrentes

En JMeter GUI:
1. Click en "Shipments API Tests" o "Notifications API Tests"
2. Modificar "Number of Threads (users)": 10 → tu valor
3. Modificar "Ramp-Up Period (seconds)": 5 → tu valor

### Cambiar servidor/puerto

En JMeter GUI:
1. Click en "Encomienda API Test Plan"
2. En "User Defined Variables":
   - Cambiar `BASE_URL` (ej: api.midominio.com)
   - Cambiar `PORT` (ej: 8443)

### Habilitar pruebas DELETE

Los DELETE están deshabilitados para no limpiar datos durante pruebas:
1. En JMeter GUI, encontrar "DELETE Shipment" o "DELETE Notification"
2. Click derecho → Enable

## 📈 Interpretación de Resultados

### Métricas Clave (Summary Report)

- **# Samples**: Total de requests ejecutados
- **Average**: Tiempo promedio de respuesta (ms)
- **Min/Max**: Tiempos mínimo y máximo (ms)
- **Std. Dev.**: Desviación estándar
- **Error %**: Porcentaje de errores
- **Throughput**: Requests por segundo
- **KB/sec**: Ancho de banda

### Valores Esperados (Referencia)

Para H2 en memoria con 10 usuarios:
- Average: < 100ms
- Error %: 0%
- Throughput: > 50 req/s

## 🐛 Troubleshooting

### Error: "Connection refused"
- ✅ Verificar que el backend esté corriendo
- ✅ Verificar puerto 8080 libre: `netstat -an | findstr 8080`

### Error 404 Not Found
- ✅ Verificar rutas en el backend coincidan con las del plan
- ✅ Verificar versión del backend actualizada

### Error 400 Bad Request
- ✅ Revisar formato JSON en los POST/PUT/PATCH
- ✅ Verificar validaciones en el backend

### Tiempos de respuesta muy altos
- ✅ Aumentar memoria de JMeter: editar `jmeter.bat` o `jmeter.sh`
  ```bash
  # Ejemplo: aumentar heap
  set HEAP=-Xms1g -Xmx1g
  ```
- ✅ Reducir número de threads concurrentes
- ✅ Verificar recursos del sistema (CPU, RAM)

## 📝 Buenas Prácticas

1. **Siempre ejecutar en modo CLI para pruebas de carga** (GUI consume muchos recursos)
2. **Guardar resultados** con timestamp: `results_$(date +%Y%m%d_%H%M%S).jtl`
3. **Limpiar resultados anteriores** antes de nueva ejecución
4. **Usar listeners solo en desarrollo**, deshabilitarlos en CLI
5. **Monitorear el servidor** durante pruebas de carga (CPU, memoria, logs)

## 🔄 Integración Continua

Ejemplo de script para CI/CD:

```bash
#!/bin/bash
# test-performance.sh

echo "Iniciando backend..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

echo "Esperando que el backend inicie..."
sleep 30

echo "Ejecutando pruebas JMeter..."
cd jmeter
jmeter -n -t Encomienda_API_Test_Plan.jmx \
       -l results_$(date +%Y%m%d_%H%M%S).jtl \
       -e -o report_$(date +%Y%m%d_%H%M%S)/

echo "Deteniendo backend..."
kill $BACKEND_PID

echo "Pruebas completadas. Ver reporte en jmeter/report_*/"
```

## 📚 Recursos Adicionales

- [JMeter User Manual](https://jmeter.apache.org/usermanual/index.html)
- [Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Functions Reference](https://jmeter.apache.org/usermanual/functions.html)

## 📞 Soporte

Para problemas o preguntas sobre las pruebas, consultar la documentación del backend en `backend/README.md`
