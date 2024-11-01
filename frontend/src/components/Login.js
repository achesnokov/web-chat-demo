import React, { useState } from 'react';
import '../styles/Login.css';

/**
 * Login component for handling user authentication.
 *
 * @param {function} onLogin - Callback function to be called after a successful login.
 */
function Login({ onLogin }) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [isRegistering, setIsRegistering] = useState(false);

    /**
     * Handles the login process.
     * Sends a POST request to the server with the username and password.
     * If successful, stores the JWT token and username in local storage and calls the onLogin callback.
     * If unsuccessful, sets an error message.
     */
    const handleLogin = async () => {
        if (!username || !password) {
            setError("Please fill in both fields.");
            return;
        }

        setLoading(true);
        setError("");
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwt', data.token);
                localStorage.setItem('username', username);
                onLogin();
            } else {
                setError("Invalid username or password.");
            }
        } catch (e) {
            setError("An error occurred. Please try again later.");
        } finally {
            setLoading(false);
        }
    };

    /**
     * Handles the registration process.
     * Sends a POST request to the server with the username and password.
     * If successful, sets a success message and switches to the login view.
     * If unsuccessful, sets an error message.
     */
    const handleRegister = async () => {
        if (!username || !password) {
            setError("Please fill in both fields.");
            return;
        }

        setLoading(true);
        setError("");
        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (response.ok) {
                setError("Registration successful! Please log in.");
                setIsRegistering(false);
            } else {
                setError("Registration failed. Please try again.");
            }
        } catch (e) {
            setError("An error occurred. Please try again later.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <h2>{isRegistering ? "Register" : "Login"}</h2>
                {error && <div className="error-message">{error}</div>}
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                {isRegistering ? (
                    <button onClick={handleRegister} disabled={loading}>
                        {loading ? "Registering..." : "Register"}
                    </button>
                ) : (
                    <button onClick={handleLogin} disabled={loading}>
                        {loading ? "Logging in..." : "Login"}
                    </button>
                )}
                <div className="toggle-register">
                    {isRegistering ? (
                        <p>
                            Already have an account? <span onClick={() => setIsRegistering(false)}>Login here</span>
                        </p>
                    ) : (
                        <p>
                            Don't have an account? <span onClick={() => setIsRegistering(true)}>Register here</span>
                        </p>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Login;