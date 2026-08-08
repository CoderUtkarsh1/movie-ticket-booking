import { useGetNotificationsQuery, useMarkAsReadMutation } from '../../features/notifications/notificationApiSlice';
import { Bell, Mail, MessageSquare } from 'lucide-react';
import './NotificationsPage.css';

const NotificationsPage = () => {
  const { data: notificationsRes, isLoading, refetch } = useGetNotificationsQuery();
  const [markAsRead] = useMarkAsReadMutation();

  const notifications = notificationsRes || [];

  const handleMarkAsRead = async (id, isRead) => {
    if (isRead) return;
    try {
      await markAsRead(id).unwrap();
      refetch(); // to update count in other parts if we had a global listener, though RTK handles cache
    } catch (err) {
      console.error('Failed to mark notification as read', err);
    }
  };

  const getIcon = (channel) => {
    switch (channel) {
      case 'EMAIL': return <Mail size={20} />;
      case 'SMS': return <MessageSquare size={20} />;
      default: return <Bell size={20} />;
    }
  };

  return (
    <div className="notifications-page">
      <div className="dashboard-page-header">
        <h2>Notifications</h2>
      </div>

      <div className="notifications-list glass-card">
        {isLoading ? (
          <div className="loader-container"><div className="spinner"></div></div>
        ) : notifications.length > 0 ? (
          notifications.map(notification => (
            <div 
              key={notification.id} 
              className={`notification-item ${!notification.read ? 'unread' : ''}`}
              onClick={() => handleMarkAsRead(notification.id, notification.read)}
            >
              <div className="notification-icon">
                {getIcon(notification.channel)}
              </div>
              
              <div className="notification-content">
                <p className="notification-message">{notification.message}</p>
                <div className="notification-meta">
                  <span className="notification-time">
                    {new Date(notification.createdAt).toLocaleString()}
                  </span>
                  {notification.bookingCode && (
                    <span className="notification-booking-ref">
                      Booking Ref: {notification.bookingCode}
                    </span>
                  )}
                </div>
              </div>
              
              {!notification.read && <div className="unread-dot"></div>}
            </div>
          ))
        ) : (
          <div className="empty-state">
            <Bell size={48} style={{ opacity: 0.2, marginBottom: '1rem' }} />
            <p>You have no notifications.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default NotificationsPage;
