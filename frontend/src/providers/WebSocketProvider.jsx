import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const apiUrl = process.env.REACT_APP_BACKEND_URL;

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
    const clientRef = useRef(null);
    const [events, setEvents] = useState({
        courier: null,
        order: null,
        schedule: null,
    });

    useEffect(() => {
        const socket = new SockJS(`${apiUrl}/v3/connect`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            debug: (msg) => console.log('[STOMP]', msg),
        });
        clientRef.current = stompClient;

        stompClient.onConnect = () => {
            console.log('WebSocket connected');

            stompClient.subscribe('/topic/courier', (msg) => {
                setEvents(prev => ({ ...prev, courier: msg.body }));
            });
            stompClient.subscribe('/topic/order', (msg) => {
                setEvents(prev => ({ ...prev, order: msg.body }));
            });
            stompClient.subscribe('/topic/schedules', (msg) => {
                setEvents(prev => ({ ...prev, schedule: msg.body }));
            });
        };

        stompClient.onStompError = (err) => {
            console.error('STOMP error', err);
        };

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, []);

    return (
        <WebSocketContext.Provider value={events}>
            {children}
        </WebSocketContext.Provider>
    );
};

export const useNotifications = () => {
    return useContext(WebSocketContext);
};