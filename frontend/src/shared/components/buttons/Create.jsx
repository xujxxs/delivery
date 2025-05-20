import React from "react";
import "shared/styles/buttons/CreateBtn.css"

const CreateBtn = ({nameObj, handleOnClick}) => {
    return ( 
        <button className="create-btn accent-btn" onClick={handleOnClick}>
            Создать {nameObj}
        </button>
    );
}

export default CreateBtn;