import React, {useEffect, useState} from 'react';
import '../styles/ChatList.css';
import IconButton from '@mui/material/IconButton';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import Dialog from '@mui/material/Dialog';

/**
 * Array of colors used for avatars.
 * Each color is represented as a hex string.
 * These colors are used to visually distinguish different avatars.
 */
const AVATAR_COLORS = [
    '#ff7675', '#74b9ff', '#a29bfe', '#81ecec',
    '#ffeaa7', '#fab1a0', '#fd79a8', '#00b894',
    '#6c5ce7', '#fdcb6e', '#e17055', '#00cec9'
];

/**
 * Generates a color for an avatar based on the input text.
 * The color is determined by hashing the text and mapping it to a predefined array of colors.
 *
 * @param {string} text - The input text used to generate the avatar color.
 * @returns {string} - The hex color code for the avatar.
 */
function getAvatarColor(text) {
    let hash = 0;
    for (let i = 0; i < text.length; i++) {
        hash = text.charCodeAt(i) + ((hash << 5) - hash);
    }
    return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
}

/**
 * Extracts the first letters of each word in a caption and returns them as a string.
 * The resulting string is converted to uppercase and limited to 2 characters.
 *
 * @param {string} caption - The caption from which to extract the avatar letters.
 * @returns {string} - The uppercase initials of the caption.
 */
function getAvatarLetters(caption) {
    return caption
        .split(' ')
        .map(word => word[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);
}


/**
 * ChatList component that displays a list of chats and allows various chat-related actions.
 *
 * @param {function} onSelectChat - Callback function to handle chat selection.
 * @param {string} selectedChatId - The ID of the currently selected chat.
 */
function ChatList({ onSelectChat, selectedChatId  }) {
    const [chats, setChats] = useState([]);

    const [openCopyDialog, setOpenCopyDialog] = useState(false);
    const [selectedChatUrl, setSelectedChatUrl] = useState('');

    const [joinDialogOpen, setJoinDialogOpen] = useState(false);
    const [joinChatId, setJoinChatId] = useState('');
    const [joinChatTitle, setJoinChatTitle] = useState('');

    const [openLeaveDialog, setOpenLeaveDialog] = useState(false);
    const [chatToLeave, setChatToLeave] = useState(null);

    const [openCreateDialog, setOpenCreateDialog] = useState(false);
    const [newChatCaption, setNewChatCaption] = useState('');

    /**
     * useEffect hook to fetch the list of chats from the server and handle joining a chat if the URL hash contains a chat ID.
     * Fetches chat details and opens the join dialog if successful.
     * Logs an error message if the fetch fails.
     */
    useEffect(() => {

        /**
         * Fetches the list of chats from the server and updates the state.
         * If the response is not ok, logs an error message.
         */
        const fetchChats = async () => {
            const response = await fetch('/api/chats');
            if (response.ok) {
                const data = await response.json();
                setChats(data);
            } else {
                console.error('Failed to fetch chats');
            }
        };
        fetchChats();

        /**
         * Handles joining a chat if the URL hash contains a chat ID.
         * Fetches chat details and opens the join dialog if successful.
         * Logs an error message if the fetch fails.
         */
        const handleJoinChat = async () => {
            if (window.location.hash.startsWith('#join-chat=')) {
                const chatId = window.location.hash.split('=')[1];
                if (chatId) {
                    try {
                        const response = await fetch(`/api/chats/${chatId}`);
                        if (response.ok) {
                            const chatData = await response.json();
                            setJoinChatTitle(chatData.caption);
                            setJoinChatId(chatId);
                            setJoinDialogOpen(true);
                        } else {
                            console.error('Failed to fetch chat details');
                        }
                    } catch (error) {
                        console.error('Error while fetching chat from hash:', error);
                    }
                }
            }
        };

        handleJoinChat();
    }, []);



    /**
     * Confirms the creation of a new chat.
     * Sends a POST request to the server with the new chat caption.
     * If the request is successful, updates the state with the new chat.
     * Logs an error message if the request fails.
     * Resets the new chat caption and closes the create dialog.
     */
    const confirmCreateChat = async () => {
        if (newChatCaption) {
            const response = await fetch('/api/chats', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ caption: newChatCaption }),
            });
            if (response.ok) {
                const newChat = await response.json();
                setChats([...chats, newChat]);
            } else {
                console.error('Failed to create chat');
            }
            setOpenCreateDialog(false);
            setNewChatCaption('');
        }
    };


    /**
     * Opens the dialog for creating a new chat.
     * Sets the state to open the create chat dialog.
     */
    const handleCreateChat = () => {
        setOpenCreateDialog(true);
    };

