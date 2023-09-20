import { getDocs, onSnapshot } from "firebase/firestore";
import { useEffect, useRef, useState } from "react";

export function useFirestoreQuery(query) {
    const [docs, setDocs] = useState([]);

    // Store current query in ref
    const queryRef = useRef(query);

    // Compare current query with the previous one
    useEffect(() => {
        // Use Firestore built-in 'isEqual' method
        // to compare queries
        if (!queryRef?.curent?.isEqual(query)) {
            queryRef.current = query;
        }
    });

    useEffect(() => {
        if (!queryRef.current) {
            return null;
        }

        onSnapshot(queryRef.current, querySnapshot => {
            const data = querySnapshot.docs.map(doc => ({
                ...doc.data(),
                id: doc.id,
            }));
            setDocs(data);
        });
    }, [queryRef]);

    return docs;
}

export default useFirestoreQuery;