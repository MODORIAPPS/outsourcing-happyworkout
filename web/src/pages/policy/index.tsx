import ReactMarkdown from "react-markdown";
import MarkdownContent from '@/docs/policy.md?raw';
import TopBar from "@/components/common/TopBar";

const Policy: React.FC = () => {

    console.log(MarkdownContent)

    return (
        <article className="container mx-auto px-4 prose lg:prose-base">
            <TopBar title="개인정보 이용약관" />
            <h2 className="py-4">개인정보 이용약관</h2>
            <ReactMarkdown>
                {MarkdownContent}
            </ReactMarkdown>
        </article>
    );
};

export default Policy;