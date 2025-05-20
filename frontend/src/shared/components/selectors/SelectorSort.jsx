import React, { useEffect, useRef } from "react";

import "shared/styles/selectors/SelectorWithTitle.css";
import "shared/styles/selectors/SortSelector.css";

const SelectorSort = ({ optionMap, value, onSortChange }) => {
    const pRef = useRef(null);
    const selectRef = useRef(null);

    useEffect(() => {
        if (pRef.current && selectRef.current) {
            const pWidth = pRef.current.getBoundingClientRect().width;
            selectRef.current.style.width = `${pWidth}px`;
        }
    }, [optionMap]);

    const handleChange = (e) => {
        onSortChange(e.target.value);
    };

    return (
        <div className="selector-with-title sort-selector">
            <p ref={pRef}>Сортировать по</p>
            <select 
                ref={selectRef} 
                value={value}
                onChange={handleChange}
            >
                {Object.entries(optionMap).map(([key, el]) => (
                    <option key={key} value={key}>
                        {el}
                    </option>
                ))}
            </select>
        </div>
    );
};

export default SelectorSort;
