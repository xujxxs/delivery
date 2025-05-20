import React from "react";

import "shared/styles/selectors/SelectorWithTitle.css"
import "shared/styles/selectors/PageSelector.css";

const SelectorPage = ({ optionList, value, onPageChange }) => {
    const handleChange = (e) => {
        onPageChange(e.target.value);
    };

    return (
        <div className={`selector-with-title page-selector`}>
            <p>Страница</p>
            <select 
                onChange={handleChange}
                value={value}
            >
                {optionList.map((el) => (
                    <option key={el} value={el}>
                        {el}
                    </option>
                ))}
            </select>
        </div>
    );
}

export default SelectorPage;