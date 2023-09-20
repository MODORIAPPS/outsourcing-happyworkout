import { UserDTO } from "@/apis/dto/user.dto";
import { db } from "@/utils/firebase";
import { doc, getDoc } from "firebase/firestore";
import { useEffect, useState } from "react";

const useMe = (firebaseUid: string) => {
    console.log(firebaseUid)
    const [user, setUser] = useState<UserDTO>();
    const [loading, setLoading] = useState<boolean>(false);


    useEffect(() => {
        if (!firebaseUid) return;
        (async () => {
            const userRef = doc(db, "users", firebaseUid);
            setLoading(true);
            const userDoc = await getDoc(userRef);
            if (userDoc.exists()) {
                const user = userDoc.data() as UserDTO;
                setUser(user);
            }
            setLoading(false);
        })();
    }, [firebaseUid])

    return { user, loading };
};

export default useMe;