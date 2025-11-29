# 🚀 SmartWomen CRM - Azure AI-Powered Customer Management

**Sistema completo de gestión de clientes diseñado específicamente para mujeres emprendedoras en América Latina, potenciado por los servicios de IA de Microsoft Azure.**

## 🎯 Características Principales

- **🤖 IA Integrada**: Azure OpenAI para procesamiento inteligente de lenguaje
- **🛡️ Seguridad**: Azure Content Safety para moderación automática
- **🌍 Multiidioma**: Azure Translator para soporte en español, portugués y más
- **🔍 Búsqueda Avanzada**: Azure Search para encontrar información rápidamente
- **📊 Analytics**: Azure Text Analytics para análisis de sentimientos
- **🗃️ Base de Datos**: Azure Cosmos DB para almacenamiento escalable
- **🎨 Frontend Moderno**: Interfaz elegante y responsive

## 🏗️ Arquitectura del Sistema

```
SmartWomen CRM/
├── backend/          # API REST (Spring Boot)
│   ├── src/main/java # Código Java
│   └── src/main/resources/application.yml
├── frontend/         # Interfaz de usuario (React)
│   ├── src/         # Componentes React
│   └── public/      # Archivos estáticos
└── README.md        # Este archivo
```

## ⚡ Inicio Rápido

### Opción 1: Ejecutar Backend y Frontend por Separado

**Backend (Puerto 8080):**
```bash
cd SmartWomenCRM/backend
mvn spring-boot:run
```

**Frontend (Puerto 3000):**
```bash
cd SmartWomenCRM/frontend
npm install
npm start
```

### Opción 2: Con Docker (Próximamente)
```bash
docker-compose up
```

## 🔗 URLs de Acceso

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html

## 🛠️ Tecnologías

### Backend
- **Java 17** - Lenguaje principal
- **Spring Boot 3** - Framework web
- **Azure SDK** - Integración con servicios Azure
- **Maven** - Gestión de dependencias

### Frontend
- **React 18** - Framework de interfaz
- **Material-UI** - Componentes UI
- **CSS3** - Estilos personalizados
- **Node.js** - Entorno de desarrollo

### Servicios Azure
- **Azure OpenAI** - Procesamiento de lenguaje
- **Azure Cosmos DB** - Base de datos NoSQL
- **Azure Content Safety** - Moderación
- **Azure Translator** - Traducción
- **Azure Search** - Búsqueda
- **Azure Text Analytics** - Análisis
- **Azure Key Vault** - Gestión de secretos

## 🎨 Capturas de Pantalla

El frontend incluye:
- **Dashboard de servicios** con estado en tiempo real
- **Botones de prueba** para verificar conectividad
- **Estadísticas live** de servicios activos
- **Diseño responsive** para móviles y desktop
- **Tema femenino elegante** con gradientes

## 📊 Servicios Configurados

| Servicio | Estado | Endpoint | Propósito |
|----------|---------|----------|-----------|
| Azure OpenAI | ✅ | `https://brian-mihsyscv-eastus2.cognitiveservices.azure.com` | IA y NLP |
| Cosmos DB | ✅ | `https://smartwomen2025.documents.azure.com:443/` | Base de datos |
| Content Safety | ✅ | `https://smartwomen-safety.cognitiveservices.azure.com/` | Moderación |
| Translator | ✅ | `https://smartwomen-translator.cognitiveservices.azure.com/` | Traducción |
| Search | ✅ | `https://smartwomen-search.search.windows.net` | Búsqueda |
| Text Analytics | ✅ | `https://smartwomen-text.cognitiveservices.azure.com/` | Análisis |

## 🔒 Configuración Segura

La aplicación utiliza variables de entorno para máxima seguridad:

```yaml
# Configuración con fallbacks
azure:
  openai:
    endpoint: ${AZURE_OPENAI_ENDPOINT:https://tu-endpoint.com}
    api-key: ${AZURE_OPENAI_API_KEY:tu-clave-api}
```

**Para producción:**
- Configurar variables de entorno reales
- No subir credenciales a GitHub
- Usar Azure Key Vault para secretos

## 🚀 Próximas Funcionalidades

- [ ] **Dashboard de Métricas**: Análisis de rendimiento en tiempo real
- [ ] **Gestión de Clientes**: CRUD completo de información de clientes
- [ ] **Chatbot IA**: Asistente virtual integrado
- [ ] **Reportes**: Generación de informes automáticos
- [ ] **Multi-tenancy**: Soporte para múltiples organizaciones
- [ ] **Mobile App**: Aplicación nativa para iOS/Android

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama para feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👩‍💻 Para Mujeres Emprendedoras

Este sistema fue diseñado específicamente para necesidades únicas de mujeres empresarias en América Latina:

- **Interfaz intuitiva** sin curva de aprendizaje pronunciada
- **Soporte multiidioma** para mercados diversos
- **IA culturalmente sensible** adaptada a contextos latinos
- **Funcionalidades de sesgo** para prevenir discriminación
- **Análisis de sentimientos** para mejor atención al cliente

## 🔧 Soporte Técnico

Para soporte técnico o preguntas:
- **Email**: soporte@smartwomancrm.com
- **GitHub Issues**: Crear issue en este repositorio
- **Documentación**: Ver carpetas `docs/` y `backend/README.md`

---
LINK OFICIAL: https://youtu.be/w8ObmUukhRc <- DEMO LIVE APLICACION!

**🎉 ¡Construido con ❤️ para empoderar a mujeres emprendedoras en la era digital!**

**Powered by Microsoft Azure AI** | **Optimizado para América Latina** | **Diseño Inclusivo**
