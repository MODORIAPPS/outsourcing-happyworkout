import React, { useEffect, useState } from "react";

export type AuthContextType = {
    userUid: string;
    setUid: (uid: string) => void;
}

const initalContext: AuthContextType = {
    userUid: '',
    setUid: () => { },
}

export const AuthContext = React.createContext<AuthContextType | null>(null);

const AuthProvider: React.FC<{ children: React.ReactElement }> = ({ children }) => {
    const [data, setData] = useState<AuthContextType>(initalContext);

    const setUid = (uid: string) => {
        setData({ ...data, userUid: uid });
    };

    useEffect(() => {
        //const userUid = "xGhYwyaFLOwaKdIz4juF";
        const userUid = (window as any).HappyWorkout.getUserUid();
        setUid(userUid);
    }, []);

    return (
        <AuthContext.Provider value={{ userUid: data.userUid, setUid: setUid }}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthProvider;