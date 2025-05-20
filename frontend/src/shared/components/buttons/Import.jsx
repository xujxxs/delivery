import React, { useRef } from "react";
import useImport from "hooks/useImport";
import "shared/styles/buttons/ImportBtn.css"

const ImportBtn = ({ type, refreshPage }) => {
    const importFile = useImport();
    const inputRef = useRef(null);
  
    const handleOnClick = () => {
        if (inputRef.current) {
            inputRef.current.click();
        }
    };
  
    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);
        importFile(type, formData)
        .then(() => {            
            refreshPage();
            console.log("File uploaded successfully");
        })
        .catch((error) => {
            console.error("File upload failed:", error);
        });
        event.target.value = null;
    };

    return (
        <>
            <button className="import-btn simple-btn" onClick={handleOnClick}>
                <img src={require('assets/import.png')} alt="import"/>Импорт
            </button>
            <input
                type="file"
                accept="application/json"
                ref={inputRef}
                style={{ display: 'none' }}
                onChange={handleFileChange}
            />
        </>
    );
}

export default ImportBtn;