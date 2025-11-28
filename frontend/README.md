# 🎨 SmartWomen CRM - Frontend

Interfaz de usuario elegante y funcional para el sistema SmartWomen CRM.

## 🚀 Características

- **Interfaz Moderna**: Diseño responsive con gradientes atractivos
- **Dashboard de Servicios**: Monitoreo en tiempo real de todos los servicios Azure
- **Testing Integrado**: Botones para probar la conectividad con cada servicio
- **Estadísticas Live**: Contadores dinámicos de servicios activos y solicitudes
- **Mobile Friendly**: Optimizado para dispositivos móviles

## 📱 Servicios Monitoreados

1. 🤖 **Azure OpenAI** - Procesamiento de lenguaje natural
2. 🗃️ **Azure Cosmos DB** - Base de datos NoSQL
3. 🛡️ **Azure Content Safety** - Moderación de contenido
4. 🌍 **Azure Translator** - Traducción automática
5. 🔍 **Azure Search** - Búsqueda inteligente
6. 📊 **Azure Text Analytics** - Análisis de sentimientos

## 🛠️ Instalación y Ejecución

### Prerrequisitos
- Node.js 16+ instalado
- Backend corriendo en http://localhost:8080

### Pasos de Instalación

```bash
# 1. Instalar dependencias
cd SmartWomenCRM/frontend
npm install

# 2. Ejecutar en modo desarrollo
npm start
```

La aplicación se abrirá automáticamente en http://localhost:3000

### Comandos Disponibles

```bash
npm start          # Ejecutar en modo desarrollo
npm run build      # Crear build de producción
npm test           # Ejecutar tests
npm run eject      # Eject (no recomendado)
```

## 🔗 Conectividad con Backend

El frontend se conecta automáticamente con el backend en:
- **Local**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## 🎨 Tecnologías Utilizadas

- **React 18** - Framework principal
- **Material-UI** - Componentes UI
- **CSS3** - Estilos personalizados con gradientes
- **Axios** - Cliente HTTP
- **React Router** - Navegación

## 📱 Responsive Design

- **Desktop**: Grid de 3 columnas para servicios
- **Tablet**: Grid de 2 columnas
- **Mobile**: Stack vertical para mejor usabilidad

## 🔍 Testing de Servicios

Cada servicio tiene un botón de prueba que:
1. ✅ Verifica conectividad con el backend
2. 📊 Muestra respuesta de la API
3. ⏱️ Registra timestamp de la prueba
4. 🔢 Incrementa contador de solicitudes

## 🎯 Próximas Funcionalidades

- [ ] Dashboard de métricas avanzadas
- [ ] Gestión de clientes CRUD
- [ ] Chatbot integrado con IA
- [ ] Reportes y analíticas
- [ ] Autenticación de usuarios
- [ ] Tema claro/oscuro

## 📞 Soporte

Para problemas o mejoras, contacta al equipo de desarrollo.

---

**🎨 Diseñado con ❤️ para mujeres emprendedoras en América Latina**