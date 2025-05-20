import React, { useState, useRef, useEffect } from 'react';
import OnMapBtn from "shared/components/buttons/OnMap"
import UpdateForm from 'shared/forms/UpdateForm';
import DeleteForm from 'shared/forms/DeleteForm';

const CourierRow = ({ courier }) => {
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
        <div className="all-info" key={courier.id} >
            <div className="table-row">
                <div className="id">{courier.id}</div>
                <div className="name">{courier.lastname} {courier.firstname} {courier.surname}</div>
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
                        <div className="name">Скорость:</div>
                        <div className="value">{courier.speed} км/ч</div>
                    </div>
                    <div className="line">
                        <div className="name">Грузоподъёмность:</div>
                        <div className="value">{courier.loadCapacity/1000} кг</div>
                    </div>
                    <div className="line">
                        <div className="name">Стоимость работы:</div>
                        <div className="value">{(courier.cost*3600).toFixed(2)} ед.в./ч</div>
                    </div>
                    <div className="line">
                        <div className="name">Типы заказов:</div>
                        <div className="value">
                            {courier.supportedTypeOrders.map(item => item.type).join(', ')}
                        </div>
                    </div>
                    <div className="line">
                        <div className="name">Начальная позиция:</div>
                        <div className="value">
                            X: {courier.position.x.toFixed(3)}; Y: {courier.position.y.toFixed(3)}
                            <OnMapBtn to_x={courier.position.x} to_y={courier.position.y} />
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
                    typeObj="courier"
                    idObj={courier.id}
                    onCancel={handleCancelUpdate}
                    initialData={courier}
                />
            )}

            {showDeleteForm && (
                <DeleteForm
                    typeObj="courier"
                    idObj={courier.id}
                    onCancel={handleCancelDelete}
                />
            )}
        </div>
    );
}

export default CourierRow;