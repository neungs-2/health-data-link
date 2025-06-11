


## 1. 데이터베이스 설계 ERD

![ERD 이미지](https://github.com/user-attachments/assets/6a893ca1-d479-4beb-a4a9-39c2f2b97d6b)

### Table List
* **steps_record**
  * 개별 data entry 정보
  * PK : step_record_id (auto increment)

* **monthly_summary**
  * recordkey 단위의 월별 통계
  * PK : user_id, recordkey, date (복합키)

* **daily_summary**
  * recordkey 단위의 일별 통계
  * PK : user_id, recordkey, date (복합키)

* **source**
  * recordkey 별 Data Source 메타 정보
  * PK : recordkey, user_id (복합키)

* **user**
  * 서비스 사용자
  * PK : user_id (auto increment)

### DDL
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
    timezone char(6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, date),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

# 유저 관련 API는 없기 때문에 테스트 유저 직접 Insert
INSERT INTO user (name, email) values ('tester', 'tester@email.com')
```

## 2. 샘플코드 (코멘트 추가)


## 3. 데이터 조회 결과 제출 (Daily/Monthly 레코드키 기준)
데이터는 KST 기준으로 변경하여 집계하였습니다.

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

