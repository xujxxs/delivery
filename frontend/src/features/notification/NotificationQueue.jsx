import React, { useState, useEffect } from 'react';
import notificationEmitter from 'service/notificationService';
import './ui/NotificationQueue.css';

let nextId = 0;

const NotificationQueue = () => {
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        const handler = ({ message, status }) => {
            const id = nextId++;
            setNotifications(prev => [...prev, { id, message, status }]);
            setTimeout(() => {
                setNotifications(prev => prev.filter(n => n.id !== id));
            }, 3000);
        };

        notificationEmitter.on('notify', handler);
        return () => {
            notificationEmitter.off('notify', handler);
        };
    }, []);

    return (
        <div className="notification-container">
            {notifications.map(n => (
                <div key={n.id} className={`notification ${n.status}`}>
                    {n.message}
                </div>
            ))}
        </div>
    );
};

export default NotificationQueue;