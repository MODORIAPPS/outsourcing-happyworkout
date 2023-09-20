import Channel from "@/components/chat/Channel";
import { AuthContext, AuthContextType } from "@/context/AuthProvider";
import useMe from "@/hooks/useMe";
import { useContext } from "react";

const Chat = () => {

    const { userUid } = useContext(AuthContext) as AuthContextType;
    const { user } = useMe(userUid);

    return (
        <div>
            <div>
                {
                    user && <Channel user={user} />
                }
            </div>
        </div>
    );
};

export default Chat;