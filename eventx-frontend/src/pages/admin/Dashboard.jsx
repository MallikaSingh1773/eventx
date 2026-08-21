import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const Dashboard = () => {
  const [stats, setStats] = useState({ totalEvents: 0, totalBookings: 0, revenue: 0 });
  const [revenueData, setRevenueData] = useState([]);

  useEffect(() => {
    setStats({ totalEvents: 25, totalBookings: 150, revenue: 50000 });
    
    setRevenueData([
      { name: 'Jan', revenue: 4000 },
      { name: 'Feb', revenue: 3000 },
      { name: 'Mar', revenue: 2000 },
      { name: 'Apr', revenue: 2780 },
      { name: 'May', revenue: 1890 },
      { name: 'Jun', revenue: 2390 },
      { name: 'Jul', revenue: 3490 },
    ]);
  }, []);

  return (
    <div className="container dashboard-page">
      <h2>Admin Dashboard</h2>
      
      <div className="stats-grid mt-4" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
        <div className="glass-card stat-card">
          <h3>Total Events</h3>
          <p className="text-xl mt-2" style={{ fontSize: '24px', fontWeight: 'bold' }}>{stats.totalEvents}</p>
        </div>
        <div className="glass-card stat-card">
          <h3>Total Bookings</h3>
          <p className="text-xl mt-2" style={{ fontSize: '24px', fontWeight: 'bold' }}>{stats.totalBookings}</p>
        </div>
        <div className="glass-card stat-card">
          <h3>Total Revenue</h3>
          <p className="text-xl mt-2" style={{ fontSize: '24px', fontWeight: 'bold' }}>${stats.revenue}</p>
        </div>
      </div>

      <div className="glass-card mt-4 chart-container">
        <h3>Revenue Overview</h3>
        <div style={{ height: '300px', width: '100%', marginTop: '20px' }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#444" />
              <XAxis dataKey="name" stroke="#ccc" />
              <YAxis stroke="#ccc" />
              <Tooltip contentStyle={{ backgroundColor: '#1a1a2e', border: 'none', borderRadius: '8px' }} />
              <Line type="monotone" dataKey="revenue" stroke="#7C3AED" strokeWidth={3} dot={{ r: 6 }} activeDot={{ r: 8 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
