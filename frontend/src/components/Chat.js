import React, { useState, useEffect } from 'react';
import './Chat.css';

const Chat = () => {
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'bot',
      text: '¡Hola! Soy tu asistente de SmartWomen CRM. ¿Cuál es tu nombre y país? ¿Qué necesitas para tu negocio?',
      timestamp: new Date().toLocaleString()
    }
  ]);
  
  const [inputMessage, setInputMessage] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [customerId, setCustomerId] = useState(null);
  const [customerData, setCustomerData] = useState({});

  useEffect(() => {
    const savedCustomer = localStorage.getItem('swcrm_customer');
    if (savedCustomer) {
      const customer = JSON.parse(savedCustomer);
      setCustomerId(customer.customerId);
      setCustomerData(customer);
    }
  }, []);

  const saveCustomerToCosmos = async (customerInfo) => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customerInfo)
      });
      
      if (response.ok) {
        const savedCustomer = await response.json();
        setCustomerId(savedCustomer.customerId);
        setCustomerData(savedCustomer);
        localStorage.setItem('swcrm_customer', JSON.stringify(savedCustomer));
        return savedCustomer;
      }
    } catch (error) {
      console.error('Error guardando cliente:', error);
    }
    return null;
  };

  const updateCustomerInteraction = async (message, agentResults) => {
    if (!customerId) return;

    try {
      await fetch(`http://localhost:8080/api/v1/customers/${customerId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...customerData,
          lastMessage: message,
          lastAgentResults: agentResults,
          lastInteraction: new Date().toISOString()
        })
      });
    } catch (error) {
      console.error('Error actualizando cliente:', error);
    }
  };

  const sendMessage = async () => {
    if (!inputMessage.trim()) return;

    const userMessage = {
      id: messages.length + 1,
      sender: 'user',
      text: inputMessage,
      timestamp: new Date().toLocaleString()
    };
    
    setMessages(prev => [...prev, userMessage]);
    const userContent = inputMessage;
    setInputMessage('');
    setIsProcessing(true);

    try {
      // 1. Planner crea estrategia
      const planResponse = await fetch('http://localhost:8080/api/v1/agents/create-plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: userContent })
      });
      
      const planData = await planResponse.json();
      
      // 2. Extraer datos del cliente
      let name = "Usuario";
      let country = "Mexico";
      let industry = "General";
      let businessSize = "small";
      
      const contentLower = userContent.toLowerCase();
      if (contentLower.includes('soy')) {
        const parts = userContent.split('soy')[1]?.split(',')[0]?.trim();
        if (parts) name = parts;
      }
      if (contentLower.includes('méxico') || contentLower.includes('mexico')) country = "Mexico";
      else if (contentLower.includes('colombia')) country = "Colombia";
      else if (contentLower.includes('argentina')) country = "Argentina";

      // 3. Guardar nuevo cliente si no existe
      if (!customerId) {
        const newCustomer = {
          name: name,
          email: `cliente_${Date.now()}@smartwomen.local`,
          country: country,
          industry: industry,
          businessSize: businessSize
        };
        await saveCustomerToCosmos(newCustomer);
      }

      // 4. Ejecutar agentes según plan
      const executionOrder = planData.metadata.executionOrder;
      let agentResults = {};

      for (const agentName of executionOrder) {
        if (agentName === 'Planner' || agentName === 'ResponseGenerator' || agentName === 'Handoff') continue;

        const endpointMap = {
          'LanguageDetector': 'language-detect',
          'SentimentAnalyzer': 'sentiment-analyze',
          'BiasGuard': 'bias-detect'
        };

        const endpoint = endpointMap[agentName];
        if (!endpoint) continue;

        try {
          const response = await fetch(`http://localhost:8080/api/v1/agents/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: userContent })
          });

          if (response.ok) {
            const data = await response.json();
            agentResults[agentName] = data;
          }
        } catch (error) {
          console.error(`Error en ${agentName}:`, error);
        }
      }

      // 5. Generar respuesta personalizada
      let botMessageText = `📊 **Análisis completado para ${name}:**\n\n`;
      botMessageText += `🌍 **Idioma:** ${agentResults.LanguageDetector?.detectedLanguage || 'es-MX'}\n`;
      botMessageText += `😊 **Sentimiento:** ${agentResults.SentimentAnalyzer?.sentiment || 'Neutral'} `;
      botMessageText += `(Intensidad: ${agentResults.SentimentAnalyzer?.intensity || 0})\n`;
      botMessageText += `🛡️ **Sesgos:** ${agentResults.BiasGuard?.biasDetected ? '⚠️ Detectados' : '✅ No detectados'}\n\n`;
      botMessageText += `🎯 **Recomendación personalizada para ${country}:**\n`;
      botMessageText += `Basado en tu contexto, te sugiero enfocarte en estrategias de crecimiento con perspectiva de género y sensibilidad cultural latinoamericana.`;

      // 6. Actualizar cliente con resultados
      await updateCustomerInteraction(userContent, agentResults);

      const botMessage = {
        id: messages.length + 2,
        sender: 'bot',
        text: botMessageText,
        timestamp: new Date().toLocaleString(),
        agentResults: agentResults
      };

      setMessages(prev => [...prev, botMessage]);

    } catch (error) {
      const errorMessage = {
        id: messages.length + 2,
        sender: 'bot',
        text: `❌ Lo siento, ocurrió un error: ${error.message}`,
        timestamp: new Date().toLocaleString()
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !isProcessing) {
      sendMessage();
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h2>💬 Chat Inteligente - SmartWomen CRM</h2>
        <p>Agentes IA especializados en negocios liderados por mujeres en LATAM</p>
        {customerId && (
          <div className="customer-info">
            🟢 Cliente activo: <strong>{customerData.name}</strong> ({customerData.country})
          </div>
        )}
      </div>

      <div className="chat-messages">
        {messages.map(msg => (
          <div key={msg.id} className={`message ${msg.sender}`}>
            <div className="message-content">
              <p style={{ whiteSpace: 'pre-line', margin: 0 }}>{msg.text}</p>
              {msg.agentResults && (
                <details className="debug-info">
                  <summary>🔍 Ver detalles técnicos</summary>
                  <pre>{JSON.stringify(msg.agentResults, null, 2)}</pre>
                </details>
              )}
            </div>
            <span className="timestamp">{msg.timestamp}</span>
          </div>
        ))}
        
        {isProcessing && (
          <div className="message bot typing">
            <div className="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        )}
      </div>

      <div className="chat-input">
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Ej: Soy Ana de México, tengo un negocio de artesanías y quiero más clientes..."
          disabled={isProcessing}
        />
        <button onClick={sendMessage} disabled={isProcessing}>
          {isProcessing ? 'Procesando...' : 'Enviar'}
        </button>
      </div>

      <style jsx>{`
        .customer-info {
          background: rgba(255,255,255,0.2);
          padding: 8px 16px;
          border-radius: 20px;
          font-size: 0.9rem;
          margin-top: 10px;
          display: inline-block;
        }
      `}</style>
    </div>
  );
};

export default Chat;