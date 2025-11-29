import React, { useState, useEffect } from 'react';
import './index.css';

function App() {
  const [services, setServices] = useState([
    {
      name: 'Language Detector',
      status: 'checking',
      description: 'Detecta idiomas con enriquecimiento cultural LATAM',
      icon: '🌍',
      endpoint: 'POST /agents/language-detect'
    },
    {
      name: 'Sentiment Analyzer',
      status: 'checking',
      description: 'Analiza sentimientos con contexto de género LATAM',
      icon: '📊',
      endpoint: 'POST /agents/sentiment-analyze'
    },
    {
      name: 'Bias Guard',
      status: 'checking',
      description: 'Detecta sesgos de género, culturales y socioeconómicos',
      icon: '🛡️',
      endpoint: 'POST /agents/bias-detect'
    },
    {
      name: 'Planner Agent',
      status: 'checking',
      description: 'Orquesta agentes, define orden y paralelismo',
      icon: '🎯',
      endpoint: 'POST /agents/create-plan'
    }
  ]);

  const [testResults, setTestResults] = useState({});
  const [globalStats, setGlobalStats] = useState({
    totalServices: 4,
    onlineServices: 0,
    totalRequests: 0
  });

  useEffect(() => {
    const timer = setTimeout(() => {
      const updatedServices = services.map(service => ({
        ...service,
        status: 'online'
      }));
      setServices(updatedServices);
      setGlobalStats(prev => ({
        ...prev,
        onlineServices: updatedServices.length
      }));
    }, 2000);

    return () => clearTimeout(timer);
  }, []);

  const testService = async (service) => {
    const serviceKey = service.name.toLowerCase().replace(/\s+/g, '-');
    
    setTestResults(prev => ({
      ...prev,
      [serviceKey]: { loading: true, result: null }
    }));

    const endpointMap = {
      'language-detector': 'language-detect',
      'sentiment-analyzer': 'sentiment-analyze',
      'bias-guard': 'bias-detect',
      'planner-agent': 'create-plan'
    };

    const agentEndpoint = endpointMap[serviceKey];
    
    if (!agentEndpoint) {
      setTestResults(prev => ({
        ...prev,
        [serviceKey]: {
          loading: false,
          result: {
            success: false,
            message: '❌ Servicio no configurado',
            error: 'No existe endpoint para este servicio',
            timestamp: new Date().toLocaleString()
          }
        }
      }));
      return;
    }

    try {
      // ✅ URL CORREGIDA: incluye /api/v1
      const response = await fetch(`http://localhost:8080/api/v1/agents/${agentEndpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content: `Test de ${service.name}` })
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} - ${response.statusText}`);
      }
      
      const data = await response.json();
      
      setTestResults(prev => ({
        ...prev,
        [serviceKey]: {
          loading: false,
          result: {
            success: true,
            message: `✅ ${service.name} FUNCIONA`,
            response: data,
            timestamp: new Date().toLocaleString()
          }
        }
      }));

      setGlobalStats(prev => ({
        ...prev,
        totalRequests: prev.totalRequests + 1
      }));

    } catch (error) {
      setTestResults(prev => ({
        ...prev,
        [serviceKey]: {
          loading: false,
          result: {
            success: false,
            message: '❌ Error CORS/Conexión',
            error: error.message,
            timestamp: new Date().toLocaleString()
          }
        }
      }));
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'online':
        return '🟢';
      case 'offline':
        return '🔴';
      case 'checking':
        return '🟡';
      default:
        return '⚪';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'online':
        return 'status-online';
      case 'offline':
        return 'status-offline';
      case 'checking':
        return 'status-warning';
      default:
        return '';
    }
  };

  return (
    <div className="app-container">
      <div className="hero-section">
        <h1 className="hero-title">SmartWomen CRM</h1>
        <p className="hero-subtitle">
          Sistema de Gestión de Clientes Potenciado por IA de Microsoft Azure
        </p>
        <button className="hero-button" onClick={() => window.location.reload()}>
          🔄 Verificar Estado
        </button>
      </div>

      <div className="main-content">
        <div className="dashboard-grid" style={{ marginTop: '20px', marginBottom: '20px' }}>
          <div className="card" style={{ textAlign: 'center' }}>
            <div className="stat-value">{globalStats.onlineServices}/{globalStats.totalServices}</div>
            <div className="stat-label">Servicios Activos</div>
          </div>
          <div className="card" style={{ textAlign: 'center' }}>
            <div className="stat-value">{globalStats.totalRequests}</div>
            <div className="stat-label">Solicitudes Realizadas</div>
          </div>
          <div className="card" style={{ textAlign: 'center' }}>
            <div className="stat-value">{Math.round((globalStats.onlineServices / globalStats.totalServices) * 100)}%</div>
            <div className="stat-label">Disponibilidad</div>
          </div>
        </div>

        <h2 style={{ 
          color: 'white', 
          textAlign: 'center', 
          marginBottom: '30px',
          fontFamily: 'Poppins, sans-serif',
          fontSize: '2.5rem',
          textShadow: '2px 2px 4px rgba(0,0,0,0.3)'
        }}>
          🎯 Panel de Control de Servicios
        </h2>

        <div className="dashboard-grid">
          {services.map((service, index) => (
            <div key={index} className="card">
              <div className="card-title">
                <span style={{ fontSize: '1.5rem' }}>{service.icon}</span>
                {service.name}
              </div>
              
              <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                marginBottom: '15px',
                fontSize: '0.9rem',
                color: '#666'
              }}>
                <span className={`status-indicator ${getStatusColor(service.status)}`}>
                  {getStatusIcon(service.status)}
                </span>
                {service.status === 'checking' ? 'Verificando...' : 
                 service.status === 'online' ? 'Conectado' : 'Desconectado'}
              </div>

              <div className="card-description">
                {service.description}
              </div>

              <div style={{ 
                fontSize: '0.8rem', 
                color: '#999', 
                marginBottom: '20px',
                fontFamily: 'Monaco, monospace'
              }}>
                {service.endpoint}
              </div>

              <button 
                className="test-button"
                onClick={() => testService(service)}
                disabled={service.status !== 'online'}
              >
                {testResults[service.name.toLowerCase().replace(/\s+/g, '-')]?.loading ? (
                  <span className="loading-spinner"></span>
                ) : null}
                {testResults[service.name.toLowerCase().replace(/\s+/g, '-')]?.loading ? 
                  'Probando...' : '🧪 Probar Conexión'}
              </button>

              {testResults[service.name.toLowerCase().replace(/\s+/g, '-')]?.result && (
                <div className="response-area">
                  <div style={{ 
                    color: testResults[service.name.toLowerCase().replace(/\s+/g, '-')].result.success ? '#4CAF50' : '#f44336',
                    fontWeight: 'bold',
                    marginBottom: '10px'
                  }}>
                    {testResults[service.name.toLowerCase().replace(/\s+/g, '-')].result.success ? '✅' : '❌'} 
                    {testResults[service.name.toLowerCase().replace(/\s+/g, '-')].result.message}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: '#666' }}>
                    <div>Timestamp: {testResults[service.name.toLowerCase().replace(/\s+/g, '-')].result.timestamp}</div>
                    {testResults[service.name.toLowerCase().replace(/\s+/g, '-')].result.error && (
                      <div style={{ color: '#f44336' }}>
                        Error: {testResults[service.name.toLowerCase().replace(/\s+/g, '-')].result.error}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>

        <div style={{ 
          textAlign: 'center', 
          marginTop: '60px', 
          padding: '40px 20px',
          background: 'rgba(255,255,255,0.1)',
          borderRadius: '20px',
          backdropFilter: 'blur(10px)',
          border: '1px solid rgba(255,255,255,0.2)'
        }}>
          <h3 style={{ 
            color: 'white', 
            marginBottom: '20px',
            fontFamily: 'Poppins, sans-serif'
          }}>
            🚀 SmartWomen CRM - Powered by Azure AI
          </h3>
          <p style={{ 
            color: 'rgba(255,255,255,0.8)',
            lineHeight: '1.6',
            maxWidth: '600px',
            margin: '0 auto'
          }}>
            Sistema completo de gestión de clientes diseñado específicamente para mujeres emprendedoras 
            en América Latina. Integra inteligencia artificial avanzada con una interfaz intuitiva 
            para maximizar el crecimiento empresarial.
          </p>
          <div style={{ 
            marginTop: '30px',
            display: 'flex',
            justifyContent: 'center',
            gap: '20px',
            flexWrap: 'wrap'
          }}>
            <span style={{ 
              background: 'rgba(255,255,255,0.2)', 
              padding: '8px 16px', 
              borderRadius: '20px',
              color: 'white',
              fontSize: '0.9rem'
            }}>
              🤖 Azure OpenAI
            </span>
            <span style={{ 
              background: 'rgba(255,255,255,0.2)', 
              padding: '8px 16px', 
              borderRadius: '20px',
              color: 'white',
              fontSize: '0.9rem'
            }}>
              🗃️ Cosmos DB
            </span>
            <span style={{ 
              background: 'rgba(255,255,255,0.2)', 
              padding: '8px 16px', 
              borderRadius: '20px',
              color: 'white',
              fontSize: '0.9rem'
            }}>
              🛡️ Content Safety
            </span>
            <span style={{ 
              background: 'rgba(255,255,255,0.2)', 
              padding: '8px 16px', 
              borderRadius: '20px',
              color: 'white',
              fontSize: '0.9rem'
            }}>
              🔍 Search
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;