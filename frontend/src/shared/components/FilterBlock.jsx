import React, { useState, useRef, useEffect } from "react";
import "shared/styles/FilterBlock.css"

const FilterBlock = ({ title, content }) => {
    const [isActive, setIsActive] = useState(true);
    const [maxHeight, setMaxHeight] = useState(0);
    const moreInfoRef = useRef(null);

    const toggleMoreInfo = () => {
        setIsActive(!isActive);
    };

    useEffect(() => {
        if (moreInfoRef.current) {
            setMaxHeight(moreInfoRef.current.scrollHeight);
        }
    }, [isActive]);
        
    return (
        <div className="filter-block">
            <div className="title-filter-block">
                <button className={`filter-more-info-btn ${isActive ? 'active' : ''}`} onClick={toggleMoreInfo} />
                {title}
            </div>
            <div 
                ref={moreInfoRef}
                className={`filter-more-info ${isActive ? 'active' : ''}`} 
                style={{
                    maxHeight: isActive ? `${maxHeight}px` : '0'
                }}
            >
                {content}
            </div>
        </div>
    );
}

export default FilterBlock;