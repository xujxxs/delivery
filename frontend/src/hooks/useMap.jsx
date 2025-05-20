import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const apiUrl = process.env.REACT_APP_BACKEND_URL + "/api";

const useMap = (filter) => {
    const [mapData, setMapData] = useState({
        couriers: [],
        orders: [],
        schedules: []
    });

    const fetchMap = useCallback(async () => {
        try {
            const params = {
                left_up_x: filter.topLeft.x,
                left_up_y: filter.topLeft.y,
                right_bottom_x: filter.bottomRight.x,
                right_bottom_y: filter.bottomRight.y,
            };
            const response = await axios.get(`${apiUrl}/map`, { params });
            setMapData(response.data);
        } catch (err) {
            console.log(err);
        }
    }, [filter]);

    useEffect(() => {
        fetchMap();
    }, [fetchMap]);

    return mapData;
};

export default useMap;
