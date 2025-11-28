@echo off
REM SmartWomen CRM - Azure Environment Variables Setup (Windows)
REM Usage: run setup-azure-env.bat

echo Setting up Azure environment variables for SmartWomen CRM...

REM Azure OpenAI Configuration
set AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
set AZURE_OPENAI_API_KEY=your-openai-api-key-here
set AZURE_OPENAI_GPT4_DEPLOYMENT=gpt-4o
set AZURE_OPENAI_EMBEDDINGS_DEPLOYMENT=text-embedding-ada-002

REM Azure Cosmos DB Configuration
set AZURE_COSMOS_ENDPOINT=https://your-cosmos-account.documents.azure.com:443/
set AZURE_COSMOS_KEY=your-cosmos-primary-key-here
set AZURE_COSMOS_DATABASE=SmartWomenCRM
set AZURE_COSMOS_PREFERRED_REGIONS=Southeast Asia

REM Azure Content Safety Configuration
set AZURE_CONTENT_SAFETY_ENDPOINT=https://your-resource.cognitiveservices.azure.com/
set AZURE_CONTENT_SAFETY_API_KEY=your-content-safety-api-key-here

REM Azure Translator Configuration
set AZURE_TRANSLATOR_ENDPOINT=https://api.cognitive.microsofttranslator.com/
set AZURE_TRANSLATOR_API_KEY=your-translator-api-key-here
set AZURE_TRANSLATOR_REGION=eastus

REM Azure Cognitive Search Configuration
set AZURE_SEARCH_ENDPOINT=https://your-search-service.search.windows.net
set AZURE_SEARCH_API_KEY=your-search-api-key-here

REM Azure Key Vault Configuration
set AZURE_KEYVAULT_URI=https://your-keyvault.vault.azure.net/

REM Application Configuration
set MAX_CONCURRENT_REQUESTS=10
set AGENT_REQUEST_TIMEOUT=30s

echo.
echo ✅ Azure environment variables configured!
echo 💡 Remember to replace 'your-*' placeholders with your actual Azure resource values
echo.
echo To start the application with these variables:
echo   mvn spring-boot:run
pause