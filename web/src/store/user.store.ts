import { UserDTO } from "@/apis/dto/user.dto";
import { create } from "zustand";

const initData: UserDTO = {
    firebaseUid: '',
    uid: '',
    nickname: '',
    profileImageUrl: ''
};

type UserStore = {
    user: UserDTO;
    setUser: (user: UserDTO) => void;
    clearUser: () => void;
};

const useUserStore = create<UserStore>((set) => ({
    user: initData,
    setUser: (user: UserDTO) => set({ user }),
    clearUser: () => set({ user: initData })
}))

export default useUserStore;

