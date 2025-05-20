import React, { useState } from 'react';

import "./TagInput.css"

const TagInput = ({ supportedTypeOrders = [], onChange }) => {
    const [inputValue, setInputValue] = useState('');

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && inputValue.trim()) {
            e.preventDefault();
            onChange([
                ...supportedTypeOrders,
                { type: inputValue.trim() },
            ]);
            setInputValue('');
        }
    };

    const handleRemove = (typeToRemove) => {
        onChange(
            supportedTypeOrders.filter((el) => el.type !== typeToRemove)
        );
    };

    return (
        <>
            <input
                type="text"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Введите тип заказа"
            />

            <div className="container-supported-types">
                {supportedTypeOrders.map((el, idx) => (
                    <button
                        key={`${el.type}-${idx}`}
                        onClick={() => handleRemove(el.type)}
                    >
                        {el.type}
                    </button>
                ))}
            </div>
        </>
    );
}

export default TagInput;