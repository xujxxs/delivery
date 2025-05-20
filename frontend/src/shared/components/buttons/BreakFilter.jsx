import React from "react";
import "shared/styles/buttons/BreakFilterBtn.css"

const BreakFilterBtn = ({ handleOnClick }) => {
    return ( 
        <button 
            className="break-filter-btn simple-btn"
            onClick={handleOnClick}
        >
            Сбросить всё
        </button>
    );
}

export default BreakFilterBtn;