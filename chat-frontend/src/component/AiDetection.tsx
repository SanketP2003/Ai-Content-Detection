import React, { useState, useEffect } from 'react';
import {
  Button, Alert, Spinner, ProgressBar,
  Card, Row, Col, Badge, Form, InputGroup
} from 'react-bootstrap';
import { saveAs } from 'file-saver';
import './AiDetection.css'; // Assuming you have a CSS file for styles

interface DetectionResult {
  probability: number;
  metrics?: {
    perplexity: number;
    burstiness: number;
    consistency: number;
  };
  patterns: string[];
  analysis: string;
}

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const AiDetection = () => {
  const [detectionText, setDetectionText] = useState('');
  const [detectionResult, setDetectionResult] = useState<DetectionResult | null>(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState('');

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (!selectedFile) return;

    if (selectedFile.size > 1024 * 1024) {
      setError('File size exceeds 1MB limit');
      return;
    }

    setFile(selectedFile);
    const reader = new FileReader();
    reader.onload = (event) => {
      setDetectionText(event.target?.result as string);
    };
    reader.readAsText(selectedFile);
  };

  const handleBulkDetection = async () => {
    setIsAnalyzing(true);
    setError('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/detect/bulk-ai`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: detectionText }),
      });

      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

      const result: DetectionResult = await response.json();
      setDetectionResult(result);
      saveResultToHistory(result);

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Detection failed');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const saveResultToHistory = (result: DetectionResult) => {
    const history = JSON.parse(localStorage.getItem('detectionHistory') || '[]');
    const newEntry = {
      timestamp: Date.now(),
      text: detectionText.substring(0, 100) + '...',
      result
    };
    localStorage.setItem('detectionHistory', JSON.stringify([newEntry, ...history]));
  };

  const getProbabilityColor = (probability: number) => {
    if (probability >= 75) return 'danger';
    if (probability >= 45) return 'warning';
    return 'success';
  };

  return (
    <div className="detection-interface">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="gradient-text m-0">AI Content Detector</h2>
      </div>

      <InputGroup className="mb-3">
        <Form.Control
          as="textarea"
          rows={10}
          value={detectionText}
          onChange={(e) => setDetectionText(e.target.value)}
          placeholder="Paste your text here (minimum 10 lines)..."
          style={{ borderRadius: '15px', resize: 'none' }}
        />
      </InputGroup>

      <div className="d-flex gap-3 mb-4">
        <Button
          variant="primary"
          onClick={handleBulkDetection}
          disabled={isAnalyzing || detectionText.split('\n').filter(l => l.trim()).length < 10}
        >
          {isAnalyzing ? (
            <>
              <Spinner size="sm" className="me-2" />
              Analyzing...
            </>
          ) : 'Analyze Text'}
        </Button>

        <label className="btn btn-outline-secondary">
          <input
            type="file"
            accept=".txt,.text"
            onChange={handleFileUpload}
            style={{ display: 'none' }}
          />
          Upload Text File
        </label>
      </div>

      {file && (
        <div className="mb-3 text-muted">
          <small>
            Loaded file: {file.name} ({Math.round(file.size / 1024)}KB)
          </small>
        </div>
      )}

      {error && <Alert variant="danger">{error}</Alert>}

      {detectionResult && (
        <Card className="mt-3 shadow">
          <Card.Body>
            <Row>
              <Col md={4}>
                <div className="probability-gauge mb-4">
                  <ProgressBar
                    now={detectionResult.probability}
                    label={
                      <span className="gauge-label">
                        {detectionResult.probability}%
                        <small>AI Probability</small>
                      </span>
                    }
                    variant={getProbabilityColor(detectionResult.probability)}
                    style={{ height: '120px', borderRadius: '60px' }}
                  />
                </div>

                {detectionResult.metrics && (
                  <div className="metrics-grid">
                    <div className="metric-item">
                      <span>Perplexity</span>
                      <strong>{detectionResult.metrics.perplexity.toFixed(1)}</strong>
                    </div>
                    <div className="metric-item">
                      <span>Burstiness</span>
                      <strong>{detectionResult.metrics.burstiness.toFixed(1)}</strong>
                    </div>
                    <div className="metric-item">
                      <span>Consistency</span>
                      <strong>{detectionResult.metrics.consistency.toFixed(1)}</strong>
                    </div>
                  </div>
                )}
              </Col>

              <Col md={8}>
                <div className="mb-3">
                  <h6>Detection Patterns ({detectionResult.patterns.length} found):</h6>
                  <div className="d-flex flex-wrap gap-2">
                    {detectionResult.patterns.map((pattern, i) => (
                      <Badge key={i} bg="secondary">{pattern}</Badge>
                    ))}
                  </div>
                </div>

                <div className="analysis-breakdown">
                  <h6>Detailed Analysis:</h6>
                  <p className="text-muted mt-2">{detectionResult.analysis}</p>
                </div>
              </Col>
            </Row>

            <div className="mt-3 d-flex gap-2">
              <Button 
                variant="outline-primary" 
                onClick={() => {
                  saveAs(
                    new Blob([JSON.stringify(detectionResult)], { type: 'application/json' }),
                    `ai-detection-${Date.now()}.json`
                  );
                }}
              >
                Export Full Report
              </Button>
              <Button variant="outline-secondary" onClick={() => setDetectionResult(null)}>
                Clear Analysis
              </Button>
            </div>
          </Card.Body>
        </Card>
      )}
    </div>
  );
};

export default AiDetection;