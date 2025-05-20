import { useState } from "react";
import axios from "axios";
import { addNotification } from 'service/notificationService';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const useUpdate = () => {
    const [loading, setLoading] = useState(false);
    const [responseData, setResponseData] = useState(null);

    const updateRequest = async (idObj, typeObj, data) => {
        setLoading(true);
        try {
            const response = await axios.put(`${apiUrl}/${typeObj}/${idObj}`, data);
            setResponseData(response.data);
            addNotification(`Status:${response.status} Объект успешно обновлён`, "success-notification");
        } catch (err) {
            addNotification(`Status:${err.status} Произошла ошибка при создании`, "error-notification");
            console.log(err);
        } finally {
            setLoading(false);
        }
    };

    return { updateRequest, loading, responseData };
};

export default useUpdate;
