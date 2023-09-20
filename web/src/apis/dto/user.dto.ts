/**
 * This DTO is using in Firebase firestore
 */
export interface UserDTO {
    firebaseUid: string;
    uid: string;
    nickname: string;
    profileImageUrl: string;
}