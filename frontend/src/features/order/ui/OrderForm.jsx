import React from "react";

const OrderForm = ({ formData, onChange }) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;

        if (name === "positionPickUp_x" || name === "positionPickUp_y") {
            onChange({
                positionPickUp: {
                    ...(formData.positionPickUp || {}),
                    [name === "positionPickUp_x" ? "x" : "y"]: value,
                },
            });
        } else if (name === "positionDelivery_x" || name === "positionDelivery_y") {
            onChange({
                positionDelivery: {
                    ...(formData.positionDelivery || {}),
                    [name === "positionDelivery_x" ? "x" : "y"]: value,
                },
            });
        } else if (name === "typeOrder") {
            onChange({
                typeOrder: {
                    ...formData.typeOrder,
                    type: value,
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

        if (name === "cost") {
            onChange({ [name]: fixedVal });
        } else if (name === "positionPickUp_x" || name === "positionPickUp_y") {
            onChange({
                positionPickUp: {
                    ...(formData.positionPickUp || {}),
                    [name === "positionPickUp_x" ? "x" : "y"]: fixedVal,
                },
            });
        } else if (name === "positionDelivery_x" || name === "positionDelivery_y") {
            onChange({
                positionDelivery: {
                    ...(formData.positionDelivery || {}),
                    [name === "positionDelivery_x" ? "x" : "y"]: fixedVal,
                },
            });
        }
    };

    return ( 
        <div className="order-form">
            <div className="name-order form-block">
            <p>Название заказа</p>
            <input
                type="text"
                name="name"
                placeholder="Название"
                value={formData.name || ""}
                onChange={handleInputChange}
            />
            </div>
            <div className="properties-order form-block">
                <p>Свойства заказа</p>
                <div className="input-block">
                    <p>Цена (в.)</p>
                    <input
                        type="text"
                        name="cost"
                        placeholder="Введите цену"
                        value={formData.cost || ""}
                        onChange={handleInputChange}
                        onBlur={(e) => handleBlur(e, 2)}
                    />
                </div>
                <div className="input-block">
                    <p>Вес (грамм)</p>
                    <input
                        type="text"
                        name="weight"
                        placeholder="Введите вес"
                        value={formData.weight || ""}
                        onChange={handleInputChange}
                    />
                </div>
                <div className="input-block">
                    <p>Тип заказа</p>
                    <input
                        type="text"
                        name="typeOrder"
                        placeholder="Введите тип заказа"
                        value={formData.typeOrder?.type || ""}
                        onChange={handleInputChange}
                    />
                </div>
            </div>
            <div className="form-block order-period">
                <p>Плановый период заказа</p>
                <input
                    type="time"
                    name="openPeriod"
                    value={formData.openPeriod || ""}
                    onChange={handleInputChange}
                />
                <input
                    type="time"
                    name="closePeriod"
                    value={formData.closePeriod || ""}
                    onChange={handleInputChange}
                />
            </div>
            <div className="coordinates-order form-block">
                <p>Стратовая позиция заказа</p>
                <div className="coordinates-form">
                    <div className="coordinates-inputs">
                        <input
                            type="text"
                            name="positionPickUp_x"
                            placeholder="X: 0.0000"
                            value={formData.positionPickUp?.x ?? ""}
                            onChange={handleInputChange}
                            onBlur={(e) => handleBlur(e, 3)}
                        />
                        <input
                            type="text"
                            name="positionPickUp_y"
                            placeholder="Y: 0.0000"
                            value={formData.positionPickUp?.y ?? ""}
                            onChange={handleInputChange}
                            onBlur={(e) => handleBlur(e, 3)}
                        />
                    </div>
                    {/* <button className="select-on-map"></button> | now not work */}
                </div>
            </div>
            <div className="coordinates-order form-block">
                <p>Конечная позиция заказа</p>
                <div className="coordinates-form">
                    <div className="coordinates-inputs">
                        <input
                            type="text"
                            name="positionDelivery_x"
                            placeholder="X: 0.0000"
                            value={formData.positionDelivery?.x ?? ""}
                            onChange={handleInputChange}
                            onBlur={(e) => handleBlur(e, 3)}
                        />
                        <input
                            type="text"
                            name="positionDelivery_y"
                            placeholder="Y: 0.0000"
                            value={formData.positionDelivery?.y ?? ""}
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

export default OrderForm;