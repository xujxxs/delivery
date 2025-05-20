import { EventEmitter } from 'events';

const notificationEmitter = new EventEmitter();
export default notificationEmitter;

export const addNotification = (message, status) => {
  notificationEmitter.emit('notify', { message, status });
};