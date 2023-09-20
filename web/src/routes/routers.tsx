import { ROUTE_URLS } from '@/constants/route.ts';
import Policy from '@/pages/policy';
import Chat from '@/pages/chat';
import React from 'react';
import { Route, Routes } from 'react-router';
import { BrowserRouter } from 'react-router-dom';
import AuthProvider from '@/context/AuthProvider';

const COMMON_ROUTER = [
    {
        path: ROUTE_URLS.policy,
        element: <Policy />,
    },
    {
        path: ROUTE_URLS.chat,
        element: <AuthProvider>
            <Chat />
        </AuthProvider>,
    },
];

const Router: React.FC = () => {

    return (
        <BrowserRouter>
            <Routes>
                {
                    COMMON_ROUTER.map(route => <Route key={route.path} {...route} />)
                }
            </Routes>
        </BrowserRouter>
    );
};

export default Router;