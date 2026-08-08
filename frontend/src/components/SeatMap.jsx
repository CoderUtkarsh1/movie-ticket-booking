import { useState } from 'react';
import './SeatMap.css';

const SeatMap = ({ seats, selectedSeats, onSeatSelect, maxSelection = 10 }) => {
  // Group seats by row
  const seatsByRow = seats.reduce((acc, seat) => {
    if (!acc[seat.seatRow]) {
      acc[seat.seatRow] = [];
    }
    acc[seat.seatRow].push(seat);
    return acc;
  }, {});

  // Sort rows alphabetically
  const rows = Object.keys(seatsByRow).sort();

  const handleSeatClick = (seat) => {
    if (seat.status !== 'AVAILABLE') return;

    const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
    
    if (isSelected) {
      onSeatSelect(selectedSeats.filter(s => s.seatId !== seat.seatId));
    } else {
      if (selectedSeats.length >= maxSelection) {
        alert(`You can only select up to ${maxSelection} seats.`);
        return;
      }
      onSeatSelect([...selectedSeats, seat]);
    }
  };

  return (
    <div className="seat-map-container">
      <div className="screen-curve">
        <div className="screen-text">SCREEN THIS WAY</div>
      </div>

      <div className="seat-map">
        {rows.map(row => (
          <div key={row} className="seat-row">
            <div className="row-label">{row}</div>
            <div className="seats">
              {seatsByRow[row]
                .sort((a, b) => a.seatNumber - b.seatNumber)
                .map(seat => {
                  const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                  const isAvailable = seat.status === 'AVAILABLE';
                  
                  let seatClass = 'seat';
                  if (!isAvailable) seatClass += ' seat-booked';
                  else if (isSelected) seatClass += ' seat-selected';
                  else seatClass += ' seat-available';

                  // Visual distinction for PREMIUM vs STANDARD
                  if (seat.seatType === 'PREMIUM' && isAvailable && !isSelected) {
                    seatClass += ' seat-premium';
                  }

                  return (
                    <button
                      key={seat.seatId}
                      className={seatClass}
                      onClick={() => handleSeatClick(seat)}
                      disabled={!isAvailable}
                      title={`Row ${seat.seatRow} - Seat ${seat.seatNumber} (₹${seat.price})`}
                    >
                      {seat.seatNumber}
                    </button>
                  );
                })}
            </div>
          </div>
        ))}
      </div>

      <div className="seat-legend">
        <div className="legend-item">
          <div className="seat seat-available legend-box"></div>
          <span>Available</span>
        </div>
        <div className="legend-item">
          <div className="seat seat-premium legend-box"></div>
          <span>Premium</span>
        </div>
        <div className="legend-item">
          <div className="seat seat-selected legend-box"></div>
          <span>Selected</span>
        </div>
        <div className="legend-item">
          <div className="seat seat-booked legend-box"></div>
          <span>Booked/Blocked</span>
        </div>
      </div>
    </div>
  );
};

export default SeatMap;
