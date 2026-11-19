import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import Login from './pages/Login';
import DocumentList from './pages/DocumentList';
import DocumentEditor from './pages/DocumentEditor';
import AdminPanel from './pages/AdminPanel';
import Profile from './pages/Profile';
import AppLayout from './components/Layout';
import StorageUtil from './utils/storage';
import './App.css';

// 路由守卫组件
function PrivateRoute({ children }) {
  const token = StorageUtil.getToken();
  return token ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={
            <PrivateRoute>
              <AppLayout />
            </PrivateRoute>
          }>
            <Route index element={<DocumentList />} />
            <Route path="document/:id" element={<DocumentEditor />} />
            <Route path="admin" element={<AdminPanel />} />
            <Route path="profile" element={<Profile />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
