import React from "react";
import "shared/styles/buttons/FilterBtn.css"

const FilterBtn = ({handleOnClick}) => {
    return ( 
        <button className="filter-btn accent-btn" onClick={handleOnClick}>
            Фильтры
        </button>
    );
}

export default FilterBtn;