import axios from 'axios';
import { addNotification } from 'service/notificationService';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const useImport = () => {
    const importFile = async (typeObj, formData) => {
        try {
            const response = await axios.post(
                `${apiUrl}/${typeObj}/import`,
                formData,
                {
                  headers: { 'Content-Type': 'multipart/form-data' },
                }
            );
            addNotification(`Status:${response.status} Файл успешно импортирован`, "success-notification");
        } catch (err) {
            addNotification(`Status:${err.status} Произошла ошибка при импорте`, "error-notification");
            console.log(err);
            throw err;
        }
    };

    return importFile;
};

export default useImport;
