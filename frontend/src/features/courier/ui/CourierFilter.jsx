import React from "react";
import FilterBlock from "shared/components/FilterBlock"

const CourierFilter = ({ onClose }) => {
    return ( 
        <div className="app-filter">
            <div id="close-filter" className="filter-block title-filter-block">
                <button onClick={onClose} />
                <p>Фильтры</p>
            </div>
            <FilterBlock
                title={<p>ФИО Курьера</p>}
                content={
                    <div className="content-filter-block">
                        <input type="text" placeholder="Фамилия" />
                        <input type="text" placeholder="Имя" />
                        <input type="text" placeholder="Отчество" />
                    </div>
                }
            />
            <FilterBlock
                title={<>
                    <p>Скорость</p><p id="dop-p">(км/ч)</p>
                </>}
                content={
                    <div className="content-filter-block line-inputs">
                        <input type="text" placeholder="от" />
                        <input type="text" placeholder="до" />
                    </div>
                }
            />
            <FilterBlock
                title={<>
                    <p>Грузоподъёмность</p><p id="dop-p">(грамм)</p>
                </>}
                content={
                    <div className="content-filter-block line-inputs">
                        <input type="text" placeholder="от" />
                        <input type="text" placeholder="до" />
                    </div>
                }
            />
            <FilterBlock
                title={<>
                    <p>Стоимость работы</p><p id="dop-p">(ед.в./ч)</p>
                </>}
                content={
                    <div className="content-filter-block line-inputs">
                        <input type="text" placeholder="от" />
                        <input type="text" placeholder="до" />
                    </div>
                }
            />
            <div className="btn-block filter-block">
                <button className="use-filters-btn">Применить</button>
                <button className="break-filters-btn">Сбросить</button>
            </div>
        </div>
    );
}

export default CourierFilter;