import { useState } from "react";
import axios from "axios";
import { addNotification } from 'service/notificationService';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const useCreate = () => {
    const [loading, setLoading] = useState(false);
    const [responseData, setResponseData] = useState(null);

    const createRequest = async (typeObj, data) => {
        setLoading(true);
        try {
            if(typeObj === "courier") {
                data.cost = data.cost/3600;
            }
            const response = await axios.post(`${apiUrl}/${typeObj}`, data);
            setResponseData(response.data);
            addNotification(`Status:${response.status} Объект успешно создан`, "success-notification");
        } catch (err) {
            addNotification(`Status:${err.status} Произошла ошибка при создании`, "error-notification");
            console.log(err);
        } finally {
            setLoading(false);
        }
    };

    return { createRequest, loading, responseData };
};

export default useCreate;
