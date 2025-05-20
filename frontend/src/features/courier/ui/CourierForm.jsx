import React from "react";

import TagInput from "./TagInput"

const CourierForm = ({ formData, onChange }) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;

        if (name === "position_x" || name === "position_y") {
            onChange({
                position: {
                    ...formData.position,
                    [name === "position_x" ? "x" : "y"]: value,
                },
            });
        } else {
            onChange({ [name]: value });
        }
    };

    const handleBlur = (e, decimals) => {
        const { name, value } = e.target;
        if (value === "" || value === null) return;
        const num = parseFloat(value);
        if (isNaN(num)) return;
        const fixedVal = parseFloat(num.toFixed(decimals));

        if (name === "speed" || name === "cost") {
            onChange({ [name]: fixedVal });
        } else if (name === "position_x" || name === "position_y") {
            onChange({
                position: {
                    ...(formData.position || {}),
                    [name === "position_x" ? "x" : "y"]: fixedVal,
                },
            });
        }
    };

    return ( 
        <div className="courier-form">
            <div className="fio-courier form-block">
                <p className="form-block-title">ФИО Курьера</p>
                <input
                    type="text"
                    name="lastname"
                    placeholder="Введите фамилию"
                    value={formData.lastname || ""}
                    onChange={handleInputChange}
                />
                <input
                    type="text"
                    name="firstname"
                    placeholder="Введите имя"
                    value={formData.firstname || ""}
                    onChange={handleInputChange}
                />
                <input
                    type="text"
                    name="middlename"
                    placeholder="Введите отчество (не обязательно)"
                    value={formData.middlename || ""}
                    onChange={handleInputChange}
                />
            </div>
            <div className="properties-courier form-block">
                <p className="form-block-title">Свойства курьера</p>
                <div className="input-block">
                    <p>Скорость (км/ч)</p>
                    <input
                        type="text"
                        name="speed"
                        placeholder="Введите скорость (км/ч)"
                        value={formData.speed || ""}
                        onChange={handleInputChange}
                        onBlur={(e) => handleBlur(e, 2)}
                    />
                </div>
                <div className="input-block">
                    <p>Грузоподъёмность (грамм)</p>
                    <input
                        type="text"
                        name="loadCapacity"
                        placeholder="Введите грузоподъёмность (грамм)"
                        value={formData.loadCapacity || ""}
                        onChange={handleInputChange}
                    />
                </div>
                <div className="input-block">
                    <p>Стоимость работы (ед.в./ч)</p>
                    <input
                        type="text"
                        name="cost"
                        placeholder="Введите стоимость работы (ед.в./ч)"
                        value={formData.cost ?? ""}
                        onChange={handleInputChange}
                        onBlur={(e) => handleBlur(e, 2)}
                    />
                </div>
                <div className="input-block">
                    <p>Поддерживаемые типы заказов</p>
                     <TagInput
                        supportedTypeOrders={formData.supportedTypeOrders || []}
                        onChange={(newArray) =>
                            onChange({ supportedTypeOrders: newArray })
                        }
                    />
                </div>
            </div>
            <div className="coordinates-courier form-block">
                <p className="form-block-title">Начальная позиция</p>
                <div className="coordinates-form">
                    <div className="coordinates-inputs">
                        <input
                            type="text"
                            name="position_x"
                            placeholder="X: 0.0000"
                            value={formData.position?.x ?? ""}
                            min="0"
                            onChange={handleInputChange}
                            onBlur={(e) => handleBlur(e, 3)}
                        />
                        <input
                            type="text"
                            name="position_y"
                            placeholder="Y: 0.0000"
                            value={formData.position?.y ?? ""}
                            min="0"
                            onChange={handleInputChange}
                            onBlur={(e) => handleBlur(e, 3)}
                        />
                    </div>
                    {/* <button className="select-on-map"></button> | now not work */}
                </div>
            </div>
        </div>
    );
}

export default CourierForm;