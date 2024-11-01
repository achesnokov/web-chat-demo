import React, { useEffect, useState } from 'react';
import Login from './components/Login';
import ChatList from './components/ChatList';
import ChatWindow from './components/ChatWindow';
import { WebSocketProvider } from './components/WebSocketContext';
import './styles/App.css';

const originalFetch = window.fetch;
window.fetch = async (url, options = {}) => {
    /**
     * Custom fetch function to include JWT token in the headers for authenticated requests.
     *
     * @param {string} url - The URL to fetch.
     * @param {object} [options={}] - The options for the fetch request.
     * @returns {Promise<Response>} The fetch response.
     */
    if (!url.includes('/auth/login') && !url.includes('/auth/register')) {
        const token = localStorage.getItem('jwt');
        if (token) {
            options.headers = {
                ...options.headers,
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            };
        }
    }
    return originalFetch(url, options);
};

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [selectedChatId, setSelectedChatId] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        /**
         * Effect to validate JWT token on initial load.
         * If the token is valid, sets the user as authenticated.
         * If the token is invalid or an error occurs, removes the token and sets the user as unauthenticated.
         */
        const token = localStorage.getItem('jwt');
        if (token) {
            const validateToken = async () => {
                try {
                    const response = await fetch('/api/auth/validate');
                    if (response.ok) {
                        setIsAuthenticated(true);
                    } else {
                        localStorage.removeItem('jwt');
                        setIsAuthenticated(false);
                    }
                } catch (error) {
                    console.error('Error validating token:', error);
                    localStorage.removeItem('jwt');
                    setIsAuthenticated(false);
                } finally {
                    setLoading(false);
                }
            };
            validateToken();
        } else {
            setLoading(false);
        }
    }, []);

    if (loading) {
        return <div className="loading">Loading...</div>;
    }

    /**
     * Handles user login by setting the authentication state to true.
     */
    const handleLogin = () => {
        setIsAuthenticated(true);
    };

    /**
     * Handles user logout by removing the JWT token and resetting the authentication state.
     */
    const handleLogout = () => {
        localStorage.removeItem('jwt');
        setIsAuthenticated(false);
        setSelectedChatId(null);
    };

    /**
     * Handles chat selection by setting the selected chat ID.
     *
     * @param {string} chatId - The ID of the selected chat.
     */
    const handleSelectChat = (chatId) => {
        setSelectedChatId(chatId);
    };

    return (
        <WebSocketProvider>
            <div className="App">
                <header className="page-header">
                    <h1>Web Chat</h1>
                    {isAuthenticated && (
                        <button className="logout-button" onClick={handleLogout}>Logout</button>
                    )}
                </header>
                {!isAuthenticated
                    ? (<Login onLogin={handleLogin}/>)
                    : (
                        <div className="chat-container">
                            <div className="chat-list-container">
                                <ChatList
                                    onSelectChat={handleSelectChat}
                                    selectedChatId={selectedChatId}
                                />
                            </div>
                            <div className="chat-window-container">
                                {selectedChatId
                                    ? <ChatWindow selectedChatId={selectedChatId}/>
                                    : <div className="chat-placeholder">Please select a chat to start messaging.</div>
                                }
                            </div>
                        </div>
                    )
                }
            </div>
        </WebSocketProvider>
    );
}

export default App;