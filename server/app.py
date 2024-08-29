from flask import Flask, request, jsonify, render_template_string
import PyPDF2
from sentence_transformers import SentenceTransformer
import numpy as np
import faiss
import openai
import os

# OpenAI API 키 설정
openai.api_key = "api키"

# Flask 앱 생성
app = Flask(__name__)

# PDF 파일이 포함된 폴더 경로
pdf_folder_path = 'data/'

# 임베딩 저장 파일 경로
embedding_path = 'embeddings.npy'
index_path = 'faiss_index.bin'

# 폴더 내 모든 PDF 파일에서 텍스트를 추출하는 함수
def extract_text_from_folder(folder_path):
    text = ''
    for filename in os.listdir(folder_path):
        if filename.endswith('.pdf'):
            file_path = os.path.join(folder_path, filename)
            with open(file_path, 'rb') as file:
                reader = PyPDF2.PdfReader(file)
                for page in reader.pages:
                    text += page.extract_text()
    return text

# 텍스트를 청크로 나누는 함수
def chunk_text(text, chunk_size=500):
    words = text.split()
    return [" ".join(words[i:i + chunk_size]) for i in range(0, len(words), chunk_size)]

# 임베딩 생성 및 저장 함수
def create_and_save_embeddings(paragraphs, model, embedding_path, index_path):
    embeddings = model.encode(paragraphs)
    dimension = embeddings.shape[1]

    # 임베딩을 파일로 저장
    np.save(embedding_path, embeddings)

    # Faiss 인덱스 생성 및 저장
    index = faiss.IndexFlatL2(dimension)
    index.add(embeddings)
    faiss.write_index(index, index_path)

# 임베딩 및 인덱스 로드 함수
def load_embeddings_and_index(embedding_path, index_path):
    embeddings = np.load(embedding_path)
    index = faiss.read_index(index_path)
    return embeddings, index

# 폴더에서 텍스트를 추출
text = extract_text_from_folder(pdf_folder_path)

# 텍스트를 청크로 나눔
paragraphs = chunk_text(text)

# 임베딩 모델 로드
model = SentenceTransformer('all-MiniLM-L6-v2')

# 임베딩이 이미 존재하지 않으면 생성하고 저장
if not os.path.exists(embedding_path) or not os.path.exists(index_path):
    create_and_save_embeddings(paragraphs, model, embedding_path, index_path)

# 임베딩 및 인덱스 로드
embeddings, index = load_embeddings_and_index(embedding_path, index_path)

# 질문에 대한 답변 생성 함수
def get_answer(query, age=None, gender=None):
    query_embedding = model.encode([query])
    D, I = index.search(query_embedding, k=3)
    
    # 검색된 문단을 바탕으로 컨텍스트 생성
    context = "\n".join([paragraphs[idx] for idx in I[0]])
    
    # 사용자 정보 포함
    user_info = f"User age: {age}, User gender: {gender}.\n\n" if age and gender else ""
    
    # ChatGPT에게 질문과 컨텍스트를 전달
    response = openai.ChatCompletion.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": "You are a helpful assistant."},
            {
                "role": "user", 
                "content": (
                    f"{user_info}Based on the following information extracted from relevant documents, "
                    f"please provide a concise answer to the user's question.\n\n"
                    f"Context: {context}\n\n"
                    f"Question: {query}\n\n"
                    "Please ensure your answer is based on the provided context and is as brief and direct as possible.\n\n"
                    "If the question is not related to the information provided, please answer 'ask me any questions you have about the policy'. "
                    "If there is nothing relevant, please answer, 'I don't know such a thing.'\n\n"
                    "Please avoid unnecessary details and provide only the most relevant information.\n\n"
                    "If you need to provide a link in the answer, please provide it in the form of 'link: {link URL}' at the bottom."
                    "Please answer in Korean."
                )
            }
        ]
    )       
    
    return response['choices'][0]['message']['content']

# 웹 인터페이스를 위한 라우트 정의
@app.route('/', methods=['GET', 'POST'])
def home():
    if request.method == 'POST':
        question = request.form['question']
        if question:
            answer = get_answer(question)
            return render_template_string(html_template, question=question, answer=answer)
    return render_template_string(html_template)

@app.route('/ask', methods=['POST'])
def ask():
    data = request.get_json()
    question = data.get('question')
    age = data.get('age')
    gender = data.get('gender')

    if question:
        answer = get_answer(question, age, gender)
        
        # HTML 태그 제거
        clean_answer = re.sub('<[^<]+?>', '', answer)
        
        # 링크 추출
        links = re.findall(r'link: (https?://\S+)', clean_answer)
        clean_answer = re.sub(r'link: https?://\S+', '', clean_answer).strip()
        
        # 단락 분리
        paragraphs = [p.strip() for p in clean_answer.split('\n') if p.strip()]

        return jsonify({
            "answer": {
                "paragraphs": paragraphs,
                "links": links
            }
        })

    return jsonify({"error": "No question provided"}), 400

# 간단한 HTML 템플릿
html_template = '''
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Question Answering</title>
</head>
<body>
    <h1>Question Answering System</h1>
    <form method="POST">
        <label for="question">Enter your question:</label><br><br>
        <input type="text" id="question" name="question" style="width: 50%; padding: 10px;"><br><br>
        <input type="submit" value="Get Answer">
    </form>
    {% if question %}
        <h2>Question:</h2>
        <p>{{ question }}</p>  
        <h2>Answer:</h2>
        <p>{{ answer }}</p>
    {% endif %}
</body>
</html>
'''

# Flask 서버 실행
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
