import React, { useState, useEffect } from "react";
import { Container, Tabs, Tab } from "react-bootstrap";
import NavigationBar from "../component/NavigationBar";
import Advisor from "../component/Advisor";
import AiDetection from "../component/AiDetection";


const MainApp = () => {
  const [activeTab, setActiveTab] = useState(() => localStorage.getItem("activeTab") || "chat");
  const [username, setUsername] = useState("");

  useEffect(() => {
    const savedUsername = localStorage.getItem("username");
    console.log("MainApp: Retrieved username from localStorage:", savedUsername); // Debug log
    if (savedUsername) setUsername(savedUsername);
  }, []);

  if (!username) {
    return <div className="text-center mt-5">🔄 Loading...</div>;
  }

  return (
    <div className="d-flex flex-column min-vh-100">
      <NavigationBar username={username} />

      <Container className="flex-grow-1 py-4">
        <Tabs
          activeKey={activeTab}
          onSelect={(k) => setActiveTab(k || "chat")}
          className="mb-3 modern-tabs"
          variant="pills"
        >
          <Tab eventKey="chat" title="💬 Chat Advisor">
            <Advisor username={username} />
          </Tab>

          <Tab eventKey="detect" title="🕵️ AI Detection">
            <AiDetection />
          </Tab>
        </Tabs>
      </Container>
    </div>
  );
};

export default MainApp;
