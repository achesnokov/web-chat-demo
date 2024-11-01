import React, { useState, useEffect, useRef } from 'react';
import { useWebSocket } from './WebSocketContext';
import '../styles/ChatWindow.css';

/**
 * ChatWindow component for displaying and interacting with a chat.
 *
 * @param {string} selectedChatId - The ID of the selected chat.
 */
function ChatWindow({ selectedChatId }) {
    const websocketHost = useWebSocket();

    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const socketRef = useRef(null);
    const messagesEndRef = useRef(null);
    const [isConnected, setIsConnected] = useState(false);

    // Auto-scroll to the latest message
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (selectedChatId && websocketHost) {
            if (socketRef.current) {
                console.log(`[WS] Closing connection for chat ${selectedChatId}`);
                socketRef.current.close();
                socketRef.current = null;
            }

            setMessages([]);
            const token = localStorage.getItem('jwt');
            const currentUser = localStorage.getItem('username');

            if (!token) {
                console.error('No JWT token found');
                setMessages(prev => [...prev, {
                    content: "Authentication error: no token found",
                    type: "error"
                }]);
                return;
            }

            // Create WebSocket URL
            socketRef.current = socketRef.current =
                new WebSocket(`${websocketHost}/chat/ws/${selectedChatId}?token=${encodeURIComponent(token)}`);

            // Error handler
            socketRef.current.onerror = (error) => {
                console.error('WebSocket error details:', error);
                console.error('WebSocket readyState:', socketRef.current.readyState);
                setMessages(prev => [...prev, {
                    content: `Error connecting to chat. Error details: ${error.type}`,
                    type: "error"
                }]);
                setIsConnected(false);
            };

            // Connection opened handler
            socketRef.current.onopen = () => {
                console.log(`[WS] Connected to chat ${selectedChatId}`);
                setIsConnected(true);
            };

            // Message handler
            socketRef.current.onmessage = (event) => {
                const data = event.data;
                try {
                    const messageData = JSON.parse(data);

                    switch(messageData.type) {
                        case 'message':
                            setMessages(prev => [...prev, {
                                username: messageData.username,
                                content: messageData.content,
                                timestamp: messageData.timestamp,
                                type: "message",
                                isOwnMessage: messageData.username === currentUser
                            }]);
                            break;
                        case 'system':
                            setMessages(prev => {
                                const withoutSystem = prev.filter(msg => msg.type !== 'system');
                                return [...withoutSystem, {
                                    content: messageData.content,
                                    timestamp: messageData.timestamp,
                                    type: "system"
                                }];
                            });
                            break;
                        case 'error':
                            setMessages(prev => [...prev, {
                                content: messageData.content,
                                timestamp: messageData.timestamp,
                                type: "error"
                            }]);
                            break;
                        default:
                            console.warn("Received unknown message type:", messageData.type);
                    }
                } catch (e) {
                    console.warn("Received non-JSON message:", data);
                    setMessages(prev => [...prev, {
                        content: data,
                        type: "message"
                    }]);
                }
            };

            // Connection closed handler
            socketRef.current.onclose = (event) => {
                console.log('WebSocket disconnected. Code:', event.code, 'Reason:', event.reason, 'Clean:', event.wasClean);
                setIsConnected(false);
                setMessages(prev => [...prev, {
                    content: `Disconnected from chat (Code: ${event.code}). ${event.reason || 'No reason provided'}. Attempting to reconnect...`,
                    type: "system"
                }]);

                // Attempt to reconnect after 5 seconds
                setTimeout(() => {
                    if (socketRef.current === null && selectedChatId) {
                        console.log('Attempting to reconnect...');
                        const token = localStorage.getItem('jwt');

                        socketRef.current = new WebSocket(`${websocketHost}/chat/ws/${selectedChatId}?token=${encodeURIComponent(token)}`);

                    }
                }, 5000);
            };

            // Cleanup function
            return () => {
                if (socketRef.current) {
                    console.log('Closing WebSocket connection...');
                    socketRef.current.close();
                    socketRef.current = null;
                }
            };
        }
    }, [selectedChatId, websocketHost]);

    // Effect for auto-scrolling
    useEffect(() => {
        scrollToBottom();
    }, [messages]);


    /**
     * Handles sending a message.
     */
    const handleSendMessage = () => {
        if (!socketRef.current || socketRef.current.readyState !== WebSocket.OPEN) {
            setMessages(prev => [...prev, {
                content: "Cannot send message: Connection is not open",
                type: "error"
            }]);
            return;
        }

        const trimmedMessage = newMessage.trim();
        if (trimmedMessage === '') {
            return;
        }

        try {
            socketRef.current.send(trimmedMessage);
/*
            setMessages(prev => [...prev, {
                username: localStorage.getItem('username'),
                content: trimmedMessage,
                timestamp: new Date().toISOString(),
                type: "message",
                isOwnMessage: true
            }]);
*/
            setNewMessage('');
        } catch (error) {
            console.error('Failed to send message:', error);
            setMessages(prev => [...prev, {
                content: "Failed to send message. Please try again.",
                type: "error"
            }]);
        }
    };

    return(
        <div className="chat-window">
            <div className="chat-window-inner">
            <div className="messages">
                <div className="messages-inner">
                    {messages.map((message, index) => (
                        <div key={index} className={`message ${message.type} ${message.isOwnMessage ? 'own-message' : ''}`}>
                            {message.type === 'message' ? (
                                <>
                                    <div className="username">{message.username}</div>
                                    <div className="message-content">
                                        <div className="message-text">{message.content}</div>
                                        {message.timestamp && (
                                            <span className="timestamp">
                                        {new Date(message.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                    </span>
                                        )}
                                    </div>
                                </>
                            ) : (
                                <div className="message-content">{message.content}</div>
                            )}
                        </div>
                    ))}
                    <div ref={messagesEndRef} />
                </div>
            </div>
            </div>
            <div className="input-area">
                <div className="input-area-inner">
                    <input
                        type="text"
                        placeholder="Type a message"
                        value={newMessage}
                        onChange={(e) => setNewMessage(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSendMessage();
                            }
                        }}
                    />
                    <button onClick={handleSendMessage}>Send</button>
                </div>
            </div>
        </div>
    );


}

export default ChatWindow;