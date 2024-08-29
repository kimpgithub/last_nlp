from flask import Flask, request, jsonify, render_template_string
from sentence_transformers import SentenceTransformer
import numpy as np
import faiss
import openai
import os
import re

# OpenAI API 키 설정
openai.api_key = "api키"

# Flask 앱 생성
app = Flask(__name__)

# 텍스트 파일이 포함된 루트 폴더 경로
root_folder_path = 'data/육아/'

# 임베딩 모델 로드
model = SentenceTransformer('all-MiniLM-L6-v2')

# 지역별 임베딩과 인덱스를 저장할 딕셔너리
embeddings_index_dict = {}

region_keywords = {
    # Regional divisions
    "강원특별자치도": ["강원도", "강원특별자치도", "춘천", "강릉", "원주"],
    "강원특별자치도 영월군": ["영월군", "영월"],
    "경기도": ["경기도", "수원", "고양", "성남", "과천", "광주", "군포", "동두천", "부천", "성남", "시흥", "안산", "양평", "여주", "이천", "평택"],
    "경기도 고양시": ["고양시", "고양"],
    "경기도 과천시": ["과천시", "과천"],
    "경기도 광주시": ["광주시", "광주(경기도)"],
    "경기도 군포시": ["군포시", "군포"],
    "경기도 동두천시": ["동두천시", "동두천"],
    "경기도 부천시": ["부천시", "부천"],
    "경기도 성남시": ["성남시", "성남"],
    "경기도 수원시": ["수원시", "수원"],
    "경기도 시흥시": ["시흥시", "시흥"],
    "경기도 안산시": ["안산시", "안산"],
    "경기도 양평군": ["양평군", "양평"],
    "경기도 여주시": ["여주시", "여주"],
    "경기도 이천시": ["이천시", "이천"],
    "경기도 평택시": ["평택시", "평택"],
    "경상남도 거제시": ["거제시", "거제"],
    "경상남도 거창군": ["거창군", "거창"],
    "경상남도 김해시": ["김해시", "김해"],
    "경상남도 하동군": ["하동군", "하동"],
    "경상북도 봉화군": ["봉화군", "봉화"],
    "경상북도 상주시": ["상주시", "상주"],
    "경상북도 의성군": ["의성군", "의성"],
    "경상북도 청송군": ["청송군", "청송"],
    "광주광역시": ["광주광역시", "광주"],
    "대구광역시 달서구": ["달서구", "달서", "대구 달서구"],
    "대구광역시 서구": ["서구", "대구 서구"],
    "부산광역시 수영구": ["수영구", "수영", "부산 수영구"],
    "서울특별시": ["서울특별시", "서울"],
    "서울특별시 강남구": ["강남구", "강남", "서울 강남구"],
    "서울특별시 강동구": ["강동구", "강동", "서울 강동구"],
    "서울특별시 강북구": ["강북구", "강북", "서울 강북구"],
    "서울특별시 강서구": ["강서구", "강서", "서울 강서구"],
    "서울특별시 관악구": ["관악구", "관악", "서울 관악구"],
    "서울특별시 광진구": ["광진구", "광진", "서울 광진구"],
    "서울특별시 구로구": ["구로구", "구로", "서울 구로구"],
    "서울특별시 금천구": ["금천구", "금천", "서울 금천구"],
    "서울특별시 노원구": ["노원구", "노원", "서울 노원구"],
    "서울특별시 도봉구": ["도봉구", "도봉", "서울 도봉구"],
    "서울특별시 동대문구": ["동대문구", "동대문", "서울 동대문구"],
    "서울특별시 동작구": ["동작구", "동작", "서울 동작구"],
    "서울특별시 마포구": ["마포구", "마포", "서울 마포구"],
    "서울특별시 서대문구": ["서대문구", "서대문", "서울 서대문구"],
    "서울특별시 서초구": ["서초구", "서초", "서울 서초구"],
    "서울특별시 성동구": ["성동구", "성동", "서울 성동구"],
    "서울특별시 성북구": ["성북구", "성북", "서울 성북구"],
    "서울특별시 송파구": ["송파구", "송파", "서울 송파구"],
    "서울특별시 양천구": ["양천구", "양천", "서울 양천구"],
    "서울특별시 영등포구": ["영등포구", "영등포", "서울 영등포구"],
    "서울특별시 용산구": ["용산구", "용산", "서울 용산구"],
    "서울특별시 은평구": ["은평구", "은평", "서울 은평구"],
    "서울특별시 종로구": ["종로구", "종로", "서울 종로구"],
    "서울특별시 중구": ["중구", "서울 중구"],
    "서울특별시 중랑구": ["중랑구", "중랑", "서울 중랑구"],
    "울산광역시 남구": ["울산 남구", "남구", "울산"],
    "인천광역시 계양구": ["계양구", "계양", "인천 계양구"],
    "인천광역시 서구": ["서구", "인천 서구"],
    "인천광역시 연수구": ["연수구", "연수", "인천 연수구"],
    "전라남도": ["전라남도", "전남"],
    "전라남도 강진군": ["강진군", "강진"],
    "전라남도 광양시": ["광양시", "광양"],
    "전라남도 나주시": ["나주시", "나주"],
    "전라남도 무안군": ["무안군", "무안"],
    "전라남도 영광군": ["영광군", "영광"],
    "전라남도 장성군": ["장성군", "장성"],
    "전라남도 화순군": ["화순군", "화순"],
    "전북특별자치도": ["전북특별자치도", "전북"],
    "전북특별자치도 익산시": ["익산시", "익산"],
    "제주특별자치도": ["제주특별자치도", "제주"],
    "충청남도 서산시": ["서산시", "서산"],
    "충청남도 아산시": ["아산시", "아산"],
    "충청남도 예산군": ["예산군", "예산"],
    "충청남도 천안시": ["천안시", "천안"],
    "충청남도 홍성군": ["홍성군", "홍성"],
    "충청북도 청주시": ["청주시", "청주"],

    # Government bodies and organizations
    "고용노동부": ["고용노동부", "노동부", "근로시간 단축", "육아휴직", "고용안정"],
    "보건복지부": ["보건복지부", "복지부", "모자보건", "장애인 부모교육", "보육료", "육아휴직급여", "입양아동"],
    "국립중앙의료원": ["국립중앙의료원", "중앙의료원", "의료원", "심리상담", "난임 부부"],
    "여성가족부": ["여성가족부", "여가부", "청소년 부모", "한부모", "다문화가정", "양육비"],
    "중소벤처기업부": ["중소벤처기업부", "중기부", "창업", "보육지원", "창업 보육센터"],
    "행정안전부": ["행정안전부", "행안부", "지방세 감면", "출산 서비스", "보육시설"],
    "한국건강가정진흥원": ["한국건강가정진흥원", "건강가정진흥원", "양육비", "가정지원"],
    "한국보육진흥원": ["한국보육진흥원", "보육진흥원", "보육교직원", "보육시설"],
    "국민건강보험공단": ["국민건강보험공단", "건강보험공단", "건강보험", "의료비", "육아휴직급여"],
    "국민연금공단": ["국민연금공단", "연금공단", "연금", "출산크레딧"],
    "통계청": ["통계청", "출산 통계", "보육 통계", "인구 통계"],
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

# 모든 지역별 폴더에 대해 임베딩 생성
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

# 질문에서 지역명을 추출하는 함수
def extract_region_from_question(question):
    for region, keywords in region_keywords.items():
        for keyword in keywords:
            if re.search(keyword, question):
                return region
    return None

def get_answer(query, age=None, gender=None):
    # 질문에서 지역 추출
    region = extract_region_from_question(query)
    
    if not region:
        # 지역이 탐지되지 않을 경우 전체 데이터에서 검색
        query_embedding = model.encode([query])
        D, I = all_index.search(query_embedding, k=3)
        
        if len(I[0]) == 0 or I[0][0] == -1:
            return "No relevant information found in the documents."
        
        valid_indices = [idx for idx in I[0] if idx >= 0 and idx < len(all_paragraphs)]
        
        if not valid_indices:
            return "No relevant information found in the documents."
        
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
        # 추가 고지: 지역을 질문에 추가하면 더 정확한 답변 가능
        return response['choices'][0]['message']['content'] + "\n\n" + (
            "참고로, 더 정확한 답변을 원하시면 질문에 해당 지역명을 추가해 주세요."
        )

    if region not in embeddings_index_dict:
        return "Could not determine the relevant region from the question, or no data available for the specified region."

    embeddings, index, paragraphs = embeddings_index_dict[region]
    
    query_embedding = model.encode([query])
    D, I = index.search(query_embedding, k=3)
    
    # 검색 결과 확인
    if len(I[0]) == 0 or I[0][0] == -1:
        return "No relevant information found in the documents."

    # 유효한 결과 확인
    valid_indices = [idx for idx in I[0] if idx >= 0 and idx < len(paragraphs)]
    
    if not valid_indices:
        return "No relevant information found in the documents."

    # 검색 결과를 바탕으로 컨텍스트 생성
    context = "\n".join([paragraphs[idx] for idx in valid_indices])
    
    # 사용자 정보
    user_info = f"User age: {age}, User gender: {gender}.\n\n" if age and gender else ""
    
    # ChatGPT 요청
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
    data = request.get_json()  # JSON 데이터를 추출
    question = data.get('question')  # 'question' 필드 추출
    age = data.get('age')  # 'age' 필드 추출
    gender = data.get('gender')  # 'gender' 필드 추출

    if question:
        answer = get_answer(question, age=age, gender=gender)
        return jsonify({"answer": answer})
    
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
