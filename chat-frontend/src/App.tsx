import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import GetStartedPage from "./pages/GetStartedPage";
import MainApp from "./pages/MainApp";
import "./App.css"; // Import your CSS file here

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<GetStartedPage />} />
        <Route path="/dashboard" element={<MainApp />} />
        <Route path="*" element={<Navigate to="/" />} /> {/* Redirect unknown routes */}
      </Routes>
    </Router>
  );// App.jsx
};

export default App;
