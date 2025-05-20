import { useState } from "react";
import axios from "axios";
import { addNotification } from 'service/notificationService';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const useDelete = () => {
    const [loading, setLoading] = useState(false);

    const deleteRequest = async (typeObj, idObj) => {
        setLoading(true);
        try {
            const response = await axios.delete(`${apiUrl}/${typeObj}/${idObj}`);
            addNotification(`Status:${response.status} Объект успешно удалён`, "success-notification");
        } catch (err) {
            addNotification(`Status:${err.status} Произошла ошибка при удалении`, "error-notification");
            console.log(err);
        } finally {
            setLoading(false);
        }
    };

    return { deleteRequest, loading };
};

export default useDelete;