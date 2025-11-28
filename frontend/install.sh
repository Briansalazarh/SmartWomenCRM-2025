#!/bin/bash

echo "🎨 SmartWomen CRM - Instalación Frontend"
echo "======================================="

# Verificar Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js no está instalado. Por favor instala Node.js 16+ primero."
    exit 1
fi

# Verificar npm
if ! command -v npm &> /dev/null; then
    echo "❌ npm no está instalado. Por favor instala npm primero."
    exit 1
fi

echo "✅ Node.js y npm encontrados"

# Crear directorio público si no existe
if [ ! -d "public" ]; then
    mkdir public
    echo "📁 Directorio public creado"
fi

# Instalar dependencias
echo "📦 Instalando dependencias..."
npm install

if [ $? -eq 0 ]; then
    echo "✅ Dependencias instaladas correctamente"
else
    echo "❌ Error al instalar dependencias"
    exit 1
fi

echo ""
echo "🎉 ¡Instalación completada!"
echo ""
echo "Para ejecutar el frontend:"
echo "  cd SmartWomenCRM/frontend"
echo "  npm start"
echo ""
echo "El frontend estará disponible en: http://localhost:3000"
echo ""
echo "📝 Asegúrate de que el backend esté corriendo en http://localhost:8080"