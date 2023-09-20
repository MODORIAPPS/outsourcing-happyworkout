import Message from "@/components/chat/Message";
import TopBar from "@/components/common/TopBar";
import { useFirestoreQuery } from '@/hooks/useFirestoreQuery';
import { db } from '@/utils/firebase';
import { Timestamp, addDoc, collection, orderBy, query, } from "firebase/firestore";
import { useEffect, useRef, useState } from "react";

const Channel = ({ user }) => {

    const hood = window.HappyWorkout.getHood();

    const messagesRef = collection(db, `messages-${hood}`);
    // const messagesRef = db.collection('messages');
    const messages = useFirestoreQuery(
        query(messagesRef, orderBy('createdAt', "desc"))
    );

    const [newMessage, setNewMessage] = useState('');

    const inputRef = useRef();
    const bottomListRef = useRef();

    const { firebaseUid, nickname, profileImageUrl } = user;

    useEffect(() => {
        if (inputRef.current) {
            inputRef.current.focus();
        }
    }, [inputRef]);

    const handleOnChange = e => {
        setNewMessage(e.target.value);
    };

    const handleOnSubmit = e => {
        e.preventDefault();

        const trimmedMessage = newMessage.trim();
        if (trimmedMessage) {
            const data = {
                text: trimmedMessage,
                createdAt: Timestamp.now(),
                uid: firebaseUid,
                displayName: nickname,
                photoURL: profileImageUrl,
            }
            console.log(data);
            addDoc(messagesRef, data);
            setNewMessage('');
            bottomListRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    };

    return (
        <div>
            <TopBar title="채팅방" />
            <div className="flex flex-col h-full">
                <div className="overflow-auto h-full">
                    <div className="py-4 max-w-screen-lg mx-auto">
                        <div className="border-b dark:border-gray-600 border-gray-200 py-8 mb-4">
                            <div className="font-bold text-3xl text-center">
                                <p className="mb-1">{hood}</p>
                            </div>
                            <p className="text-gray-400 text-center">
                                {hood} 대화방에 오신 것을 환영합니다. <br />
                                커뮤니티 사용 가이드라인을 준수해주세요.
                            </p>
                        </div>
                        <ul>
                            {messages
                                ?.sort((first, second) =>
                                    first?.createdAt?.seconds <= second?.createdAt?.seconds ? -1 : 1
                                )
                                ?.map(message => (
                                    <li key={message.id}>
                                        <Message {...message} />
                                    </li>
                                ))}
                        </ul>
                        <div ref={bottomListRef} />
                    </div>
                </div>
                <div className="mb-6 mx-4">
                    <form
                        onSubmit={handleOnSubmit}
                        className="flex flex-row bg-gray-200 dark:bg-coolDark-400 rounded-md px-4 py-3 z-10 max-w-screen-lg mx-auto dark:text-white shadow-md"
                    >
                        <input
                            ref={inputRef}
                            type="text"
                            value={newMessage}
                            onChange={handleOnChange}
                            placeholder="메시지 입력"
                            className="flex-1 bg-transparent outline-none"
                        />
                        <button
                            type="submit"
                            disabled={!newMessage}
                            className="uppercase font-semibold text-sm tracking-wider text-gray-500 hover:text-gray-900 dark:hover:text-white transition-colors"
                        >
                            전송
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Channel;