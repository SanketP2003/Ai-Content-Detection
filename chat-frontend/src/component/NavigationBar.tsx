import React from "react";
import { Navbar, Container, Nav, Badge, Dropdown, Button } from "react-bootstrap";
import { NavLink } from "react-router-dom";

const NavigationBar = ({ username }: { username: string }) => {

  const handleLogout = () => {
    localStorage.removeItem("username");
    window.location.href = "/"; // ✅ Reloads to reset state
  };

  return (
    <Navbar bg="dark" variant="dark" expand="lg" className="mb-4">
      <Container>
        <Navbar.Brand as={NavLink} to="/">
          <span className="gradient-text">AI Advisor</span>
          <Badge bg="secondary" className="ms-2">Beta</Badge>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav>
            <Dropdown align="end">
              <Dropdown.Toggle variant="outline-light" id="user-dropdown">
                <i className="bi bi-person-circle me-2"></i> {username}
              </Dropdown.Toggle>
              <Dropdown.Menu>
                <Dropdown.Item as={Button} variant="danger" onClick={handleLogout}>
                  🚪 Logout
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default NavigationBar;
