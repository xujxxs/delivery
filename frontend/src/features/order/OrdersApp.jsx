import React, { useState, useEffect } from 'react';

import CourierFilter from '../courier/ui/CourierFilter';
import OrderRow from './ui/OrderRow';

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

const OrdersApp = () => {
    const sortOptions = {
        redactedAt: "Дате редактирования",
        createdAt: "Дате создания",
        weight: "Весу",
        cost: "Стоимости",
        id: "ID Заказа"
    };

    const sortKeys = Object.keys(sortOptions);
    const [sortBy, setSortBy] = useState(sortKeys[0]);
    const [pageNum, setPageNum] = useState(1);   

    const [showFilterForm, setShowFilterForm] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);

    const { objs, totalPages, loading, refreshPage } = usePage("order", sortBy, pageNum, null);
    const { order } = useNotifications();
    

    useEffect(() => {
        if (order) {
            refreshPage();
        }
    }, [order, refreshPage]);

    const handleBreakFilterClick = () => {
        setPageNum("");
        setSortBy("");
        refreshPage();
    }

    const handleFilterClick = () => {
        setShowFilterForm(!showFilterForm);
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
            <div className="orders-block app-list-work">
                <div className="list-editor">
                    <div className="create-export-import">
                        <CreateBtn nameObj={"заказ"} handleOnClick={handleCreateClick} />
                        <ExportBtn type="order" />
                        <ImportBtn type="order" />
                    </div>
                    <div className="filters-sort">
                        <div className="filter-and-break">
                            <FilterBtn handleOnClick={handleFilterClick} />
                            <BreakFilterBtn handleOnClick={handleBreakFilterClick} />
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
                            <div className="name">Название заказа</div>
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
                                {objs.map((order) => (
                                    <OrderRow 
                                        key={order.id}
                                        order={order} 
                                    />
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {showCreateForm && (
                    <CreateForm
                        typeObj="order" 
                        onCancel={handleCancelCreate}
                    />
                )}
            </div>

            {showFilterForm && (
                <div className="filter-courier-block app-filter">
                    <CourierFilter 
                        onClose={handleCloseFilter}
                    />
                </div>
            )}
        </div>
    );
};

export default OrdersApp;
