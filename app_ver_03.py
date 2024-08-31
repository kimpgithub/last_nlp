from flask import Flask, request, jsonify, render_template_string
from sentence_transformers import SentenceTransformer
import numpy as np
import faiss
import openai
import os
import re
import pickle
import json

# OpenAI API 키 설정
openai.api_key = "api"

# Flask 앱 생성
app = Flask(__name__)

# 텍스트 파일이 포함된 루트 폴더 경로
root_folder_path = 'data/육아/'

# 임베딩 모델 로드
model = SentenceTransformer('all-MiniLM-L6-v2')

# 지역별 임베딩과 인덱스를 저장할 딕셔너리
embeddings_index_dict = {}

# pickle 파일 경로
pickle_file_path = 'embeddings_index.pkl'

region_keywords = {
    # Regional divisions and other regions...
    "서울특별시": ["서울특별시", "서울"],
    # Other regions...
}

# 전체 텍스트 데이터를 저장할 변수
all_paragraphs = []

# 특정 폴더 내 모든 텍스트 파일에서 텍스트를 추출하는 함수
def extract_text_from_folder(folder_path):
    text_data = []
    file_paths = []
    for root, dirs, files in os.walk(folder_path):
        for filename in files:
            if filename.endswith('.txt'):
                file_path = os.path.join(root, filename)
                with open(file_path, 'r', encoding='utf-8') as file:
                    text = file.read()
                    text_data.append(text)
                    file_paths.append(file_path)
    return text_data, file_paths

# 텍스트를 청크로 나누는 함수
def chunk_text(text, chunk_size=500):
    words = text.split()
    return [" ".join(words[i:i + chunk_size]) for i in range(0, len(words), chunk_size)]

# 폴더 내 텍스트 파일로부터 임베딩과 인덱스를 생성 및 저장하는 함수
def create_and_save_embeddings_for_folder(folder_name, folder_path):
    texts, file_paths = extract_text_from_folder(folder_path)
    paragraphs = []
    for text in texts:
        paragraphs.extend(chunk_text(text))
    
    embeddings = model.encode(paragraphs)
    dimension = embeddings.shape[1]

    # Faiss 인덱스 생성
    index = faiss.IndexFlatL2(dimension)
    index.add(embeddings)

    # 딕셔너리에 저장
    embeddings_index_dict[folder_name] = (embeddings, index, paragraphs)
    
    # 전체 텍스트 데이터에 추가
    all_paragraphs.extend(paragraphs)

# 임베딩과 인덱스를 미리 생성하고 저장하는 함수
def initialize_embeddings_and_indices():
    if os.path.exists(pickle_file_path):
        with open(pickle_file_path, 'rb') as f:
            global embeddings_index_dict, all_paragraphs, all_embeddings, all_index
            embeddings_index_dict, all_paragraphs, all_embeddings, all_index = pickle.load(f)
    else:
        for folder_name in os.listdir(root_folder_path):
            folder_path = os.path.join(root_folder_path, folder_name)
            if os.path.isdir(folder_path):
                create_and_save_embeddings_for_folder(folder_name, folder_path)

        # 전체 데이터의 임베딩 생성
        if all_paragraphs:
            all_embeddings = model.encode(all_paragraphs)
            dimension = all_embeddings.shape[1]
            all_index = faiss.IndexFlatL2(dimension)
            all_index.add(all_embeddings)

        # 인덱스와 임베딩을 pickle 파일에 저장
        with open(pickle_file_path, 'wb') as f:
            pickle.dump((embeddings_index_dict, all_paragraphs, all_embeddings, all_index), f)

# 질문에서 지역 추출
def extract_region_from_question(question):
    question = question.lower()
    for region, keywords in region_keywords.items():
        for keyword in keywords:
            if re.search(keyword.lower(), question):
                return region
    return None

