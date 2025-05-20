import React, { useState } from "react";

import CourierForm from "features/courier/ui/CourierForm"
import OrderForm from "features/order/ui/OrderForm"
import useUpdate from "hooks/useUpdate";

import "shared/styles/form/CeUpForm.css";
import "shared/styles/form/MainForm.css";

const UpdateForm = ({ typeObj, idObj, onCancel, initialData }) => {
    const [formData, setFormData] = useState(initialData || {});
    const { updateRequest, loading } = useUpdate();

    const handleChange = (data) => {
        setFormData((prev) => ({
            ...prev,
            ...data,
        }));
    };
    
    const handleUpdate = async () => {
        await updateRequest(idObj, typeObj, formData);
        onCancel();
    };

    return (
        <div className="form-overlay">
            <div className="form update">
                <h3>Редактирование {typeObj === "courier" ? "курьера" : "заказа"} №{idObj}</h3>
                {typeObj === 'courier' ? (
                    <CourierForm formData={formData} onChange={handleChange}/> 
                ) : ( 
                    <OrderForm formData={formData} onChange={handleChange} />
                )}

                <div className="buttons-controll-form">
                    <button 
                        className="cancel-btn-form simple-btn" 
                        onClick={onCancel}
                    >
                        Отмена
                    </button>
                    <button 
                        className="crup-btn-form accent-btn" 
                        onClick={handleUpdate}
                        disabled={loading}
                    >
                        Редактировать
                    </button>
                </div>
            </div>
        </div>
    );
}

export default UpdateForm;