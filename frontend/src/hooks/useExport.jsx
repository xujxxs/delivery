import axios from 'axios';
import { addNotification } from 'service/notificationService';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const useExport = () => {
    const exportFile = async (typeObj) => {
        try {
            const response = await axios.get(`${apiUrl}/${typeObj}/export`, {
                responseType: 'blob',
            });

            const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/json' }));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${typeObj}s.json`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (err) {
            addNotification(`Status:${err.status} Произошла ошибка при экспорте`, "error-notification");
            console.log(err);
        }
    };

    return exportFile;
};

export default useExport;