/*
    const handleCopyToClipboard = () => {
        navigator.clipboard.writeText(selectedChatUrl)
            .then(() => {
                console.log('Chat URL copied to clipboard');
                setOpenCopyDialog(false);
            })
            .catch(err => {
                console.error('Failed to copy chat URL: ', err);
            });
    };
*/


    function fallbackCopyTextToClipboard(text) {
        const textArea = document.createElement("textarea");
        textArea.value = text;

        textArea.style.top = "0";
        textArea.style.left = "0";
        textArea.style.position = "fixed";

        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        try {
            const successful = document.execCommand('copy');
            const msg = successful ? 'successful' : 'unsuccessful';
            console.log('Fallback: Copying text command was ' + msg);
        }
        finally {
            document.body.removeChild(textArea);
        }

    }

    /**
     * Copies the selected chat URL to the clipboard.
     * Uses the Clipboard API if available, otherwise falls back to a legacy method.
     * Logs success or error messages to the console.
     */
    const handleCopyToClipboard = async () => {
//        const isSecureContext = window.isSecureContext || window.location.hostname === 'localhost';

        if (navigator.clipboard) {
            try {
                await navigator.clipboard.writeText(selectedChatUrl);
                console.log('Chat URL copied to clipboard');
                setOpenCopyDialog(false);
            } catch (err) {
                console.error('Failed to copy chat URL: ', err);
            }
        } else {
            // Fallback for cases where the clipboard API is not available. Should not be used due to legacy API and security issues
            try {
                fallbackCopyTextToClipboard(selectedChatUrl)
                setOpenCopyDialog(false);
            } catch (err) {
                console.error('Fallback: Oops, unable to copy', err);
            }
        }
    };

    /**
     * Generates the chat URL for the given chat ID and sets it as the selected chat URL.
     * Opens the dialog to copy the chat URL.
     *
     * @param {string} chatId - The ID of the chat for which to generate the URL.
     */
    const handleCopyChatUrl = (chatId) => {
        const chatUrl = `${window.location.origin}/#join-chat=${chatId}`;
        setSelectedChatUrl(chatUrl);
        setOpenCopyDialog(true);
    };


    /**
     * Joins the chat with the given chat ID.
     * Sends a POST request to the server to join the chat.
     * If the request is successful, updates the state with the joined chat.
     * Logs an error message if the request fails.
     * Closes the join dialog and resets the URL hash.
     */
    const handleJoinChat = async () => {
        if (joinChatId) {
            try {
                const response = await fetch(`/api/chats/${joinChatId}/participants`, {
                    method: 'POST',
                });

                if(!response.ok) {
                    console.error('Failed to join chat:', response.statusText);
                }

                if(response.status === 204) {
                    console.log('Already joined the chat:', joinChatId);
                    onSelectChat(joinChatId);
                    return;
                }

                const chat = await response.json();
                setChats([...chats, chat]);
                onSelectChat(joinChatId);
                console.log('Successfully joined the chat:', joinChatId);

            } catch (error) {
                console.error('Error while joining chat:', error);
            } finally {
                setJoinDialogOpen(false);
                window.history.replaceState({}, document.title, "/");
            }
        }
    };

    /**
     * Cancels the join chat operation.
     * Closes the join dialog and resets the URL hash.
     */
    const handleCancelJoinChat = () => {
        setJoinDialogOpen(false);
        window.history.replaceState({}, document.title, "/");
    };

    /**
     * Initiates the process to leave a chat.
     * Sets the chat ID of the chat to be left.
     *
     * @param {string} chatId - The ID of the chat to leave.
     */
    const handleLeaveChat = (chatId) => {
        setChatToLeave(chatId);
        setOpenLeaveDialog(true);
    };


    /**
     * Confirms the action to leave a chat.
     * Sends a DELETE request to the server to remove the user from the chat.
     * If the request is successful, updates the state to remove the chat from the list.
     * Logs an error message if the request fails.
     * Closes the leave dialog and resets the chat to leave state.
     */
    const confirmLeaveChat = async () => {
        if (chatToLeave) {
            try {
                const response = await fetch(`/api/chats/${chatToLeave}/participants`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    setChats(chats.filter(chat => chat.chatId !== chatToLeave));
                    console.log('Successfully left the chat');
                } else {
                    console.error('Failed to leave chat');
                }
            } catch (error) {
                console.error('Error while leaving the chat:', error);
            }
            setOpenLeaveDialog(false);
            setChatToLeave(null);
        }
    };

    return (
        <div className="chat-list-content">
            <div className="chat-list-header">
                <h2>Chats</h2>
                <IconButton onClick={handleCreateChat} className="create-chat-icon">
                    <AddCircleIcon />
                </IconButton>
            </div>
            <div className="chat-list-wrapper">
                <ul className="chat-list">
                    {chats.map(chat => (
                        <li
                            key={chat.chatId}
                            className={`chat-item ${selectedChatId === chat.chatId ? 'selected' : ''}`}
                            onClick={() => onSelectChat(chat.chatId)}
                        >
                            <div
                                className="chat-avatar"
                                style={{ backgroundColor: getAvatarColor(chat.caption) }}
                            >
                                {getAvatarLetters(chat.caption)}
                            </div>
                            <span className="chat-caption" title={chat.caption}>{chat.caption}</span>
                            <div className="chat-actions">
                                <IconButton
                                    className="chat-action-button"
                                    size="small"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleLeaveChat(chat.chatId);
                                    }}
                                >
                                    <ExitToAppIcon fontSize="small" />
                                </IconButton>
                                <IconButton
                                    className="chat-action-button"
                                    size="small"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleCopyChatUrl(chat.chatId);
                                    }}
                                >
                                    <ContentCopyIcon fontSize="small" />
                                </IconButton>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>


            <Dialog open={openCopyDialog} onClose={() => setOpenCopyDialog(false)}>
                <div className="dialog-content">
                    <h2 className="dialog-title">Copy Chat URL</h2>
                    <p className="dialog-text">
                        You can copy the chat URL below to share it with others.
                    </p>
                    <div className="dialog-input-container">
                        <input
                            className="dialog-input"
                            type="text"
                            value={selectedChatUrl}
                            readOnly
                        />
                    </div>
                    <div className="dialog-actions">
                        <button
                            className="dialog-button dialog-button-cancel"
                            onClick={() => setOpenCopyDialog(false)}
                        >
                            Cancel
                        </button>
                        <button
                            className="dialog-button dialog-button-action"
                            onClick={handleCopyToClipboard}
                        >
                            Copy to Clipboard
                        </button>
                    </div>
                </div>
            </Dialog>

            <Dialog open={joinDialogOpen} onClose={() => setJoinDialogOpen(false)}>
                <div className="dialog-content">
                    <h2 className="dialog-title">Join Chat</h2>
                    <p className="dialog-text">
                        Would you like to join the chat titled "{joinChatTitle}"?
                    </p>
                    <div className="dialog-actions">
                        <button
                            className="dialog-button dialog-button-cancel"
                            onClick={handleCancelJoinChat}
                        >
                            Cancel
                        </button>
                        <button
                            className="dialog-button dialog-button-action"
                            onClick={handleJoinChat}
                        >
                            Join Chat
                        </button>
                    </div>
                </div>
            </Dialog>

            <Dialog open={openLeaveDialog} onClose={() => setOpenLeaveDialog(false)}>
                <div className="dialog-content">
                    <h2 className="dialog-title">Leave Chat</h2>
                    <p className="dialog-text">
                        Are you sure you want to leave this chat?
                    </p>
                    <div className="dialog-actions">
                        <button
                            className="dialog-button dialog-button-cancel"
                            onClick={() => setOpenLeaveDialog(false)}
                        >
                            Cancel
                        </button>
                        <button
                            className="dialog-button dialog-button-warning"
                            onClick={confirmLeaveChat}
                        >
                            Leave Chat
                        </button>
                    </div>
                </div>
            </Dialog>

            <Dialog open={openCreateDialog} onClose={() => setOpenCreateDialog(false)}>
                <div className="dialog-content">
                    <h2 className="dialog-title">Create New Chat</h2>
                    <p className="dialog-text">
                        Please enter the title for the new chat.
                    </p>
                    <input
                        className="dialog-input"
                        type="text"
                        placeholder="Chat Title"
                        value={newChatCaption}
                        onChange={(e) => setNewChatCaption(e.target.value)}
                    />
                    <div className="dialog-actions">
                        <button
                            className="dialog-button dialog-button-cancel"
                            onClick={() => setOpenCreateDialog(false)}
                        >
                            Cancel
                        </button>
                        <button
                            className="dialog-button dialog-button-action"
                            onClick={confirmCreateChat}
                        >
                            Create Chat
                        </button>
                    </div>
                </div>
            </Dialog>
        </div>
    );
}

export default ChatList;
