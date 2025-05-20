import React, { useState, useRef, createContext } from 'react';
import MapComponent from 'features/map/MapComponent';
import { WebSocketProvider } from 'providers/WebSocketProvider';
import NotificationQueue from 'features/notification/NotificationQueue';
import SlidePanel from 'features/slide_panel/SlidePanel';

import 'shared/styles/App.css'

export const MapContext = createContext(null);

function App() {
    const [isPanelOpen, setIsPanelOpen] = useState(false);
    const mapRef = useRef(null);

    return (
        <WebSocketProvider>
            <MapContext.Provider value={mapRef}>
                <div className="app-container">
                
                    <NotificationQueue />
                
                    <SlidePanel 
                        onClose={() => setIsPanelOpen(false)} 
                        isOpen={isPanelOpen} 
                    />

                    <MapComponent ref={mapRef} />

                    <button 
                        className="open-slide-menu" 
                        onClick={() => setIsPanelOpen(true)}
                    />
                </div>
            </MapContext.Provider>
        </WebSocketProvider>
        
    );
}

export default App;
