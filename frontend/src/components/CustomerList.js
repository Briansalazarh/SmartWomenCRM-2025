import React, { useState, useEffect } from 'react';
import './CustomerList.css';

const CustomerList = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/customers');
      const data = await response.json();
      setCustomers(data);
    } catch (error) {
      console.error('Error cargando clientes:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('es-ES');
  };

  if (loading) {
    return <div className="loading">Cargando clientes...</div>;
  }

  return (
    <div className="customers-container">
      <div className="customers-header">
        <h2>📊 Base de Clientes - SmartWomen CRM</h2>
        <p>Clientes atendidos por nuestros agentes IA</p>
      </div>

      <div className="customers-list">
        {customers.length === 0 ? (
          <div className="no-customers">
            <h3>🌟 Aún no hay clientes</h3>
            <p>¡Usa el chat inteligente para crear el primer cliente!</p>
            <button onClick={() => window.location.href='/#chat'}>
              Ir al Chat
            </button>
          </div>
        ) : (
          customers.map(customer => (
            <div key={customer.customerId} className="customer-card">
              <div className="customer-header">
                <h3>{customer.name}</h3>
                <span className="country-flag">
                  {customer.country === 'Mexico' ? '🇲🇽' : 
                   customer.country === 'Colombia' ? '🇨🇴' : '🇦🇷'}
                </span>
              </div>
              
              <div className="customer-details">
                <p><strong>Email:</strong> {customer.email}</p>
                <p><strong>País:</strong> {customer.country}</p>
                <p><strong>Sector:</strong> {customer.industry}</p>
                <p><strong>Tamaño:</strong> {customer.businessSize}</p>
                <p><strong>Última interacción:</strong> {formatDate(customer.lastInteraction)}</p>
              </div>

              {customer.lastMessage && (
                <div className="last-message">
                  <strong>Último mensaje:</strong>
                  <p>{customer.lastMessage}</p>
                </div>
              )}

              {customer.lastAgentResults && (
                <details className="agent-results">
                  <summary>📊 Resultados de Agentes</summary>
                  <div className="results-grid">
                    {customer.lastAgentResults.LanguageDetector && (
                      <div className="result-item">
                        <span>🌍</span>
                        <span>{customer.lastAgentResults.LanguageDetector.detectedLanguage}</span>
                      </div>
                    )}
                    {customer.lastAgentResults.SentimentAnalyzer && (
                      <div className="result-item">
                        <span>📊</span>
                        <span>{customer.lastAgentResults.SentimentAnalyzer.sentiment}</span>
                      </div>
                    )}
                    {customer.lastAgentResults.BiasGuard && (
                      <div className="result-item">
                        <span>🛡️</span>
                        <span>{customer.lastAgentResults.BiasGuard.biasDetected ? '⚠️' : '✅'}</span>
                      </div>
                    )}
                  </div>
                </details>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default CustomerList;