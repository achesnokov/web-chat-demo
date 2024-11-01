import React, { createContext, useContext, useState, useEffect } from 'react';

const WebSocketContext = createContext();

/**
 * WebSocketProvider component to provide WebSocket host context to its children.
 *
 * @param {object} props - The properties object.
 * @param {React.ReactNode} props.children - The child components that will consume the WebSocket context.
 */
export const WebSocketProvider = ({ children }) => {
    const [websocketHost, setWebsocketHost] = useState(null);

    useEffect(() => {
        /**
         * Fetches the WebSocket host from the server configuration.
         */
        const fetchWebSocketHost = async () => {
            try {
                const response = await fetch('/api/config');
                const data = await response.json();
                setWebsocketHost(data.websocketHost);
            } catch (error) {
                console.error('Error of getting websocket host: ', error);
            }
        };

        fetchWebSocketHost();
    }, []);

    return (
        <WebSocketContext.Provider value={websocketHost}>
            {children}
        </WebSocketContext.Provider>
    );
};

/**
 * Custom hook to use the WebSocket context.
 *
 * @returns {string|null} The WebSocket host.
 */
export const useWebSocket = () => {
    return useContext(WebSocketContext);
};