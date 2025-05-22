import React, { useState, useEffect } from 'react';

import CourierFilter from './ui/CourierFilter';
import CourierRow from './ui/CourierRow';

import CreateForm from 'shared/forms/CreateForm';

import SelectorPage from "shared/components/selectors/SelectorPage"
import SelectorSort from "shared/components/selectors/SelectorSort"
import BreakFilterBtn from "shared/components/buttons/BreakFilter"
import CreateBtn from "shared/components/buttons/Create"
import ExportBtn from "shared/components/buttons/Export"
import FilterBtn from "shared/components/buttons/Filter"
import ImportBtn from "shared/components/buttons/Import"
import Loader from 'shared/components/Loader';

import usePage from 'hooks/usePage';
import { useNotifications } from 'providers/WebSocketProvider';

import 'shared/styles/table/CourierOrderTable.css';

const CouriersApp = () => {
    const sortOptions = {
        redactedAt: "Дате редактирования",
        createdAt: "Дате создания",
        loadCapacity: "Грузоподъёмности",
        speed: "Скорости",
        cost: "Ценам",
        id: "ID Курьера"
    };

    const sortKeys = Object.keys(sortOptions);
    const [sortBy, setSortBy] = useState(sortKeys[0]);
    const [pageNum, setPageNum] = useState(1);   
    
    const [showFilterForm, setShowFilterForm] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);           

    const { objs, totalPages, loading, refreshPage } = usePage("courier", sortBy, pageNum, null);
    const { courier } = useNotifications();

    useEffect(() => {
        if (courier) {
            refreshPage();
        }
    }, [courier, refreshPage]);


    const handleBreakFilterClick = () => {
        setSortBy(sortKeys[0]);
        setPageNum(1);
        refreshPage();
    }

    const handleFilterClick = () => {
        setShowFilterForm(true);
    };

    const handleCloseFilter = () => {
        setShowFilterForm(false);
    };

    const handleCreateClick = () => {
        setShowCreateForm(true);
    };

    const handleCancelCreate = () => {
        setShowCreateForm(false);
    };

    return (
        <div className="app-main">
            <div className={`couriers-block app-list-work ${showFilterForm ? 'filter-active' : ''}`}>
                <div className="list-editor">
                    <div className="create-export-import">
                        <CreateBtn nameObj={"курьера"} handleOnClick={handleCreateClick} />
                        <ExportBtn type="courier" />
                        <ImportBtn type="courier" />
                    </div>
                    <div className="filters-sort">
                        <div className="filter-and-break">
                            <FilterBtn handleOnClick={handleFilterClick}/>
                            <BreakFilterBtn handleOnClick={handleBreakFilterClick}/>
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
                            <div className="id">ID</div>
                            <div className="name">ФИО Курьера</div>
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
                                {objs.map((courier) => {
                                    courier = {
                                        ...courier,
                                        cost: courier.cost * 3600
                                    };
                                    return (
                                        <CourierRow 
                                            key={courier.id}
                                            courier={courier} 
                                        />
                                    );
                                })}
                            </div>
                        </div>
                    )}
                </div>

                {showCreateForm && (
                    <CreateForm
                        typeObj="courier" 
                        onCancel={handleCancelCreate}
                    />
                )}
            </div>

            {showFilterForm && (
                <CourierFilter 
                    onClose={handleCloseFilter}
                />
            )}
        </div>
    );
};

export default CouriersApp;
