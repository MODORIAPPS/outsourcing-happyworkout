import { initializeApp } from "firebase/app";
import { getDatabase } from "firebase/database";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
    apiKey: "AIzaSyBBztNaKJYzGo-NJlQIRetNeL7MDjNCofo",
    authDomain: "happyworkout-f638d.firebaseapp.com",
    databaseURL: "https://happyworkout-f638d-default-rtdb.asia-southeast1.firebasedatabase.app",
    projectId: "happyworkout-f638d",
    storageBucket: "happyworkout-f638d.appspot.com",
    messagingSenderId: "214243782399",
    appId: "1:214243782399:web:08e1780146ee6924e626ca"
};

const app = initializeApp(firebaseConfig);

export const db = getFirestore(app);
export const realtimeDB = getDatabase(app);
export const storage = getStorage(app);