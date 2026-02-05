# OLLLIM Backend

> 목소리를 편지로, 마음을 전하다

음성 녹음을 텍스트 편지로 변환하고, AI 기반 폰트 추천 / 편지지 / BGM을 적용하여 감성적인 디지털 편지를 만들 수 있는 서비스입니다.

---

## 서비스 소개

### 핵심 기능

| 기능 | 설명                                    |
|------|---------------------------------------|
| **음성 편지 작성** | 음성 녹음 → Google STT로 텍스트 변환 → 편지 내용 편집 |
| **폰트 추천** | 음성 분석, 문맥 분석으로 편지에 어울리는 폰트 추천         |
| **편지 꾸미기** | 편지지 템플릿, BGM 적용으로 감성 편지 완성            |
| **편지 공유** | 링크 생성 + 비밀번호 설정으로 안전하게 편지 전달          |
| **편지함** | 보낸 편지 / 받은 편지 보관 및 관리                 |

### 서비스 흐름

```
음성 녹음 → STT 변환 → 내용 편집 → 폰트 추천 → 편지지 선택 → BGM 선택 → 저장 → 링크 공유
```

---

## 백엔드 팀원 소개

| 이름  | GitHub                                 | 역할 |
|-----|----------------------------------------|-------------------|
| 오세연 | [@oosedus](https://github.com/oosedus) | 인증/인가, 음성 편지 작성, 폰트 추천, 음성 분석, 편지 작성 |
| 김은지 | [@ej9374](https://github.com/ej9374)   | CI/CD, 문맥 분석, 편지 꾸미기, 편지함, 편지 공유 |

---

## 기술 스택

### Backend

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL_5.1-0769AD?style=for-the-badge&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL_8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis_7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

### Infra

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![AWS CodeDeploy](https://img.shields.io/badge/AWS_CodeDeploy-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Discord](https://img.shields.io/badge/Discord_Webhook-5865F2?style=for-the-badge&logo=discord&logoColor=white)

### External API

![Google Cloud](https://img.shields.io/badge/Google_STT-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white)
![Python](https://img.shields.io/badge/Python_분석서버-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Kakao](https://img.shields.io/badge/Kakao_OAuth-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)

## 프로젝트 구조

```
src/main/java/haennihaesseo/sandoll/
│
├── domain/                          # 도메인별 비즈니스 로직
│   ├── letter/                      # 편지 (핵심 도메인)
│   │   ├── controller/              #   API 엔드포인트
│   │   ├── service/                 #   비즈니스 로직
│   │   ├── repository/              #   DB 접근
│   │   ├── entity/                  #   JPA 엔티티
│   │   ├── cache/                   #   Redis 캐시 엔티티
│   │   ├── dto/                     #   요청/응답 DTO
│   │   ├── converter/               #   엔티티 ↔ DTO 변환
│   │   ├── exception/               #   도메인 예외
│   │   └── util/                    #   유틸 (AES 암호화 등)
│   │
│   ├── deco/                        # 꾸미기 (편지지, BGM)
│   ├── font/                        # 폰트 관리 및 추천
│   └── user/                        # 유저
│
└── global/                          # 공통 모듈
    ├── auth/                        #   인증/인가 (OAuth, JWT)
    ├── config/                      #   설정 (Security, S3, QueryDSL)
    ├── infra/                       #   외부 연동 (STT, Python, Discord)
    ├── exception/                   #   글로벌 예외 처리
    ├── response/                    #   공통 응답 래퍼
    └── status/                      #   공통 상태 코드
```
---

## ERD
<img width="500" alt="올림ERD" src="https://github.com/user-attachments/assets/88198727-e375-412c-9b55-87c4557c76f1" />


## 코드 컨벤션

### 브랜치 전략

```
main ← feat/#이슈번호
     ← fix/#이슈번호
     ← refactor/#이슈번호
     ← chore/#이슈번호
```

### 커밋 메시지

```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 코드 리팩토링
chore: 설정, 빌드 관련 변경
hotfix: 긴급 수정
```
