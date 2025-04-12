import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders the AI Advisor title", () => {
  render(<App />);
  const titleElement = screen.getByText(/AI Advisor/i);
  expect(titleElement).toBeInTheDocument();
});

