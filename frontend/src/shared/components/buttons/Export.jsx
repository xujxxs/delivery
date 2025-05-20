import React from "react";
import useExport from "hooks/useExport";
import "shared/styles/buttons/ExportBtn.css"

const ExportBtn = ({ type, subClass }) => {
    const exportFile = useExport();
    
    const handleOnClick = () => {
        exportFile(type);
    };

    return (
        <button className={`export-btn simple-btn ${subClass}`} onClick={handleOnClick}>
            <img src={require('assets/export.png')} alt="export"/>Экспорт
        </button>
    );
}

export default ExportBtn;