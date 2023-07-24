# 💵 AccountSystem
## 🎯 서비스 개요
#### 계좌 생성, 조회, 해지 + 거래, 거래취소, 거래 조회 기능 제공하는 계좌 서비스 

# 📄 API 명세서
## ✅ 계좌 관련 API
## 1) 계좌 생성
   a. 파라미터 : 사용자 아이디, 초기 잔액

   b. 결과

   i. 실패 : 사용자 없는 경우, 계좌가 10개(사용자당 최대 보유 가능 계좌) 인 경우 실패 응답

   ii. 성공
- 계좌번호는 10자리 랜덤 숫자 - 만약 중복시 ACCOUNTNUMBER_ALREADY_USED 예외 발생
- 응답정보 : 사용자 아이디, 생성된 계좌 번호, 등록일시(LocalDateTime)

[ 계좌생성 시 주의사항 ]
- 계좌 생성 시 계좌 번호는 10자리의 정수로 구성되며, 기존에 동일 계좌 번호가 있는지 중복체크를 해야 한다.
- 기본적으로 계좌번호는 순차 증가 방식으로 생성한다. (응용하는 방식으로는 계좌 번호를 랜덤 숫자 10자리로 구성하는 것도 가능하며, 랜덤생성이 현업과 더욱 유사하니 참고해주세요.)

## 2) 계좌 해지
   a. 파라미터 : 사용자 아이디, 계좌 번호

   b. 결과

   i. 실패 : 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우, 계좌가 이미 해지 상태인 경우, 잔액이 있는 경우 실패 응답
   
ii. 성공
- 응답 : 사용자 아이디, 계좌번호, 해지일시

## 3) 계좌 확인
   a. 파라미터 : 사용자 아이디

   b. 결과

   i. 실패 : 사용자 없는 경우 실패 응답

   ii. 성공
- 응답 : (계좌번호, 잔액) 정보를 Json list 형식으로 응답

## ✅ 거래(Transaction) 관련 API
## 1) 잔액 사용
   a. 파라미터 : 사용자 아이디, 계좌 번호, 거래 금액

   b. 결과

   i. 실패 : 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우, 계좌가 이미 해지 상태인 경우, 거래금액이 잔액보다 큰 경우, 거래금액이 너무 작거나 큰 경우 실패 응답

   ii. 성공

- 응답 : 계좌번호, transaction_result, transaction_id, 거래금액, 거래일시

## 2) 잔액 사용 취소
   a. 파라미터 : transaction_id, 계좌번호, 거래금액

   b. 결과

   i. 실패 : 원거래 금액과 취소 금액이 다른 경우, 트랜잭션이 해당 계좌의 거래가 아닌경우 실패 응답

   ii. 성공

- 응답 : 계좌번호, transaction_result, transaction_id, 취소 거래금액, 거래일시

## 3) 거래 확인
   a. 파라미터 : transaction_id

   b. 결과

   i. 실패 : 해당 transaction_id 없는 경우 실패 응답

   ii. 성공

- 응답 : 계좌번호, 거래종류(잔액 사용, 잔액 사용 취소), transaction_result, transaction_id, 거래금액, 거래일시
- 성공거래 뿐 아니라 실패한 거래도 거래 확인할 수 있도록 합니다.


# 🤔 관심가진 부분
> AccountService와 TransactionService의 경우 향후 변경가능성이 있다고 판단하여 **인터페이스**로 구현.

> mokito를 사용하여 Service와 Controller에 대한 **유닛 테스트** 작성.