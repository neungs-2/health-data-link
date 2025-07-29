# 헬스케어 데이터 연동 과제

## 0. 코드 실행
1. Repository Clone
```shell
git clone https://github.com/neungs-2/health-data-link.git
```
2. Docker 실행 (Docker Compose)
```shell
cd ./health-data-link
docker-compose up -d
```
3. Application code 실행

<br>

---
## 1. 데이터베이스 설계 ERD

![ERD 이미지](https://github.com/user-attachments/assets/6a893ca1-d479-4beb-a4a9-39c2f2b97d6b)

### 1.1 Table List
* **steps_record**
  * 개별 data entry 정보
  * PK : step_record_id (auto increment)
  * FK : recordkey, user_id (source 테이블 참조)
  * Unique : recordkey, user_id, period_from, period_to 조합
    * 유일성을 보장할 수 있는 조합 필드가 너무 많다고 판단
    * 코드 및 쿼리가 너무 복잡해지고 데이터 식별 가독성 향상을 위해 별도 PK 설정

  <br>

* **monthly_summary**
  * recordkey 단위의 월별 통계
  * PK : user_id, recordkey, date (복합키)
  * FK : user_id (user 테이블 참조)
  * date는 'YYYY-MM' 형식으로 저장

  <br>

* **daily_summary**
  * recordkey 단위의 일별 통계
  * PK : user_id, recordkey, date (복합키)
  * FK : user_id (user 테이블 참조)
  * date는 'YYYY-MM-DD' 형식으로 저장

  <br>

* **source**
  * recordkey 별 Data Source 메타 정보
  * PK : recordkey, user_id (복합키)
  * FK : user_id (user 테이블 참조)

  <br>

* **user**
  * 서비스 사용자
  * PK : user_id (auto increment)

  <br>

### 1.2 설계 포인트

* User 삭제 시 Cascade가 가능하도록 FK 설계
* 식별 가능한 필드 조합이 3개가 넘어가는 경우 별도의 PK를 설정
* 실수형 데이터는 고정 소수점 방식(Decimal)을 사용하여 정확한 소수점 표현
* 고정된 크기의 문자열 char 타입 사용
* timestamp 타입을 사용하여 일관된 시간대로 저장
  * 서버와 DB 시간대는 KST로 통일

  <br>

### 1.3 DDL
```mysql
CREATE TABLE user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE source (
recordkey CHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(100),
    product_name VARCHAR(100),
    product_vender VARCHAR(100),
    mode INT,
    type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (recordkey, user_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE steps_record (
    step_record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recordkey CHAR(36) NOT NULL,
    steps INT,
    distance DECIMAL(20, 10),
    calories DECIMAL(20, 10),
    period_from TIMESTAMP NOT NULL,
    period_to TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recordkey, user_id) REFERENCES source(recordkey, user_id),
    UNIQUE KEY uq_steps_record_user_record_period (user_id, recordkey, period_from, period_to)
);

CREATE TABLE daily_summary (
    user_id BIGINT NOT NULL,
    recordkey CHAR(36) NOT NULL,
    date DATE NOT NULL,
    steps INT,
    calories DECIMAL(20, 10),
    distance DECIMAL(20, 10),
    timezone CHAR(6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, date),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE monthly_summary (
    user_id BIGINT NOT NULL,
    recordkey CHAR(36) NOT NULL,
    date CHAR(7) NOT NULL,
    steps INT,
    calories DECIMAL(20, 10),
    distance DECIMAL(20, 10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, date),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

# 유저 관련 API는 없기 때문에 테스트 유저 직접 Insert
INSERT INTO user (name, email) values ('tester', 'tester@email.com')
```

<br>

---
## 2. Application 코드 핵심 사항 (샘플 코드 코멘트)

### 2.1 API Spec
#### 걸음 수 데이터 저장
- **URL**: POST /api/health-records/steps
- **Headers**:
  - userId: Long (사용자 식별자)
- **Request Body**:
```json
{
  "recordkey": "uuid-string",
  "steps": 1000,
  "distance": 0.8,
  "calories": 50.5,
  "period_from": "2024-01-01T00:00:00",
  "period_to": "2024-01-01T23:59:59"
}
```
- **Response**:
```json
{
  "success": true,
  "data": true,
  "message": "요청이 성공적으로 처리되었습니다."
}
```
- **특이사항**:
  - 멱등성 보장을 위한 Redis 키 사용
  - 동일 recordkey + period 조합 중복 방지

#### 일별 데이터 조회
- **URL**: GET /api/health-records/daily-summaries/{recordkey}
- **Headers**:
  - userId: Long (사용자 식별자)
- **Query Parameters**:
  - timezone: String (기본값: "+09:00")
- **Response**:
```json
{
  "success": true,
  "data": [
    {
      "daily": "2024-11-15",
      "steps": 5585,
      "calories": 203.21,
      "distance": 4.26,
      "recordkey": "uuid-string",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 월별 데이터 조회
- **URL**: GET /api/health-records/monthly-summaries/{recordkey}
- **Headers**:
  - userId: Long (사용자 식별자)
- **Query Parameters**:
  - timezone: String (기본값: "+09:00")
- **Response**:
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-11",
      "steps": 129021,
      "calories": 4607.72,
      "distance": 99.25,
      "recordkey": "uuid-string",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```

  <br>

### 2.2 계층 구조

```
.
├── java
│   └── com
│       └── healthcare
│           └── link
│               ├── LinkApplication.java
│               ├── common
│               │   ├── aspect
│               │   │   ├── TimezoneOffset.java
│               │   │   └── TimezoneOffsetValidator.java
│               │   ├── constant
│               │   │   └── DateTimeConstant.java
│               │   ├── deserializer
│               │   │   ├── StepsDeserializer.java
│               │   │   └── ZonedDateTimeDeserializer.java
│               │   ├── enums
│               │   │   ├── HealthRecordType.java
│               │   │   └── MeasurementUnit.java
│               │   ├── error
│               │   │   ├── ErrorCode.java
│               │   │   ├── GlobalExceptionHandler.java
│               │   │   └── exception
│               │   │       ├── AccessDeniedException.java
│               │   │       ├── BadRequestException.java
│               │   │       ├── BaseException.java
│               │   │       ├── InternalParamException.java
│               │   │       └── ResourceNotFoundException.java
│               │   ├── redis
│               │   │   └── RedisHandler.java
│               │   └── response
│               │       ├── ApiResponse.java
│               │       └── ErrorResponse.java
│               ├── config
│               │   ├── JpaConfiguration.java
│               │   ├── RedisConfiguration.java
│               │   └── SwaggerConfiguration.java
│               ├── controller
│               │   └── HealthRecordController.java
│               ├── domain
│               │   ├── entity
│               │   │   ├── DailySummary.java
│               │   │   ├── MonthlySummary.java
│               │   │   ├── Source.java
│               │   │   ├── StepsRecord.java
│               │   │   └── User.java
│               │   └── vo
│               │       ├── DailySummaryId.java
│               │       ├── MonthlySummaryId.java
│               │       ├── SourceId.java
│               │       └── StepsRecordId.java
│               ├── dto
│               │   ├── StepsEntrySumDto.java
│               │   ├── StepsEntryValueDto.java
│               │   ├── cache
│               │   │   ├── DailySummaryResponseListCacheDto.java
│               │   │   └── MonthlySummaryResponseListCacheDto.java
│               │   ├── request
│               │   │   └── StepsRecordRequestDto.java
│               │   ├── response
│               │   │   ├── DailySummaryResponseDto.java
│               │   │   └── MonthlySummaryResponseDto.java
│               │   └── value
│               │       ├── Calory.java
│               │       └── Distance.java
│               ├── mapper
│               │   └── HealthRecordMapper.java
│               ├── repository
│               │   ├── DailySummaryRepository.java
│               │   ├── MonthlySummaryRepository.java
│               │   ├── SourceRepository.java
│               │   ├── StepsRecordRepository.java
│               │   └── UserRepository.java
│               └── service
│                   ├── HealthRecordService.java
│                   └── cache
│                       └── HealthRecordCacheService.java
└── resources
    ├── application.yml
    ├── logback-spring.xml
    ├── static
    └── templates

```

#### Controller Layer
- Swagger API 문서화 
- 요청 유효성 검증
- 공통 응답 형식 사용 (`ApiResponse<T>`)

#### Service Layer
- 비즈니스 로직 및 트랜잭션과 캐싱 로직 분리

- `HealthRecordService`: 코어 비즈니스 로직
  - 데이터 집계 및 통계 처리
  - 트랜잭션 관리

- `HealthRecordCacheService`: 캐시 관리 전용 서비스
  - Redis 캐싱 전략 구현
  - 멱등성 체크 로직

#### Repository Layer
- JPA Repository 활용

  <br>

### 2.3 Redis 사용 전략
#### 캐싱
- 일별/월별 집계 데이터 캐시
  - Key 구조: `cache:[daily/monthly]:{recordkey}:{userId}:{timezone}`
  - Value: 집계된 통계 데이터 (JSON)
  - TTL 설정으로 자동 만료 관리
  - 캐시 히트 시 TTL 리셋
  - `recordkey` 기준이므로 동일 유저 Step Record 추가시에도 캐시 무효화 불필요

#### 멱등성 키
- key 구조: `idempotent:{recordkey}:{userId}`
- TTL 적용으로 자동 만료 관리
- 중복 요청 체크 방식
  - 키 존재 여부로 중복 판단

  <br>

### 2.4 엔티티
- 핵심 엔티티
  - StepsRecord: 원본 데이터 저장
  - DailySummary: 일별 집계 데이터
  - MonthlySummary: 월별 집계 데이터

- 복합키 사용시 isNew 오버라이딩으로 `created_at`을 신규 엔티티 기준으로 변경 

  <br>

### 2.5 Validation & Deserialization
- API 요청 검증
  - `@TimezoneOffset`: 커스텀 타임존 포맷 검증
  - DTO 필드 유효성 검증 (`@Valid`)

- 역직렬화
  - Steps 데이터 중 소수는 반올림 처리
  - 다양한 포멧의 시간 데이터를 파싱하도록 처리

<br>

---
## 3. 데이터 조회 결과 제출 (Daily/Monthly 레코드키 기준)
데이터는 KST 기준으로 변경하여 집계하였습니다.

#### Daily 통계
| date       | steps  | calories      | distance      | recordkey                             |
|------------|--------|--------------|---------------|---------------------------------------|
| 2024-11-15 | 5,585  | 203.20997485 | 4.2552300822  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-16 | 5,834  | 203.08995558 | 4.3962200648  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-17 | 5,053  | 199.88999262 | 4.2531403435  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-18 | 8,290  | 302.44995739 | 6.3551000018  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-19 | 12,798 | 440.79991546 | 9.9276002419  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-20 | 10,870 | 378.70994269 | 8.3883900876  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-21 | 8,036  | 284.7799332  | 6.1511402794  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-22 | 8,531  | 296.97997455 | 6.52133051    | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-23 | 5,910  | 205.21996798 | 4.51164055    | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-24 | 6,204  | 225.22995818 | 4.835489983   | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-25 | 10,108 | 378.87995035 | 7.8014101875  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-26 | 9,023  | 318.91992976 | 6.9471501185  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-27 | 8,300  | 306.3899532  | 6.3299302095  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-28 | 9,277  | 330.97996757 | 6.9950002598  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-29 | 10,965 | 381.24998201 | 8.372650117   | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-30 | 4,237  | 150.9399852  | 3.2106700806  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-01 | 3,112  | 105.04999741 | 2.389550015   | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-02 | 10,257 | 355.90997034 | 7.9942501644  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-03 | 9,670  | 342.9799666  | 7.356209992   | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-04 | 9,675  | 335.48997871 | 7.465789973   | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-05 | 7,312  | 266.7599777  | 5.5457200657  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-06 | 12,528 | 450.1999618  | 9.50484002    | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-07 | 8,020  | 265.66992305 | 6.2201906806  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-08 | 5,710  | 240.64999271 | 4.9801598265  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-09 | 5,961  | 199.56998688 | 4.6749399978  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-10 | 8,269  | 285.4299928  | 6.4333500641  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-11 | 10,837 | 363.21997286 | 8.4244499127  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-12 | 11,997 | 405.45994939 | 9.2257299195  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-13 | 8,547  | 291.51997744 | 6.5950798903  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-14 | 4,991  | 170.98999117 | 3.8090099027  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-15 | 4,027  | 139.34999349 | 3.0840801035  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-16 | 8,117  | 284.57998702 | 6.21411995    | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12-17 | 3,445  | 121.34998031 | 2.6466300657  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11-15 | 4,067  | 160.57997378 | 3.046929949   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-16 | 9,985  | 401.88993317 | 7.4613995058  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-17 | 7,526  | 292.78998665 | 5.6888298164  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-18 | 6,211  | 245.70996514 | 4.69842976    | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-19 | 7,310  | 291.0299817  | 5.564409816   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-20 | 8,916  | 355.37994482 | 6.7491692657  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-21 | 5,011  | 199.5199792  | 3.8204598033  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-22 | 8,886  | 354.20994398 | 6.69971936    | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-23 | 9,327  | 373.2799468  | 7.0999090993  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-24 | 5,929  | 233.18998178 | 4.4994300913  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-25 | 6,266  | 251.0499723  | 4.800279619   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-26 | 5,956  | 241.0999739  | 4.5414197148  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-27 | 10,696 | 449.01995484 | 8.1071797007  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-28 | 11,458 | 466.25995831 | 8.6163795364  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-29 | 7,289  | 287.2999758  | 5.468020042   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-30 | 6,401  | 255.83998347 | 4.8069598657  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-01 | 8,553  | 341.1599811  | 6.435619776   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-02 | 3,855  | 160.5399942  | 2.975449873   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-03 | 7,383  | 296.66996065 | 5.6127396117  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-04 | 9,193  | 367.4199907  | 6.8924298775  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-05 | 16,850 | 675.05993647 | 12.659619611  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-06 | 6,241  | 250.15997317 | 4.6885497512  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-07 | 7,766  | 314.8999657  | 5.907709465   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-08 | 4,452  | 175.43998166 | 3.349169735   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-09 | 3,518  | 138.87998289 | 2.6473498337  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-10 | 9,098  | 365.089965   | 6.9290499047  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-11 | 8,876  | 358.00997353 | 6.6786299217  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-12 | 4,772  | 194.21997784 | 3.607729775   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-13 | 9,849  | 396.32992307 | 7.4522593442  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-14 | 6,018  | 239.94998092 | 4.6005097036  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-15 | 4,639  | 185.299993   | 3.5155399415  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-16 | 5,795  | 230.9999884  | 4.358619853   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12-17 | 2,283  | 90.05999491  | 1.7385299377  | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11-15 | 7,543  | 0            | 6.0339703213  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-16 | 6,629  | 0            | 5.303650701   | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-17 | 1,708  | 0            | 1.3664        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-18 | 11,119 | 0            | 8.8955379178  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-19 | 7,676  | 0            | 6.141942546   | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-20 | 6,348  | 0            | 5.0784        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-21 | 7,916  | 0            | 6.3328        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-22 | 9,376  | 0            | 7.5008        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-23 | 8,940  | 0            | 7.1508162151  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-24 | 4,492  | 0            | 3.592         | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-25 | 7,187  | 0            | 5.7509897118  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-26 | 8,681  | 0            | 6.9433310806  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-27 | 7,562  | 0            | 6.0529631552  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-28 | 9,044  | 0            | 7.2344        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-29 | 8,230  | 0            | 6.5849473173  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-30 | 3,504  | 0            | 2.8033718243  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-01 | 7,117  | 0            | 5.6936        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-02 | 5,838  | 0            | 4.6714408306  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-03 | 9,475  | 0            | 7.58          | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-04 | 7,856  | 0            | 6.285210235   | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-05 | 7,511  | 0            | 6.008         | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-06 | 7,137  | 0            | 5.7084561189  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-07 | 12,909 | 0            | 10.3277558245 | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-08 | 3,484  | 0            | 2.7869587845  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-09 | 6,455  | 0            | 5.164         | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-10 | 7,980  | 0            | 6.3839972788  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-11 | 8,550  | 0            | 6.8402767838  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-12 | 8,696  | 0            | 6.9576        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-13 | 9,223  | 0            | 7.3784207815  | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-14 | 5,749  | 0            | 4.5992        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12-15 | 5,902  | 0            | 4.7208        | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11-15 | 6,672  | 0            | 5.3376        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-16 | 12,449 | 0            | 9.96          | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-17 | 233    | 0            | 0.1864        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-18 | 13,602 | 0            | 10.8808       | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-19 | 12,193 | 0            | 9.7536        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-20 | 5,402  | 0            | 4.3208        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-21 | 9,570  | 0            | 7.6552        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-22 | 8,612  | 0            | 6.8904        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-23 | 8,243  | 0            | 6.5944        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-24 | 8      | 0            | 0.0064        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-25 | 12,802 | 0            | 10.2408       | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-26 | 8,143  | 0            | 6.5136        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-27 | 6,673  | 0            | 5.3376        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-28 | 7,839  | 0            | 6.272         | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-29 | 10,896 | 0            | 8.7168        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-11-30 | 12,912 | 0            | 10.3296       | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-01 | 35     | 0            | 0.028         | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-02 | 12,039 | 0            | 9.6312        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-03 | 6,953  | 0            | 5.5624        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-04 | 14,534 | 0            | 11.6264       | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-05 | 10,817 | 0            | 8.6536        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-06 | 10,765 | 0            | 8.6128        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-07 | 7,486  | 0            | 5.988         | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-08 | 12,306 | 0            | 9.8456        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-09 | 10,958 | 0            | 8.7664        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-10 | 12,939 | 0            | 10.3504       | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-11 | 12,589 | 0            | 10.0712       | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-12 | 8,808  | 0            | 7.0464        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-13 | 10,484 | 0            | 8.388         | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-14 | 5,351  | 0            | 4.28          | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12-15 | 788    | 0            | 0.6304        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |

#### Monthly 통계
| date     | steps   | calories        | distance        | recordkey                             |
|----------|---------|----------------|-----------------|---------------------------------------|
| 2024-11  | 129,021 | 4607.71934059  | 99.2520931171   | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-12  | 132,475 | 4624.17959968  | 102.5641005435  | 3b87c9a4-f983-4168-8f27-85436447bb57 |
| 2024-11  | 121,234 | 4858.14945564  | 91.6689249454   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-12  | 119,141 | 4780.18956321  | 90.0495059155   | 7836887b-b12a-440f-af0f-851546504b13 |
| 2024-11  | 115,955 | 0              | 92.7663207905   | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-12  | 113,882 | 0              | 91.1057166377   | 7b012e6e-ba2b-49c7-bc2e-473b7b58e72e |
| 2024-11  | 136,249 | 0              | 108.996         | e27ba7ef-8bb2-424c-af1d-877e826b7487 |
| 2024-12  | 136,852 | 0              | 109.4808        | e27ba7ef-8bb2-424c-af1d-877e826b7487 |

<br>

## 결과 데이터 원본
### Data1
- recordkey: "3b87c9a4-f983-4168-8f27-85436447bb57"

#### Daily 기준
```json
{
  "success": true,
  "data": [
    {
      "daily": "2024-11-15",
      "steps": 5585,
      "calories": 203.20997485,
      "distance": 4.2552300822,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-16",
      "steps": 5834,
      "calories": 203.08995558,
      "distance": 4.3962200648,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-17",
      "steps": 5053,
      "calories": 199.88999262,
      "distance": 4.2531403435,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-18",
      "steps": 8290,
      "calories": 302.44995739,
      "distance": 6.3551000018,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-19",
      "steps": 12798,
      "calories": 440.79991546,
      "distance": 9.9276002419,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-20",
      "steps": 10870,
      "calories": 378.70994269,
      "distance": 8.3883900876,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-21",
      "steps": 8036,
      "calories": 284.7799332,
      "distance": 6.1511402794,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-22",
      "steps": 8531,
      "calories": 296.97997455,
      "distance": 6.52133051,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-23",
      "steps": 5910,
      "calories": 205.21996798,
      "distance": 4.51164055,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-24",
      "steps": 6204,
      "calories": 225.22995818,
      "distance": 4.835489983,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-25",
      "steps": 10108,
      "calories": 378.87995035,
      "distance": 7.8014101875,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-26",
      "steps": 9023,
      "calories": 318.91992976,
      "distance": 6.9471501185,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-27",
      "steps": 8300,
      "calories": 306.3899532,
      "distance": 6.3299302095,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-28",
      "steps": 9277,
      "calories": 330.97996757,
      "distance": 6.9950002598,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-29",
      "steps": 10965,
      "calories": 381.24998201,
      "distance": 8.372650117,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-30",
      "steps": 4237,
      "calories": 150.9399852,
      "distance": 3.2106700806,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-01",
      "steps": 3112,
      "calories": 105.04999741,
      "distance": 2.389550015,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-02",
      "steps": 10257,
      "calories": 355.90997034,
      "distance": 7.9942501644,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-03",
      "steps": 9670,
      "calories": 342.9799666,
      "distance": 7.356209992,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-04",
      "steps": 9675,
      "calories": 335.48997871,
      "distance": 7.465789973,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-05",
      "steps": 7312,
      "calories": 266.7599777,
      "distance": 5.5457200657,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-06",
      "steps": 12528,
      "calories": 450.1999618,
      "distance": 9.50484002,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-07",
      "steps": 8020,
      "calories": 265.66992305,
      "distance": 6.2201906806,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-08",
      "steps": 5710,
      "calories": 240.64999271,
      "distance": 4.9801598265,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-09",
      "steps": 5961,
      "calories": 199.56998688,
      "distance": 4.6749399978,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-10",
      "steps": 8269,
      "calories": 285.4299928,
      "distance": 6.4333500641,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-11",
      "steps": 10837,
      "calories": 363.21997286,
      "distance": 8.4244499127,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-12",
      "steps": 11997,
      "calories": 405.45994939,
      "distance": 9.2257299195,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-13",
      "steps": 8547,
      "calories": 291.51997744,
      "distance": 6.5950798903,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-14",
      "steps": 4991,
      "calories": 170.98999117,
      "distance": 3.8090099027,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-15",
      "steps": 4027,
      "calories": 139.34999349,
      "distance": 3.0840801035,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-16",
      "steps": 8117,
      "calories": 284.57998702,
      "distance": 6.21411995,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-17",
      "steps": 3445,
      "calories": 121.34998031,
      "distance": 2.6466300657,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```


#### Monthly 기준
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-11",
      "steps": 129021,
      "calories": 4607.71934059,
      "distance": 99.2520931171,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "date": "2024-12",
      "steps": 132475,
      "calories": 4624.17959968,
      "distance": 102.5641005435,
      "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}


```

<br>

### Data2
- recordkey: "7836887b-b12a-440f-af0f-851546504b13"

#### Daily 기준
```json
{
  "success": true,
  "data": [
    {
      "daily": "2024-11-15",
      "steps": 4067,
      "calories": 160.57997378,
      "distance": 3.046929949,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-16",
      "steps": 9985,
      "calories": 401.88993317,
      "distance": 7.4613995058,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-17",
      "steps": 7526,
      "calories": 292.78998665,
      "distance": 5.6888298164,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-18",
      "steps": 6211,
      "calories": 245.70996514,
      "distance": 4.69842976,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-19",
      "steps": 7310,
      "calories": 291.0299817,
      "distance": 5.564409816,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-20",
      "steps": 8916,
      "calories": 355.37994482,
      "distance": 6.7491692657,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-21",
      "steps": 5011,
      "calories": 199.5199792,
      "distance": 3.8204598033,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-22",
      "steps": 8886,
      "calories": 354.20994398,
      "distance": 6.69971936,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-23",
      "steps": 9327,
      "calories": 373.2799468,
      "distance": 7.0999090993,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-24",
      "steps": 5929,
      "calories": 233.18998178,
      "distance": 4.4994300913,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-25",
      "steps": 6266,
      "calories": 251.0499723,
      "distance": 4.800279619,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-26",
      "steps": 5956,
      "calories": 241.0999739,
      "distance": 4.5414197148,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-27",
      "steps": 10696,
      "calories": 449.01995484,
      "distance": 8.1071797007,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-28",
      "steps": 11458,
      "calories": 466.25995831,
      "distance": 8.6163795364,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-29",
      "steps": 7289,
      "calories": 287.2999758,
      "distance": 5.468020042,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-30",
      "steps": 6401,
      "calories": 255.83998347,
      "distance": 4.8069598657,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-01",
      "steps": 8553,
      "calories": 341.1599811,
      "distance": 6.435619776,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-02",
      "steps": 3855,
      "calories": 160.5399942,
      "distance": 2.975449873,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-03",
      "steps": 7383,
      "calories": 296.66996065,
      "distance": 5.6127396117,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-04",
      "steps": 9193,
      "calories": 367.4199907,
      "distance": 6.8924298775,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-05",
      "steps": 16850,
      "calories": 675.05993647,
      "distance": 12.659619611,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-06",
      "steps": 6241,
      "calories": 250.15997317,
      "distance": 4.6885497512,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-07",
      "steps": 7766,
      "calories": 314.8999657,
      "distance": 5.907709465,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-08",
      "steps": 4452,
      "calories": 175.43998166,
      "distance": 3.349169735,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-09",
      "steps": 3518,
      "calories": 138.87998289,
      "distance": 2.6473498337,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-10",
      "steps": 9098,
      "calories": 365.089965,
      "distance": 6.9290499047,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-11",
      "steps": 8876,
      "calories": 358.00997353,
      "distance": 6.6786299217,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-12",
      "steps": 4772,
      "calories": 194.21997784,
      "distance": 3.607729775,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-13",
      "steps": 9849,
      "calories": 396.32992307,
      "distance": 7.4522593442,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-14",
      "steps": 6018,
      "calories": 239.94998092,
      "distance": 4.6005097036,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-15",
      "steps": 4639,
      "calories": 185.299993,
      "distance": 3.5155399415,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-16",
      "steps": 5795,
      "calories": 230.9999884,
      "distance": 4.358619853,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-17",
      "steps": 2283,
      "calories": 90.05999491,
      "distance": 1.7385299377,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```


#### Monthly 기준
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-11",
      "steps": 121234,
      "calories": 4858.14945564,
      "distance": 91.6689249454,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "date": "2024-12",
      "steps": 119141,
      "calories": 4780.18956321,
      "distance": 90.0495059155,
      "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```

<br>

### Data3
- recordkey: "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e"

#### Daily 기준
```json
{
  "success": true,
  "data": [
    {
      "daily": "2024-11-15",
      "steps": 7543,
      "calories": 0,
      "distance": 6.0339703213,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-16",
      "steps": 6629,
      "calories": 0,
      "distance": 5.303650701,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-17",
      "steps": 1708,
      "calories": 0,
      "distance": 1.3664,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-18",
      "steps": 11119,
      "calories": 0,
      "distance": 8.8955379178,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-19",
      "steps": 7676,
      "calories": 0,
      "distance": 6.141942546,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-20",
      "steps": 6348,
      "calories": 0,
      "distance": 5.0784,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-21",
      "steps": 7916,
      "calories": 0,
      "distance": 6.3328,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-22",
      "steps": 9376,
      "calories": 0,
      "distance": 7.5008,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-23",
      "steps": 8940,
      "calories": 0,
      "distance": 7.1508162151,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-24",
      "steps": 4492,
      "calories": 0,
      "distance": 3.592,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-25",
      "steps": 7187,
      "calories": 0,
      "distance": 5.7509897118,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-26",
      "steps": 8681,
      "calories": 0,
      "distance": 6.9433310806,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-27",
      "steps": 7562,
      "calories": 0,
      "distance": 6.0529631552,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-28",
      "steps": 9044,
      "calories": 0,
      "distance": 7.2344,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-29",
      "steps": 8230,
      "calories": 0,
      "distance": 6.5849473173,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-30",
      "steps": 3504,
      "calories": 0,
      "distance": 2.8033718243,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-01",
      "steps": 7117,
      "calories": 0,
      "distance": 5.6936,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-02",
      "steps": 5838,
      "calories": 0,
      "distance": 4.6714408306,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-03",
      "steps": 9475,
      "calories": 0,
      "distance": 7.58,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-04",
      "steps": 7856,
      "calories": 0,
      "distance": 6.285210235,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-05",
      "steps": 7511,
      "calories": 0,
      "distance": 6.008,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-06",
      "steps": 7137,
      "calories": 0,
      "distance": 5.7084561189,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-07",
      "steps": 12909,
      "calories": 0,
      "distance": 10.3277558245,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-08",
      "steps": 3484,
      "calories": 0,
      "distance": 2.7869587845,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-09",
      "steps": 6455,
      "calories": 0,
      "distance": 5.164,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-10",
      "steps": 7980,
      "calories": 0,
      "distance": 6.3839972788,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-11",
      "steps": 8550,
      "calories": 0,
      "distance": 6.8402767838,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-12",
      "steps": 8696,
      "calories": 0,
      "distance": 6.9576,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-13",
      "steps": 9223,
      "calories": 0,
      "distance": 7.3784207815,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-14",
      "steps": 5749,
      "calories": 0,
      "distance": 4.5992,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-15",
      "steps": 5902,
      "calories": 0,
      "distance": 4.7208,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```


#### Monthly 기준
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-11",
      "steps": 115955,
      "calories": 0,
      "distance": 92.7663207905,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "date": "2024-12",
      "steps": 113882,
      "calories": 0,
      "distance": 91.1057166377,
      "recordkey": "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```

<br>

### Data4
- recordkey: "e27ba7ef-8bb2-424c-af1d-877e826b7487"

#### Daily 기준
```json
{
  "success": true,
  "data": [
    {
      "daily": "2024-11-15",
      "steps": 6672,
      "calories": 0,
      "distance": 5.3376,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-16",
      "steps": 12449,
      "calories": 0,
      "distance": 9.96,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-17",
      "steps": 233,
      "calories": 0,
      "distance": 0.1864,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-18",
      "steps": 13602,
      "calories": 0,
      "distance": 10.8808,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-19",
      "steps": 12193,
      "calories": 0,
      "distance": 9.7536,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-20",
      "steps": 5402,
      "calories": 0,
      "distance": 4.3208,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-21",
      "steps": 9570,
      "calories": 0,
      "distance": 7.6552,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-22",
      "steps": 8612,
      "calories": 0,
      "distance": 6.8904,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-23",
      "steps": 8243,
      "calories": 0,
      "distance": 6.5944,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-24",
      "steps": 8,
      "calories": 0,
      "distance": 0.0064,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-25",
      "steps": 12802,
      "calories": 0,
      "distance": 10.2408,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-26",
      "steps": 8143,
      "calories": 0,
      "distance": 6.5136,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-27",
      "steps": 6673,
      "calories": 0,
      "distance": 5.3376,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-28",
      "steps": 7839,
      "calories": 0,
      "distance": 6.272,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-29",
      "steps": 10896,
      "calories": 0,
      "distance": 8.7168,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-11-30",
      "steps": 12912,
      "calories": 0,
      "distance": 10.3296,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-01",
      "steps": 35,
      "calories": 0,
      "distance": 0.028,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-02",
      "steps": 12039,
      "calories": 0,
      "distance": 9.6312,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-03",
      "steps": 6953,
      "calories": 0,
      "distance": 5.5624,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-04",
      "steps": 14534,
      "calories": 0,
      "distance": 11.6264,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-05",
      "steps": 10817,
      "calories": 0,
      "distance": 8.6536,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-06",
      "steps": 10765,
      "calories": 0,
      "distance": 8.6128,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-07",
      "steps": 7486,
      "calories": 0,
      "distance": 5.988,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-08",
      "steps": 12306,
      "calories": 0,
      "distance": 9.8456,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-09",
      "steps": 10958,
      "calories": 0,
      "distance": 8.7664,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-10",
      "steps": 12939,
      "calories": 0,
      "distance": 10.3504,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-11",
      "steps": 12589,
      "calories": 0,
      "distance": 10.0712,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-12",
      "steps": 8808,
      "calories": 0,
      "distance": 7.0464,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-13",
      "steps": 10484,
      "calories": 0,
      "distance": 8.388,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-14",
      "steps": 5351,
      "calories": 0,
      "distance": 4.28,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "daily": "2024-12-15",
      "steps": 788,
      "calories": 0,
      "distance": 0.6304,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```


#### Monthly 기준
```json
{
  "success": true,
  "data": [
    {
      "date": "2024-11",
      "steps": 136249,
      "calories": 0,
      "distance": 108.996,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    },
    {
      "date": "2024-12",
      "steps": 136852,
      "calories": 0,
      "distance": 109.4808,
      "recordkey": "e27ba7ef-8bb2-424c-af1d-877e826b7487",
      "userId": 1,
      "timezone": "+09:00"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```