# 질문에 대한 답변 생성
def get_answer(query, age=None, gender=None):
    region = extract_region_from_question(query)
    from_server = False  # 기본값을 False로 설정
    policies = []  # 정책 리스트 초기화
    paragraphs = []
    links = []

    if not region:
        query_embedding = model.encode([query])
        D, I = all_index.search(query_embedding, k=3)
        
        if len(I[0]) == 0 or I[0][0] == -1:
            return {
                "answer": {
                    "paragraphs": ["No relevant information found in the documents."],
                    "links": links,
                    "policies": policies
                },
                "fromServer": from_server
            }
        
        valid_indices = [idx for idx in I[0] if idx >= 0 and idx < len(all_paragraphs)]
        
        if not valid_indices:
            return {
                "answer": {
                    "paragraphs": ["No relevant information found in the documents."],
                    "links": links,
                    "policies": policies
                },
                "fromServer": from_server
            }
        
        context = "\n".join([all_paragraphs[idx] for idx in valid_indices])
        user_info = f"User age: {age}, User gender: {gender}.\n\n" if age and gender else ""
        
        response = openai.ChatCompletion.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "You are a helpful assistant."},
                {
                    "role": "user", 
                    "content": (
                    f"{user_info}Based on the following information extracted from relevant documents, "
                    f"please provide a clear, well-organized, and visually distinct answer to the user's question.\n\n"
                    f"Context: {context}\n\n"
                    f"Question: {query}\n\n"
                    "Your answer should be concise and use bullet points, numbered lists, or other formatting techniques to ensure each section is visually distinct. "
                    "Additionally, please create a list of the policy titles or key items in your response, and format this list as a simple JSON array at the end of your answer under the key 'policies'.\n\n"
                    "Use symbols such as asterisks (*), dashes (-), or brackets [ ] to clearly separate different parts of the answer, such as different policies or steps.\n\n"
                    "If you need to include a link in the answer, format it as: [link text](link: {link URL}).\n\n"
                    "Please ensure the answer is clear, direct, and well-structured to help the user easily understand the information.\n\n"
                    "Please respond in Korean."
                    )
                }
            ]
        )
        
        from_server = True  # 응답을 생성했다면 fromServer 값을 True로 설정
        content = response['choices'][0]['message']['content']
        paragraphs = [content]
        
        # 정책 목록을 추출하는 정규 표현식
        policies_match = re.search(r'```json\s*({[^}]+})', content, re.DOTALL)
        if policies_match:
            policies_json = json.loads(policies_match.group(1))
            policies = policies_json.get('policies', [])
        
        return {
            "answer": {
                "paragraphs": paragraphs,
                "links": links,
                "policies": policies
            },
            "fromServer": from_server
        }

    if region not in embeddings_index_dict:
        return {
            "answer": {
                "paragraphs": ["Could not determine the relevant region from the question, or no data available for the specified region."],
                "links": links,
                "policies": policies
            },
            "fromServer": from_server
        }

    embeddings, index, paragraphs_data = embeddings_index_dict[region]
    
    query_embedding = model.encode([query])
    D, I = index.search(query_embedding, k=3)
    
    if len(I[0]) == 0 or I[0][0] == -1:
        return {
            "answer": {
                "paragraphs": ["No relevant information found in the documents."],
                "links": links,
                "policies": policies
            },
            "fromServer": from_server
        }

    valid_indices = [idx for idx in I[0] if idx >= 0 and idx < len(paragraphs_data)]
    
    if not valid_indices:
        return {
            "answer": {
                "paragraphs": ["No relevant information found in the documents."],
                "links": links,
                "policies": policies
            },
            "fromServer": from_server
        }

    context = "\n".join([paragraphs_data[idx] for idx in valid_indices])
    
    user_info = f"User age: {age}, User gender: {gender}.\n\n" if age and gender else ""
    
    response = openai.ChatCompletion.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": "You are a helpful assistant."},
            {
                "role": "user", 
                "content": (
                f"{user_info}Based on the following information extracted from relevant documents, "
                f"please provide a clear, well-organized, and visually distinct answer to the user's question.\n\n"
                f"Context: {context}\n\n"
                f"Question: {query}\n\n"
                "Your answer should be concise and use bullet points, numbered lists, or other formatting techniques to ensure each section is visually distinct. "
                "Additionally, please create a list of the policy titles or key items in your response, and format this list as a simple JSON array at the end of your answer under the key 'policies'.\n\n"
                "Use symbols such as asterisks (*), dashes (-), or brackets [ ] to clearly separate different parts of the answer, such as different policies or steps.\n\n"
                "If the question is not directly related to the provided information, please answer with 'ask me any questions you have about the policy'. "
                "If no relevant information is found, please respond with 'I don't know such a thing.'\n\n"
                "Avoid unnecessary details and focus only on the most relevant information.\n\n"
                "If you need to include a link in the answer, provide it in the format 'link: {link URL}' at the end."
                "Please respond in Korean."
                )
            }
        ]
    )
    
    from_server = True  # 응답을 생성했다면 fromServer 값을 True로 설정
    content = response['choices'][0]['message']['content']
    paragraphs = [content]

    # 정책 목록을 추출하는 정규 표현식
    policies_match = re.search(r'```json\s*({[^}]+})', content, re.DOTALL)
    if policies_match:
        policies_json = json.loads(policies_match.group(1))
        policies = policies_json.get('policies', [])
    
    return {
        "answer": {
            "paragraphs": paragraphs,
            "links": links,
            "policies": policies
        },
        "fromServer": from_server
    }

@app.route('/', methods=['GET', 'POST'])
def home():
    if request.method == 'POST':
        question = request.form['question']
        if question:
            answer_dict = get_answer(question)
            
            # JSON 형태의 답변을 올바르게 렌더링하기
            formatted_answer = json.dumps(answer_dict, indent=4, ensure_ascii=False)
            
            # 답변의 길이를 제한하여 웹에서 보기 쉽게 함
            short_answer = formatted_answer[:2000]  # 응답의 앞 2000글자만 가져오기
            return render_template_string(html_template, question=question, answer=short_answer)
    
    return render_template_string(html_template)

@app.route('/ask', methods=['POST'])
def ask():
    data = request.get_json()
    question = data.get('question')
    age = data.get('age')
    gender = data.get('gender')

    if question:
        answer = get_answer(question, age=age, gender=gender)
        return jsonify(answer)
    
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
    initialize_embeddings_and_indices()
    app.run(host='0.0.0.0', port=5000, debug=True)
