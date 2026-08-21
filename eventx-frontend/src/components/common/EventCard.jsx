import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Calendar, Clock, MapPin, Tag, Users, Music, Briefcase, Trophy, Ticket, Mic, Tent, Hammer, Star } from 'lucide-react';
import { formatCurrency, formatDate, formatTime, getCategoryLabel } from '../../utils/helpers';

export const getCategoryIcon = (category) => {
  switch (category) {
    case 'CONCERT': return <Music size={16} />;
    case 'CONFERENCE': return <Briefcase size={16} />;
    case 'SPORTS': return <Trophy size={16} />;
    case 'THEATER': return <Ticket size={16} />;
    case 'COMEDY': return <Mic size={16} />;
    case 'FESTIVAL': return <Tent size={16} />;
    case 'WORKSHOP': return <Hammer size={16} />;
    default: return <Star size={16} />;
  }
};

const EventCard = ({ event }) => {
  const navigate = useNavigate();
  const [imgError, setImgError] = useState(false);

  const {
    id,
    title,
    category,
    imageUrl,
    venueName,
    city,
    eventDate,
    startTime,
    startingPrice,
    totalSeats,
    availableSeats,
    status
  } = event;

  const availabilityPercentage = totalSeats > 0 ? (availableSeats / totalSeats) * 100 : 0;
  let availabilityColor = 'var(--success)';
  let availabilityText = 'Available';

  if (status === 'CANCELLED') {
    availabilityColor = 'var(--danger)';
    availabilityText = 'Cancelled';
  } else if (availableSeats === 0 || status === 'SOLD_OUT') {
    availabilityColor = 'var(--danger)';
    availabilityText = 'Sold Out';
  } else if (availabilityPercentage < 20) {
    availabilityColor = 'var(--warning)';
    availabilityText = 'Filling Fast';
  }

  const handleImageError = () => {
    setImgError(true);
  };

  const handleCardClick = () => {
    navigate(`/events/${id}`);
  };

  return (
    <>
      <style>{`
        .event-card {
          background-color: var(--bg-surface);
          border: 1px solid var(--border);
          border-radius: 12px;
          overflow: hidden;
          transition: transform 0.3s ease, box-shadow 0.3s ease;
          display: flex;
          flex-direction: column;
          height: 100%;
          cursor: pointer;
        }
        .event-card:hover {
          transform: translateY(-4px);
          box-shadow: 0 10px 20px rgba(0,0,0,0.2);
        }
        .event-card-image-container {
          position: relative;
          height: 180px;
          overflow: hidden;
          background: linear-gradient(135deg, var(--bg-surface-hover), var(--bg-primary));
        }
        .event-card-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }
        .event-card:hover .event-card-image {
          transform: scale(1.05);
        }
        .event-card-badge {
          position: absolute;
          top: 12px;
          left: 12px;
          background-color: rgba(10, 10, 22, 0.8);
          backdrop-filter: blur(4px);
          color: var(--text-primary);
          padding: 6px 12px;
          border-radius: 20px;
          font-size: 0.75rem;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 6px;
          border: 1px solid var(--border);
        }
        .event-card-content {
          padding: 20px;
          display: flex;
          flex-direction: column;
          flex-grow: 1;
        }
        .event-card-title {
          font-size: 1.25rem;
          font-weight: 600;
          color: var(--text-primary);
          margin: 0 0 12px 0;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        .event-card-details {
          display: flex;
          flex-direction: column;
          gap: 8px;
          margin-bottom: 20px;
        }
        .event-card-detail-item {
          display: flex;
          align-items: center;
          gap: 8px;
          color: var(--text-secondary);
          font-size: 0.875rem;
        }
        .event-card-footer {
          margin-top: auto;
          padding-top: 16px;
          border-top: 1px solid var(--border);
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .event-card-price {
          display: flex;
          flex-direction: column;
        }
        .event-card-price-label {
          font-size: 0.75rem;
          color: var(--text-muted);
        }
        .event-card-price-value {
          font-size: 1.125rem;
          font-weight: 700;
          color: var(--primary-light);
        }
        .event-card-button {
          background-color: var(--primary);
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          font-weight: 500;
          cursor: pointer;
          transition: background-color 0.2s ease;
        }
        .event-card-button:hover {
          background-color: var(--primary-light);
        }
        .event-card-availability {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 0.75rem;
          margin-top: 8px;
          color: var(--text-secondary);
        }
        .availability-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
        }
      `}</style>
      <div className="event-card" onClick={handleCardClick}>
        <div className="event-card-image-container">
          {!imgError && imageUrl ? (
            <img 
              src={imageUrl} 
              alt={title} 
              className="event-card-image" 
              onError={handleImageError}
            />
          ) : (
            <div className="event-card-image" style={{ background: 'linear-gradient(135deg, var(--bg-surface-hover), var(--primary-dark))' }} />
          )}
          <div className="event-card-badge">
            {getCategoryIcon(category)}
            <span>{getCategoryLabel ? getCategoryLabel(category) : category}</span>
          </div>
        </div>
        
        <div className="event-card-content">
          <h3 className="event-card-title">{title}</h3>
          
          <div className="event-card-details">
            <div className="event-card-detail-item">
              <Calendar size={16} className="text-primary" />
              <span>{formatDate ? formatDate(eventDate) : eventDate}</span>
            </div>
            <div className="event-card-detail-item">
              <Clock size={16} className="text-primary" />
              <span>{formatTime ? formatTime(startTime) : startTime}</span>
            </div>
            <div className="event-card-detail-item">
              <MapPin size={16} className="text-primary" />
              <span>{venueName}, {city}</span>
            </div>
          </div>
          
          <div className="event-card-availability">
            <span className="availability-dot" style={{ backgroundColor: availabilityColor }}></span>
            <span>{availabilityText}</span>
          </div>
          
          <div className="event-card-footer">
            <div className="event-card-price">
              <span className="event-card-price-label">Starting from</span>
              <span className="event-card-price-value">
                {startingPrice === 0 ? 'Free' : (formatCurrency ? formatCurrency(startingPrice) : `$${startingPrice}`)}
              </span>
            </div>
            <button className="event-card-button" onClick={(e) => { e.stopPropagation(); handleCardClick(); }}>
              View Details
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default EventCard;
