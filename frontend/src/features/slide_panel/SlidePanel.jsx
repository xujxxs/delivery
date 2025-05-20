import React, { useState } from 'react';
import CouriersApp from 'features/courier/CouriersApp';
import OrdersApp from 'features/order/OrdersApp';
import ScheduleApp from 'features/schedule/ScheduleApp';

import './ui/SlidePanel.css';
import './ui/AppBarSlidePanel.css';
import './ui/AppWorkSlidePanel.css';
import './ui/FilterSlidePanel.css';
import './ui/TableSlidePanel.css';

const TAB_OPTIONS = {
  COURIERS: 'Курьеры',
  ORDERS: 'Заказы',
  SCHEDULE: 'Расписание'
};

const SlidePanel = ({ onClose, isOpen }) => {
  const [selectedTab, setSelectedTab] = useState(TAB_OPTIONS.COURIERS);

  const renderContent = () => {
    switch (selectedTab) {
      case TAB_OPTIONS.COURIERS:
        return <CouriersApp />;
      case TAB_OPTIONS.ORDERS:
        return <OrdersApp />;
      case TAB_OPTIONS.SCHEDULE:
        return <ScheduleApp />;
      default:
        return null;
    }
  };

  return (
    <div className={`slide-panel ${isOpen ? 'open' : ''}`}>
      <div className="app-bar">
        <ul className="chose-app">
          {Object.values(TAB_OPTIONS).map((tab) => (
            <li
              key={tab}
              className={selectedTab === tab ? 'selected' : ''}
              onClick={() => setSelectedTab(tab)}
            >
              {tab}
            </li>
          ))}
        </ul>
        <button className="close-slide-menu" onClick={onClose}></button>
      </div>
      <div className="app-selected">
        {renderContent()}
      </div>
    </div>
  );
};

export default SlidePanel;
