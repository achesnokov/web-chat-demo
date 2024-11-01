import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './styles/index.css';

/**
 * Entry point for the React application.
 * Renders the App component into the root DOM element.
 */
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <App />
);
