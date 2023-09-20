import { IcBackArrow } from "@/assets/icons";

interface TopBarProps {
    title: string;
}

const TopBar: React.FC<TopBarProps> = ({ title }) => {

    const handleClickBack = () => closeWindow();
    
    return (
        <div className="flex flex-row items-center px-4 py-5">
            <img onClick={handleClickBack} className="w-6 h-6" src={IcBackArrow} />
            <p className="ml-2 font-bold text-gray-700">{title}</p>
        </div>
    );
};

/**
 * Android Interface
 * `finish()` 호출
 */
const closeWindow = () => {
    (window as any).HappyWorkout.close();
};

export default TopBar;