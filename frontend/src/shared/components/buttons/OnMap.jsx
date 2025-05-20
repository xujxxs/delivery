import React, { useContext } from "react";
import { MapContext } from 'App';

import "shared/styles/buttons/OnMapBtn.css"

const OnMapBtn = ({ to_x, to_y }) => {
    const mapRef = useContext(MapContext);

    const handleOnMap = () => {
        mapRef.current.focusOn({ x: to_x, y: to_y });
    }

    return (
        <button className="find-on-map" onClick={handleOnMap}></button>
    );
}

export default OnMapBtn;