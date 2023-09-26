interface TopBarProps {
    title: string;
}

const TopBar: React.FC<TopBarProps> = () => {

    return (
        <div className="flex flex-row items-center px-4 py-5">
            <p className="ml-2 font-bold text-gray-700">채팅하기</p>
        </div>
    );
};

export default TopBar;