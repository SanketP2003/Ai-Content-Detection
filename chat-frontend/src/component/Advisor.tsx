import React, { useReducer, useEffect, useRef, JSX } from 'react';
import { 
  Form, Button, Alert, Spinner, InputGroup 
} from 'react-bootstrap';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface Message {
  id: string;
  content: string;
  isBot: boolean;
  timestamp: number;
}

interface State {
  messages: Message[];
  isLoading: boolean;
  error: string;
}

type Action =
  | { type: 'ADD_USER_MESSAGE'; payload: Message }
  | { type: 'ADD_BOT_MESSAGE'; payload: Message }
  | { type: 'SET_LOADING' }
  | { type: 'SET_ERROR'; payload: string }
  | { type: 'CLEAR_CHAT' }
  | { type: 'LOAD_MESSAGES'; payload: Message[] };

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const chatReducer = (state: State, action: Action): State => {
  switch (action.type) {
    case 'ADD_USER_MESSAGE':
      return { ...state, messages: [...state.messages, action.payload] };
    case 'ADD_BOT_MESSAGE':
      return { ...state, messages: [...state.messages, action.payload], isLoading: false };
    case 'SET_LOADING':
      return { ...state, isLoading: true, error: '' };
    case 'SET_ERROR':
      return { ...state, isLoading: false, error: action.payload };
    case 'CLEAR_CHAT':
      localStorage.removeItem('chatHistory');
      return { ...state, messages: [] };
    case 'LOAD_MESSAGES':
      return { ...state, messages: action.payload };
    default:
      return state;
  }
};

const Advisor = ({ username }: { username: string }) => {
  const [state, dispatch] = useReducer(chatReducer, {
    messages: [],
    isLoading: false,
    error: '',
  });

  const [input, setInput] = React.useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const savedHistory = localStorage.getItem('chatHistory');
    if (savedHistory) {
      dispatch({ type: 'LOAD_MESSAGES', payload: JSON.parse(savedHistory) });
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('chatHistory', JSON.stringify(state.messages));
  }, [state.messages]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [state.messages, state.isLoading]);

  const handleChatSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      content: input,
      isBot: false,
      timestamp: Date.now(),
    };

    dispatch({ type: 'ADD_USER_MESSAGE', payload: userMessage });
    setInput('');
    dispatch({ type: 'SET_LOADING' });

    try {
      const response = await fetch(`${API_BASE_URL}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: input }),
      });
      
      if (!response.ok) throw new Error('Failed to get response');
      
      const data = await response.json();
      console.log('Received:', data);
      
      const botMessage: Message = {
        id: `bot-${Date.now()}`,
        content: data,
        isBot: true,
        timestamp: Date.now(),
      };
      
      dispatch({ type: 'ADD_BOT_MESSAGE', payload: botMessage });      
    } catch (err) {
      dispatch({ type: 'SET_ERROR', payload: err instanceof Error ? err.message : 'Request failed' });
    }
  }    

  const formatCodeBlocks = (content: string) => {
    const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g;
    const parts: (string | JSX.Element)[] = [];
    let lastIndex = 0;

    content.replace(codeBlockRegex, (match, lang, code, offset) => {
      parts.push(content.slice(lastIndex, offset));
      parts.push(
        <div className="code-block position-relative" key={offset}>
          <SyntaxHighlighter
            language={lang || 'javascript'}
            style={vscDarkPlus}
            customStyle={{ borderRadius: '8px', padding: '1rem' }}
          >
            {code.trim()}
          </SyntaxHighlighter>
          <Button
            variant="outline-light"
            size="sm"
            className="position-absolute top-0 end-0 m-2"
            onClick={() => navigator.clipboard.writeText(code.trim())}
          >
            📋 Copy
          </Button>
        </div>
      );
      lastIndex = offset + match.length;
      return match;
    });

    parts.push(content.slice(lastIndex));
    return parts;
  };

  const renderMessage = (message: Message) => (
    <div key={message.id} className={`d-flex ${message.isBot ? 'justify-content-start' : 'justify-content-end'} mb-2`}>
      <div className={`message-bubble ${message.isBot ? 'bot' : 'user'}`}>
        <div className="message-content">{formatCodeBlocks(message.content)}</div>
        <div className="message-meta">
          <small className="text-muted">
            {new Date(message.timestamp).toLocaleTimeString()} • {message.isBot ? ' AI Advisor' : ` ${username}`}
          </small>
        </div>
      </div>
    </div>
  );

  return (
    <div className="chat-interface">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="gradient-text m-0">AI Advisor</h2>
        <Button variant="outline-secondary" size="sm" onClick={() => dispatch({ type: 'CLEAR_CHAT' })}>
          Clear Chat
        </Button>
      </div>

      <div className="chat-container mb-3">
        {state.messages.map(renderMessage)}
        {state.isLoading && (
          <div className="d-flex justify-content-start mb-2">
            <div className="message-bubble bot">
              <Spinner animation="border" size="sm" />
              <span className="ms-2">Generating response...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <Form onSubmit={handleChatSubmit}>
        <InputGroup>
          <Form.Control
            as="textarea"
            rows={3}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask your question..."
            style={{ borderRadius: '20px', resize: 'none' }}
          />
          <Button
            variant="primary"
            type="submit"
            disabled={state.isLoading || !input.trim()}
            style={{ borderRadius: '0 20px 20px 0' }}
          >
            {state.isLoading ? 'Sending...' : 'Send'}
          </Button>
        </InputGroup>
      </Form>

      {state.error && <Alert variant="danger" className="mt-3">{state.error}</Alert>}
    </div>
  );
};

export default Advisor;
