import React, {
    useRef,
    useEffect,
    useState,
    useCallback,
    forwardRef,
    useImperativeHandle
} from 'react';
import useMap from 'hooks/useMap';

import './ui/MapComponent.css';

const MapComponent = forwardRef(({ minScale = 100, maxScale = 100000 }, ref) => {
    const canvasRef = useRef(null);
    const [scale, setScale] = useState(200);
    const [origin, setOrigin] = useState({ x: window.innerWidth / 2, y: window.innerHeight / 2 });
    const [isPanning, setIsPanning] = useState(false);
    const [mousePos, setMousePos] = useState({ x: null, y: null });
    const panStart = useRef({ x: 0, y: 0 });
    const [bounds, setBounds] = useState({ topLeft: { x: 0, y: 0 }, bottomRight: { x: 0, y: 0 } });

    const mapData = useMap(bounds);

    useImperativeHandle(ref, () => ({
        focusOn({ x, y }) {
            const canvas = canvasRef.current;
            if (!canvas) return;
            const { width, height } = canvas;
            const newOrigin = {
                x: width / 2 - x * scale,
                y: height / 2 + y * scale
            };
            setOrigin(newOrigin);
            setBounds({
                topLeft: {
                    x: (0 - newOrigin.x) / scale,
                    y: (newOrigin.y - 0) / scale
                },
                bottomRight: {
                    x: (width - newOrigin.x) / scale,
                    y: (newOrigin.y - height) / scale
                }
            });
        }
    }));

    const draw = useCallback(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        ctx.clearRect(0, 0, width, height);

        const step = scale / 10;
        ctx.beginPath();
        ctx.lineWidth = 1;
        ctx.strokeStyle = '#ddd';
        for (let x = origin.x % step; x < width; x += step) {
            ctx.moveTo(x, 0);
            ctx.lineTo(x, height);
        }
        for (let y = origin.y % step; y < height; y += step) {
            ctx.moveTo(0, y);
            ctx.lineTo(width, y);
        }
        ctx.stroke();

        ctx.beginPath();
        ctx.strokeStyle = '#555';
        ctx.moveTo(origin.x, 0);
        ctx.lineTo(origin.x, height);
        ctx.moveTo(0, origin.y);
        ctx.lineTo(width, origin.y);
        ctx.stroke();

        const couriers = mapData.couriers.map(c => ({ id: c.id, type: 'courier', x: c.point.x, y: c.point.y }));
        const pickups = mapData.orders.map(o => ({ id: o.id, type: 'pickup', x: o.startPoint.x, y: o.startPoint.y }));
        const deliveries = mapData.orders.map(o => ({ id: o.id, type: 'delivery', x: o.endPoint.x, y: o.endPoint.y }));
        const schedules = mapData.schedules.map(s => ({ id: s.id, type: 'schedule', x1: s.startPosition.x, y1: s.startPosition.y, x2: s.endPosition.x, y2: s.endPosition.y }));
        const objects = [...couriers, ...pickups, ...deliveries, ...schedules];

        objects.forEach(obj => {
            if (obj.type === 'schedule') {
                const sx = origin.x + obj.x1 * scale;
                const sy = origin.y - obj.y1 * scale;
                const ex = origin.x + obj.x2 * scale;
                const ey = origin.y - obj.y2 * scale;
                ctx.beginPath();
                ctx.lineWidth = 1;
                ctx.strokeStyle = 'grey';
                ctx.moveTo(sx, sy);
                ctx.lineTo(ex, ey);
                ctx.stroke();
                return;
            }

            const screenX = origin.x + obj.x * scale;
            const screenY = origin.y - obj.y * scale;
            const size = Math.max(1, scale * 0.001);
            ctx.beginPath();
            ctx.lineWidth = 2;
            ctx.strokeStyle = 'black';
            ctx.fillStyle = 'transparent';

            switch (obj.type) {
                case 'courier':
                    ctx.moveTo(screenX, screenY + size);
                    ctx.lineTo(screenX - size, screenY - size);
                    ctx.lineTo(screenX + size, screenY - size);
                    ctx.closePath();
                    break;
                case 'pickup':
                    ctx.rect(screenX - size, screenY - size, size * 2, size * 2);
                    break;
                case 'delivery':
                    ctx.arc(screenX, screenY, size, 0, 2 * Math.PI);
                    break;
                default:
                  break;
            }
            ctx.stroke();
        });
    }, [origin, scale, mapData]);

    useEffect(() => draw(), [draw]);

    const resizeCanvas = useCallback(() => {
        const canvas = canvasRef.current;
        if (canvas) {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
            setOrigin({ x: canvas.width / 2, y: canvas.height / 2 });
        }
    }, []);

    useEffect(() => {
        resizeCanvas();
        window.addEventListener('resize', resizeCanvas);
        return () => window.removeEventListener('resize', resizeCanvas);
    }, [resizeCanvas]);

    const applyZoom = ({ factor, center }) => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const newScale = Math.max(minScale, Math.min(maxScale, scale * factor));
        const ox = center.x - ((center.x - origin.x) * newScale) / scale;
        const oy = center.y - ((center.y - origin.y) * newScale) / scale;
        setScale(newScale);
        setOrigin({ x: ox, y: oy });
        setBounds({
            topLeft: { x: (0 - ox) / newScale, y: (oy - 0) / newScale },
            bottomRight: { x: (canvas.width - ox) / newScale, y: (oy - canvas.height) / newScale }
        });
    };

    const handleMouseMove = e => {
        const rect = canvasRef.current.getBoundingClientRect();
        const mouseX = e.clientX - rect.left,
        mouseY = e.clientY - rect.top;
        setMousePos({
            x: (mouseX - origin.x) / scale,
            y: (origin.y - mouseY) / scale
        });
        if (isPanning) setOrigin({ x: e.clientX - panStart.current.x, y: e.clientY - panStart.current.y });
    };
    const handleWheel = e => { 
        e.preventDefault(); 
        applyZoom({ 
            factor: e.deltaY < 0 ? 1.1 : 0.9, 
            center: { 
                x: e.clientX - canvasRef.current.getBoundingClientRect().left, 
                y: e.clientY - canvasRef.current.getBoundingClientRect().top 
              } 
        }); 
    };

    const handleMouseDown = e => { 
        e.preventDefault(); 
        setIsPanning(true); 
        panStart.current = { 
            x: e.clientX - origin.x, 
            y: e.clientY - origin.y 
        }; 
    };

    const handleMouseUp = () => { 
        setIsPanning(false); 
        setBounds({ 
            topLeft: { 
                x: (0 - origin.x) / scale, 
                y: (origin.y - 0) / scale 
            }, 
            bottomRight: { 
                x: (canvasRef.current.width - origin.x) / scale, 
                y: (origin.y - canvasRef.current.height) / scale 
            } 
        }); 
    };

    return (
        <>
            <canvas
                ref={canvasRef}
                style={{ cursor: isPanning ? 'grabbing' : 'grab' }}
                onWheel={handleWheel}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
            />
            {mousePos.x !== null && (
                <div className="coordinates-container">
                    Позиция: {mousePos.x.toFixed(3)} | {mousePos.y.toFixed(3)}
                </div>
            )}
            <div className="map-control-container">
                <button onClick={() => applyZoom({ factor: 1.1, center: { x: canvasRef.current.width/2, y: canvasRef.current.height/2 } })}>+</button>
                <button onClick={() => applyZoom({ factor: 0.9, center: { x: canvasRef.current.width/2, y: canvasRef.current.height/2 } })}>−</button>
            </div>
        </>
    );
});

export default MapComponent;
