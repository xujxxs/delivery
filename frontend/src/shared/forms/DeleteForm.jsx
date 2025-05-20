import React from "react";

import useDelete from "hooks/useDelete";

import "shared/styles/form/DeleteForm.css";
import "shared/styles/form/MainForm.css";

const DeleteForm = ({ typeObj, idObj, onCancel }) => {
    const { deleteRequest, loading } = useDelete();
    
    const handleOnDelete = () => {
        const deleteObj = async () => {
            await deleteRequest(typeObj, idObj);
            onCancel();
        };

        deleteObj();
    }
    return (
        <div className="form-overlay">
            <div className="form delete">
                <h3>Удаление {typeObj === "courier" ? "курьера" : "заказа"} №{idObj}</h3>
                <p className="delete-description">Вы уверены что хотите удалить {typeObj === "courier" ? "курьера" : "заказ"} №{idObj}?</p>
                <div className="buttons-controll-form">
                    <button 
                        className="cancel-btn-form simple-btn" 
                        onClick={onCancel}
                        disabled={loading}
                    >
                        Отмена
                    </button>
                    <button 
                        className="delete-btn-form" 
                        onClick={handleOnDelete}
                        disabled={loading}
                    >
                        Удалить
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DeleteForm;