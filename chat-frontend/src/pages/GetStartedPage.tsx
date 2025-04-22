import React, { useState } from "react";
import { Button, Form, Container, Card, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

const GetStartedPage = () => {
  const [username, setUsername] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) {
      setError("Please enter a valid username");
      return;
    }

    localStorage.setItem("username", username.trim());

    // ✅ Ensure navigation happens AFTER storage update
    setTimeout(() => {
      navigate("/dashboard");
    }, 100);
  };

  return (
    <Container className="d-flex flex-column justify-content-center align-items-center min-vh-100">
      <Card className="glass-pane p-4" style={{ width: "100%", maxWidth: "500px" }}>
        <Card.Body>
          <h1 className="text-center mb-4 gradient-text">Welcome to AI Advisor</h1>
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-4">
              <Form.Label className="text-primary">Enter Your Name</Form.Label>
              <Form.Control
                type="text"
                placeholder="John Doe"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value);
                  setError("");
                }}
                size="lg"
              />
              {error && <Alert variant="danger" className="mt-2">{error}</Alert>}
            </Form.Group>

            <div className="d-grid gap-3">
              <Button variant="primary" size="lg" type="submit">
                Get Started
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default GetStartedPage;
