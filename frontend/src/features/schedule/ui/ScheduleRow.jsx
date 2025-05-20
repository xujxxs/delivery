import React, { useState, useRef, useEffect } from 'react';
import OnMapBtn from "shared/components/buttons/OnMap"

const ScheduleRow = ({ schedule }) => {
    const [isActive, setIsActive] = useState(false);
    const [maxHeight, setMaxHeight] = useState(0);
    const moreInfoRef = useRef(null);

    const toggleMoreInfo = () => {
        setIsActive(!isActive);
    };

    useEffect(() => {
        if (moreInfoRef.current) {
            setMaxHeight(moreInfoRef.current.scrollHeight);
        }
    }, [isActive]);

    return (
        <div className="all-info" key={schedule.id} >
            <div className="table-row">
                <div className="row-info">
                    <div className="id">{schedule.id}</div>
                    <div className="order_id">{schedule.deliveryOrder.id}</div>
                    <div className="courier_id">{schedule.courier.id}</div>
                    <div className="typeOperation">
                        {schedule.typeOperation === "DELIVERY" ? "Доставка" : "Подбор"}
                    </div>
                </div>
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
                        <div className="name">Курьер: </div>
                        <div className="value">{schedule.courier.id}</div>
                    </div>
                    <div className="line">
                        <div className="name">Заказ:</div>
                        <div className="value">{schedule.deliveryOrder.id}</div>
                    </div>
                    <div className="line">
                        <div className="name">Индекс операции для курьера:</div>
                        <div className="value">{schedule.index}</div>
                    </div>
                    <div className="line">
                        <div className="name">Конечное время операции:</div>
                        <div className="value">{schedule.arrivalTime}</div>
                    </div>
                    <div className="line">
                        <div className="name">Начальная позиция:</div>
                        <div className="value">
                            X: {schedule.positionStart.x.toFixed(3)}; Y: {schedule.positionStart.y.toFixed(3)}
                            <OnMapBtn to_x={schedule.positionStart.x} to_y={schedule.positionStart.y} />
                        </div>
                    </div>
                    <div className="line">
                        <div className="name">Конечная позиция:</div>
                        <div className="value">
                            X: {schedule.positionEnd.x.toFixed(3)}; Y: {schedule.positionEnd.y.toFixed(3)}
                            <OnMapBtn to_x={schedule.positionEnd.x} to_y={schedule.positionEnd.y} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ScheduleRow;