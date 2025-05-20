import React, { useState, useEffect } from 'react';

import CourierFilter from '../courier/ui/CourierFilter';
import ScheduleRow from './ui/ScheduleRow';

import SelectorPage from "shared/components/selectors/SelectorPage"
import SelectorSort from "shared/components/selectors/SelectorSort"
import ExportBtn from "shared/components/buttons/Export"
import FilterBtn from "shared/components/buttons/Filter"
import Loader from 'shared/components/Loader';

import usePage from 'hooks/usePage';
import { useNotifications } from 'providers/WebSocketProvider';

import './ui/ScheduleTable.css';

const ScheduleApp = () => {
    const sortOptions = {
        redactedAt: "Дате редактирования",
        createdAt: "Дате создания",
        id: "ID Рассписания",
        "courier.id": "ID Курьера",
        "deliveryOrder.id": "ID Заказа",
        TypeOperation: "Типу рассписания",
    };

    const sortKeys = Object.keys(sortOptions);
    const [sortBy, setSortBy] = useState(sortKeys[0]);
    const [pageNum, setPageNum] = useState(1);   
    
    const [showFilterForm, setShowFilterForm] = useState(false);         

    const { objs, totalPages, loading, refreshPage } = usePage("schedule", sortBy, pageNum, null);
    const { schedule } = useNotifications();
    
    useEffect(() => {
        if (schedule) {
            refreshPage();
        }
    }, [schedule, refreshPage]);

    const handleFilterClick = () => {
        setShowFilterForm(!showFilterForm);
    };

    const handleCloseFilter = () => {
        setShowFilterForm(false);
    };

    return (
        <div className="app-main">
            <div className="schedule-block app-list-work">
                <div className="list-editor">
                    <div className="schedule-filters-sort">
                        <div className="filter-and-break">
                            <FilterBtn handleOnClick={handleFilterClick}/>
                            <ExportBtn 
                                type="schedule"
                                subClass="schedule-export" />
                        </div>
                        <div className="sort-and-page">
                            <SelectorSort
                                optionMap={sortOptions}
                                value={sortBy}
                                onSortChange={(value) => setSortBy(value)} 
                            />
                            <SelectorPage
                                optionList={Array.from({ length: totalPages }, (_, i) => i + 1)}
                                value={pageNum}
                                onPageChange={(value) => setPageNum(value)} 
                            />
                        </div>
                    </div>
                </div>
                <div className="table-container">
                    <div className="table-title">
                        <div className="table-row">
                            <div className="row-info">
                                <div className="id">ID</div>
                                <div className="order_id">ID Заказа</div>
                                <div className="courier_id">ID Курьера</div>
                                <div className="typeOperation">Тип операции</div>
                            </div>
                            <div className="dop"></div>
                        </div>
                    </div>

                    {loading && (
                        <div className="loading-table">
                            <Loader />
                            <p>Загрузка...</p>
                        </div>
                    )}

                    {!loading && (
                        <div className="scroll" style={{ overflowY: 'scroll' }}>
                            <div className="table-body">
                                {objs.map((schedule) => (
                                    <ScheduleRow 
                                        key={schedule.id}
                                        schedule={schedule} 
                                    />
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {showFilterForm && (
                <div className="filter-schedules-block app-filter">
                    <CourierFilter 
                        onClose={handleCloseFilter}
                    />
                </div>
            )}
        </div>
    );
};

export default ScheduleApp;
