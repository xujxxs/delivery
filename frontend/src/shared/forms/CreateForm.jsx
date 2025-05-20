import React, { useState } from "react";

import CourierForm from "features/courier/ui/CourierForm"
import OrderForm from "features/order/ui/OrderForm"
import useCreate from "hooks/useCreate";

import "shared/styles/form/CeUpForm.css";
import "shared/styles/form/MainForm.css";

const CreateForm = ({ typeObj, onCancel }) => {
    const [formData, setFormData] = useState({});
    const { createRequest, loading } = useCreate();

    const handleChange = (data) => {
        setFormData((prev) => ({
            ...prev,
            ...data,
        }));
    };
    
    const handleCreate = async () => {
        await createRequest(typeObj, formData);
        onCancel();
    };

    return (
        <div className="form-overlay">
            <div className="form create">
                <h3>Создание {typeObj === "courier" ? "курьера" : "заказа"}</h3>
                {typeObj === 'courier' ? (
                    <CourierForm formData={formData} onChange={handleChange} />
                ) : (
                    <OrderForm formData={formData} onChange={handleChange} />
                )}

                <div className="buttons-controll-form">
                    <button 
                        className="cancel-btn-form simple-btn" 
                        onClick={onCancel}
                        disabled={loading}
                    >
                        Отмена
                    </button>
                    <button 
                        className="crup-btn-form accent-btn"
                        onClick={handleCreate}
                        disabled={loading}
                    >
                        Создать
                    </button>
                </div>
            </div>
        </div>
    );
}

export default CreateForm;