import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { addNotification } from 'service/notificationService';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const usePage = (type, sortBy, pageNumber, filters) => {
    const [objs, setObjs] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);

    const fetchPage = useCallback(async () => {
        setLoading(true);
        try {
            const params = `${sortBy ? "sortBy=" + sortBy : ""}${pageNumber ? "&pageNumber=" + pageNumber : ""}${filters ? "" : ""}`
            const response = await axios.get(`${apiUrl}/${type}?${params}`);
            setObjs(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (err) {
            addNotification(`Status:${err.status} Произошла ошибка при получении страницы`, "error-notification");
            console.log(err);
        } finally {
            setLoading(false);
        }
    }, [type, sortBy, pageNumber, filters]);

    useEffect(() => {
        fetchPage();
    }, [fetchPage]);

    return { objs, totalPages, loading, refreshPage: fetchPage };
};

export default usePage;
