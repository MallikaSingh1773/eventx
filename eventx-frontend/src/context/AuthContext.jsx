import { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';
import { toast } from 'react-toastify';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const token = localStorage.getItem('accessToken');
    if (savedUser && token) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (credentials) => {
    const response = await authService.login(credentials);
    const data = response.data;
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    const userData = { id: data.userId, name: data.name, email: data.email, role: data.role };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    toast.success(`Welcome back, ${data.name}!`);
    return userData;
  };

  const register = async (data) => {
    const response = await authService.register(data);
    const authData = response.data;
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    const userData = { id: authData.userId, name: authData.name, email: authData.email, role: authData.role };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    toast.success('Account created successfully!');
    return userData;
  };

  const logout = async () => {
    try { await authService.logout(); } catch (e) { /* ignore */ }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
    toast.info('Logged out successfully');
  };

  const isAdmin = () => user?.role === 'ROLE_ADMIN';
  const isOrganizer = () => user?.role === 'ROLE_ORGANIZER' || user?.role === 'ROLE_ADMIN';
  const isAuthenticated = () => !!user;

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, isAdmin, isOrganizer, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};
