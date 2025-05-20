import React, { useState, useRef, useEffect } from 'react';
import OnMapBtn from "shared/components/buttons/OnMap"
import UpdateForm from 'shared/forms/UpdateForm';
import DeleteForm from 'shared/forms/DeleteForm';

const OrderRow = ({ order }) => {
    const [isActive, setIsActive] = useState(false);
    const [maxHeight, setMaxHeight] = useState(0);
    const [showUpdateForm, setShowUpdateForm] = useState(false);
    const [showDeleteForm, setShowDeleteForm] = useState(false);
    const moreInfoRef = useRef(null);

    const toggleMoreInfo = () => {
        setIsActive(!isActive);
    };

    const handleUpdateClick = () => {
        setShowUpdateForm(true);
    };

    const handleCancelUpdate = () => {
        setShowUpdateForm(false);
    };

    const handleDeleteClick = () => {
        setShowDeleteForm(true);
    };

    const handleCancelDelete = () => {
        setShowDeleteForm(false);
    };

    useEffect(() => {
        if (moreInfoRef.current) {
            setMaxHeight(moreInfoRef.current.scrollHeight);
        }
    }, [isActive]);

    return (
        <div className="all-info" key={order.id} >
            <div className="table-row">
                <div className="id">{order.id}</div>
                <div className="name">{order.name}</div>
                <button className={`more-btn ${isActive ? 'active' : ''}`} onClick={toggleMoreInfo}></button>
            </div>
            <div 
                ref={moreInfoRef}
                className={`more-info ${isActive ? 'active' : ''}`} 
                style={{
                    maxHeight: isActive ? `${maxHeight}px` : '0'
                }}
            >
                <div className="line-list">
                    <div className="line">
                        <div className="name">Цена:</div>
                        <div className="value">{order.cost.toFixed(2)} в.</div>
                    </div>
                    <div className="line">
                        <div className="name">Вес:</div>
                        <div className="value">{order.weight/1000} кг</div>
                    </div>
                    <div className="line">
                        <div className="name">Открытие периода доставки:</div>
                        <div className="value">{(order.openPeriod)}</div>
                    </div>
                    <div className="line">
                        <div className="name">Закрытие периода доставки:</div>
                        <div className="value">{(order.closePeriod)}</div>
                    </div>
                    <div className="line">
                        <div className="name">Тип заказа:</div>
                        <div className="value">{order.typeOrder.type}</div>
                    </div>
                    <div className="line">
                        <div className="name">Начальная позиция:</div>
                        <div className="value">
                            X: {order.positionPickUp.x.toFixed(3)}; Y: {order.positionPickUp.y.toFixed(3)}
                            <OnMapBtn to_x={order.positionPickUp.x} to_y={order.positionPickUp.y} />
                        </div>
                    </div>
                    <div className="line">
                        <div className="name">Конечная позиция:</div>
                        <div className="value">
                            X: {order.positionDelivery.x.toFixed(3)}; Y: {order.positionDelivery.y.toFixed(3)}
                            <OnMapBtn to_x={order.positionDelivery.x} to_y={order.positionDelivery.y} />
                        </div>
                    </div>
                    <div className="line btns-line">
                        <button className="del-btn" onClick={handleDeleteClick}>Удалить</button>
                        <button className="red-btn" onClick={handleUpdateClick}>Редактировать</button>
                    </div>
                </div>
            </div>

            {showUpdateForm && (
                <UpdateForm
                    typeObj="order"
                    idObj={order.id}
                    onCancel={handleCancelUpdate}
                    initialData={order}
                />
            )}

            {showDeleteForm && (
                <DeleteForm
                    typeObj="order"
                    idObj={order.id}
                    onCancel={handleCancelDelete}
                />
            )}
        </div>
    );
}

export default OrderRow;